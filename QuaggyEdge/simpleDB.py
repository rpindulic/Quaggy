'''
Databases with placeholder values, used for testing various endpoints
'''

import json

class SimpleDB():

    def __init__(self, data):
        self.db = data

    @staticmethod
    def from_file(filename):
        data = json.loads(open(filename).read())
        return SimpleDB(data)

    @staticmethod
    def from_dict(dict):
        return SimpleDB(dict)

    def get(self, primaryKey):
        data = self.db.get(primaryKey)
        return data 

    def contains(self, primaryKey):
        return primaryKey in self.db

    def put(self, key, value):
        self.db[key] = value
        return

    def iteritems(self):
        return self.db.iteritems()

    def len(self):
        return len(self.db)

    def keys(self):
        return self.db.keys()

    def search(self, key, value):
        needle = None
        for pKey, data in self.db.iteritems():
            if data.get(key) == value:
                needle = self.get(pKey)
                break
        return needle