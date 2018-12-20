import json
import requests
import logging
import urllib.parse
import socket

logger = logging.getLogger(__name__)

class URL:
    def __init__(self, hostname, port, path = "/"):
        self.hostname = hostname
        self.port = port
        self.path = path
        self.params = {}

    def build(self):
        url = "http://%s:%d%s" % (self.hostname, self.port, self.path)
        if len(self.params) == 0:
            return url
        logger.info(self.params)
        return url + "?" + urllib.parse.urlencode(self.params, True)

    def withPath(self, path):
        return URL(self.hostname, self.port, path)

    def withParam(self, name, value):
        values = []
        if name not in self.params:
            values = [ value ]
        else:
            self.params[name].append(value)

        self.params[name] = values
        return self

class HTTP:

    def get(self, url):
        try:
            logger.info(url)
            return requests.get(url, auth=None, verify=False, timeout=2).content.decode("utf-8")
        except requests.exceptions.ConnectTimeout:
            logger.warn("requests.exceptions.ConnectTimeout")
            return None
        except requests.exceptions.ConnectionError:
            logger.warn("requests.exceptions.ConnectionError")
            return None

    """
    Basic HTTP GET functionatlity.
    Won't throw an exception in case nobody is listening on the specified port,
    but returns 'None' to signal that there is no or non json data available.
    """
    def json(self, url):
        logger.info(url)
        httpResponse = None
        try:
            httpResponse = requests.get(url, auth=None, verify=None, timeout=2)
        except requests.exceptions.ConnectTimeout:
            logger.warn("requests.exceptions.ConnectTimeout")
            return None
        except requests.exceptions.ConnectionError:
            logger.warn("requests.exceptions.ConnectionError")
            return None
        
        decoded = None
        try:
            decoded = json.loads(httpResponse.content.decode())
        except Exception as ex:
            logger.warn("Exception")
            logger.warn(str(ex))
            return None
        
        return decoded