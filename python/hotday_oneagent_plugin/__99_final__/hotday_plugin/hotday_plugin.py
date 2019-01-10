from ruxit.api.base_plugin import BasePlugin
from ruxit.api.snapshot import pgi_name
import json
import requests
import logging

logger = logging.getLogger(__name__)

class HOTDayPlugin(BasePlugin):
    def query(self, **kwargs):
        # Get the port from the properties
        config = kwargs['config']
        port = int(config["port"])
        
        # Get the metrics defined in the plugin.json
        json_config = kwargs["json_config"]
        metrics = json_config["metrics"]
        
        # Get the entity id
        pgi_id = kwargs["associated_entity"].group_instance_id
        
        for metric in metrics:
            # Poll the rest interface to get the metrics
            result = requests.get("http://localhost:" + str(port) + "/actuator/metrics/" + metric["source"]["key"])
            result_content = result.content.decode("utf-8")
            result_json = json.loads(result_content)
            # Response example: {"name":"hikaricp.connections.idle","description":"Idle connections","baseUnit":null,"measurements":[{"statistic":"VALUE","value":15.0}],"availableTags":[{"tag":"pool","values":["pool-c","pool-b","pool-a"]}]}
            
            value = result_json["measurements"][0]["value"]
            tags = result_json["availableTags"]
            for tag in tags:
                tagName = tag["tag"]
                for tagValue in tag["values"]:
                    self.results_builder.absolute(key=metric["timeseries"]["key"], value=value, dimensions = {tagName:tagValue}, entity_id=pgi_id)