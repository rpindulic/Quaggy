from util.validate import ValidatorFactory as Factory
from util.basicValidate import BasicTypeValidator
from util.gw2Validate import *
from util.apiexceptions import ValidationError

import sys
from os import path
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))))
from datastore import Datastore 

import re

class PlanValidator(BasicTypeValidator):
    label = 'plan'

    def __init__(self, fieldName, forbidNone=False):
        super(PlanValidator, self).__init__(fieldName, forbidNone)

    def test(self, valueToTest):
        if valueToTest is None:
            raise ValidationError("No plan provided")

        # Create the filters that we'll need
        mode_validate = Factory.create('mode', 'mode')
        history_days_validate = Factory.create('history', 'history')
        int_validate = Factory.create('int', 'int')
        dict_validate = Factory.create('dict', 'dict')
        # Filter the input as a dict
        plan = dict_validate.test(valueToTest)
        # Use the filters to get the result
        result = {}
        result['Id'] = int_validate.test(plan.get('Id'))
        result['HistoryDays'] = history_days_validate.test(plan.get('HistoryDays'))
        result['BuyMode'] = mode_validate.test(plan.get('BuyMode'))
        result['SellMode'] = mode_validate.test(plan.get('SellMode'))
        return result

Factory.register(PlanValidator)


class FilterValidator(BasicTypeValidator):
    label = 'filter'

    def __init__(self, fieldName, forbidNone=False):
        super(FilterValidator, self).__init__(fieldName, forbidNone)

    def test(self, valueToTest):
        if valueToTest is None:
            raise ValidationError("No filter provided")

        # Create the filters that we will need
        str_validate = Factory.create('string', 'string')
        int_validate = Factory.create('int', 'int')
        float_validate = Factory.create('float', 'float')
        list_validate = Factory.create('list', 'list')
        dict_validate = Factory.create('dict', 'dict')
        mode_validate = Factory.create('mode', 'mode')
        feature_validate = Factory.create('feature', 'feature')
        order_validate = Factory.create('order', 'order')
        item_type_validate = Factory.create('item_type', 'item_type')
        history_days_validate = Factory.create('history', 'history')

        result = {}
        # Verify that each individual filter is valid
        for name, fil in valueToTest.iteritems():
            # Validate name
            name = str_validate.test(name)
            if len(name) <= 0 or len(name) > 30:
                raise ValidationError('Filter name must be length 1-30')
            result[name] = {}
            # Validate the filter
            fil = dict_validate.test(fil) 
            result[name]['HistoryDays'] = history_days_validate.test(fil.get('HistoryDays'))
            result[name]['BuyMode'] = mode_validate.test(fil.get('BuyMode'))
            result[name]['SellMode'] = mode_validate.test(fil.get('SellMode'))
            result[name]['SortBy'] = feature_validate.test(fil.get('SortBy'))
            result[name]['SortOrder'] = order_validate.test(fil.get('SortOrder'))
            types = list_validate.test(fil.get('Types'))
            good_types = []
            for t in types:
                t = item_type_validate.test(t)
                if t in good_types:
                    raise ValidationError('Types list cannot contain duplicates')
                good_types.append(t)
            result[name]['Types'] = good_types
            bounds = dict_validate.test(fil.get('Bounds'))
            good_bounds = {}
            for feat, dic in bounds.iteritems():
                feat = feature_validate.test(feat)
                dic = dict_validate.test(dic)
                good_bounds[feat] = {}
                min_val = dic.get('Min')
                max_val = dic.get('Max')
                if min_val is not None:
                    min_val = float_validate.test(min_val)
                    good_bounds[feat]['Min'] = min_val
                if max_val is not None:
                    max_val = float_validate.test(max_val)
                    good_bounds[feat]['Max'] = max_val
            result[name]['Bounds'] = good_bounds
        return result

Factory.register(FilterValidator)
