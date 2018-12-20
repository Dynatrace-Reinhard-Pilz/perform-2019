import json
import requests
from ruxit.api.base_plugin import BasePlugin
from ruxit.api.snapshot import pgi_name
import logging
import urllib.parse
import socket
from utils import URL
from utils import HTTP
from metrics import Measurement
from metrics import MetricClient

logger = logging.getLogger(__name__)
request = HTTP()

class HOTDayPlugin(BasePlugin):
    def initialize(self, **kwargs):
        config = kwargs['config']
        self.port = int(config["port"])
        json_config = kwargs["json_config"]
        if "metrics" not in json_config:
            self.metrics = None
        self.metrics = json_config["metrics"]

    def query(self, **kwargs):
        pgi = self.find_single_process_group(pgi_name("tokenizer-*.jar"))
        pgi_id = pgi.group_instance_id
        measurements = []
        for metric in self.metrics:
            client = MetricClient(URL("localhost", self.port), metric["timeseries"]["key"], metric["source"]["key"])
            for measurement in client.get():
                measurements.append(measurement)
        for measurement in measurements:
            logger.info("key: %s, value: %d" % (measurement.key, measurement.value))
            self.results_builder.absolute(key=measurement.key, value=measurement.value, dimensions = measurement.dimensions, entity_id=pgi_id)