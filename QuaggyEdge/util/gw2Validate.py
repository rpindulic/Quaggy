from util.validate import ValidatorFactory as Factory
from util.basicValidate import BasicTypeValidator
from util.apiexceptions import ValidationError

import sys, re
from os import path
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))))
from datastore import Datastore 

# Supports validators for GW2 types

class ModeValidator(BasicTypeValidator):
    label = 'mode'

    def __init__(self, fieldName, forbidNone=False):
        super(ModeValidator, self).__init__(fieldName, forbidNone)

    def test(self, valueToTest):
        if valueToTest is None:
            raise ValidationError("No mode provided")
        # Parse mode to string
        str_validate = Factory.create('string', 'string')
        mode = str_validate.test(valueToTest)
        # Verify that it is a valid value
        if mode != 'Instant' and mode != 'Bid':
          raise ValidationError('Mode must be Instant or Bid')
        return mode

Factory.register(ModeValidator)


class FeatureValidator(BasicTypeValidator):
    label = 'feature'

    def __init__(self, fieldName, forbidNone=False):
        super(FeatureValidator, self).__init__(fieldName, forbidNone)
        self.features = ['ItemID', 'ItemType', 'ItemRarity', 'ItemLevel', 'NumBuyOrders',
          'NumSellOrders', 'BuyPrice', 'SellPrice', 'ZScoreBuyPrice', 'ZScoreSellPrice',
          'MeanBuyPrice', 'MeanSellPrice', 'VarBuyPrice', 'VarSellPrice', 'MedianBuyPrice',
          'MedianSellPrice', 'SlopeBuyPrice', 'SlopeSellPrice', 'CurrentFlipProfit',
          'MeanProfit', 'VarProfit', 'MedianProfit', 'OurBuyPrice', 'NumConsidered']

    def test(self, valueToTest):
        if valueToTest is None:
            raise ValidationError("No feature provided")
        # Parse feature to string
        str_validate = Factory.create('string', 'string')
        feature = str_validate.test(valueToTest)
        # Verify that it is a valid value
        if feature not in self.features:
          raise ValidationError(feature + ' is not a valid feature')
        return feature

Factory.register(FeatureValidator)

class HistoryDaysValidator(BasicTypeValidator):
    label = 'history'

    def __init__(self, fieldName, forbidNone=False):
        self.allowed_days = [1,2,3,4,5,6,7,8,9,10,15,20,25,30,35,40,45,
            50,75,100];
        super(HistoryDaysValidator, self).__init__(fieldName, forbidNone)

    def test(self, valueToTest):
        if valueToTest is None:
            raise ValidationError("No history days provided")
        int_validate = Factory.create('int', 'int')
        days = int_validate.test(valueToTest)
        if days not in self.allowed_days:
            raise ValidationError(str(days) + " is not a valid history day amount")
        return days

Factory.register(HistoryDaysValidator)


class OrderValidator(BasicTypeValidator):
    label = 'order'

    def __init__(self, fieldName, forbidNone=False):
        super(OrderValidator, self).__init__(fieldName, forbidNone)

    def test(self, valueToTest):
        if valueToTest is None:
            raise ValidationError("No order provided")
        # Parse order to string
        str_validate = Factory.create('string', 'string')
        order = str_validate.test(valueToTest)
        # Verify that it is a valid value
        if order != 'Asc' and order != 'Desc':
          raise ValidationError('Order must be Asc or Desc')
        return order

Factory.register(OrderValidator)


class ItemTypeValidator(BasicTypeValidator):
    label = 'item_type'

    def __init__(self, fieldName, forbidNone=False):
        super(ItemTypeValidator, self).__init__(fieldName, forbidNone)
        self.item_types = ['Armor', 'Back', 'Bag', 'Consumable', 'Container',
          'CraftingMaterial', 'Gizmo', 'Mini', 'Trinket', 'Trophy',
          'UpgradeComponent', 'Weapon']

    def test(self, valueToTest):
        if valueToTest is None:
            raise ValidationError("No item type provided")
        # Parse item type to string
        str_validate = Factory.create('string', 'string')
        item_type = str_validate.test(valueToTest)
        # Verify that it is a valid value
        if item_type not in self.item_types:
          raise ValidationError(item_type + ' is not a valid item type')
        return item_type


Factory.register(ItemTypeValidator)