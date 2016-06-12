from simpleDB import SimpleDB
import sys

from os import path
from collections import defaultdict

base_path = path.dirname(path.abspath(__file__))

class Datastore(object):

    @classmethod
    def initialize (cls):
        # Map from username -> user data
        cls.userdata = SimpleDB({})
        # Map from buy mode -> sell mode -> history days -> id -> feature vector
        cls.cache = defaultdict(lambda: defaultdict(lambda: defaultdict(lambda: defaultdict(lambda: {}))))
        # Map from item type (index) -> name
        cls.item_types = ['Armor', 'Back', 'Bag', 'Consumable', 'Container', 'CraftingMaterial', \
          'Gizmo', 'Mini', 'Trinket', 'Trophy', 'UpgradeComponent', 'Weapon']