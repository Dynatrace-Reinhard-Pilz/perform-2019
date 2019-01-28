import json
import requests
from ruxit.api.base_plugin import RemoteBasePlugin
import logging
import urllib.parse
import socket
from utils import URL
from utils import HTTP
from metrics import Measurement
from metrics import MetricClient

logger = logging.getLogger(__name__)
request = HTTP()

# http://ec2-52-41-83-14.us-west-2.compute.amazonaws.com:8080

class HOTDayPluginRemote(RemoteBasePlugin):
    def initialize(self, **kwargs):
        config = kwargs['config']
        # logger.info("Config: %s", config)
        self.url = config["url"]
        json_config = kwargs["json_config"]
        if "metrics" not in json_config:
            self.metrics = None
        self.metrics = json_config["metrics"]
        self.port = 8090
        host_and_port = urllib.parse.urlparse(self.url).netloc
        host_and_port_array = host_and_port.split(":")
        self.hostname = host_and_port_array[0]
        if (len(host_and_port_array) > 1):
            self.port = int(host_and_port_array[1])


    def query(self, **kwargs):
        group_name = "Token Gateway"
        group = self.topology_builder.create_group(group_name, group_name)
        device_name = self.hostname
        device = group.create_element(device_name, device_name)
        device.add_endpoint(self.hostname, self.port)
        device.add_endpoint(socket.gethostbyname(self.hostname), self.port)        
        endpoint_ip = request.get("http://%s:%d/ip" % (self.hostname, self.port))
        if endpoint_ip is not None:
            device.add_endpoint(endpoint_ip, self.port, dnsNames = [ self.hostname ])
        endpoint_ip = socket.gethostbyname(self.hostname)
        if endpoint_ip is not None:
            device.add_endpoint(endpoint_ip, self.port, dnsNames = [ self.hostname ])
        response = request.json("http://%s:%d/actuator/env" % (self.hostname, self.port))
        if response is not None:
            for property_source in response["propertySources"]:
                if property_source["name"] == "systemProperties":
                    system_properties = property_source["properties"]
                    device.report_property("Operating System", system_properties["os.name"]["value"] + " " + system_properties["os.version"]["value"])
                    device.report_property("Time Zone", system_properties["user.timezone"]["value"])
        measurements = []
        for metric in self.metrics:
            client = MetricClient(URL(self.hostname, self.port), metric["timeseries"]["key"], metric["source"]["key"])
            for measurement in client.get():
                measurements.append(measurement)
        for measurement in measurements:
            logger.info("key: %s, value: %d" % (measurement.key, measurement.value))
            device.absolute(key=measurement.key, value=measurement.value, dimensions = measurement.dimensions)