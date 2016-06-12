from util import validate
from util.validate import ValidatorFactory
from util.apiexceptions import ValidationError

def _get_user_and_pass_from_json(jsonData):
    username = jsonData.get('username')
    password = jsonData.get('password')

    validate.UsernameValidator().test(username)
    validate.PasswordValidator().test(password)
    return username, password

def _get_filters_from_json(jsonData):
    return ValidatorFactory.create('filter', 'filter').test(jsonData)

def _get_plan_from_json(jsonData):
    return ValidatorFactory.create('plan', 'plan').test(jsonData)

def _get_filter_name_from_json(jsonData):
    return ValidatorFactory.create('string', 'string').test(jsonData.get('filter_name'))