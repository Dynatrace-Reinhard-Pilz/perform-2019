from utils import HTTP
from utils import URL
import logging

logger = logging.getLogger(__name__)

class Measurement:
    def __init__(self, key, value, dimensions):
        self.key = key
        self.value = value
        self.dimensions = dimensions

class MetricClient:
    def __init__(self, urlbuilder, key, actuator_key, dimensions = {}):
        self.key = key
        self.actuator_key = actuator_key
        self.dimensions = dimensions
        self.urlbuilder = urlbuilder

    def withDimension(self, name, value):
        dims = dict(self.dimensions)
        dims[name] = value
        return MetricClient(self.urlbuilder, self.key, self.actuator_key, dims)

    def get(self):
        response = HTTP().json(self.url())
        if response is None:
            return []
        logger.info(response)
        availableTags = []
        if "availableTags" in response:
            availableTags = response["availableTags"]
        if len(availableTags) > 0:
            tag = availableTags[0]
            tagName = tag["tag"]
            results = []
            for tagValue in tag["values"]:
                for result in self.withDimension(tagName, tagValue).get():
                    results.append(result)
            return results

        if "measurements" not in response:
            return []
        measurements = response["measurements"]
        if len(measurements) == 0:
            return []
        measurement = measurements[0]
        if "value" not in measurement:
            return []
        return [ Measurement(self.key, measurement["value"], self.dimensions) ]

    def url(self):
        urlbuilder = self.urlbuilder.withPath("/actuator/metrics/" + self.actuator_key)
        if len(self.dimensions) > 0:
            for name, value in self.dimensions.items():
                if value is not None:
                    urlbuilder = urlbuilder.withParam("tag", name + ":" + value)
        return urlbuilder.build()