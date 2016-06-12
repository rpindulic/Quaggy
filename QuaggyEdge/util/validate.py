from util.apiexceptions import ValidationError, BadParameter, BadValue

from collections import Iterable, Mapping

# Make sure this inherits from object and creates a 'new style class'
class Validator(object):
    """
    A validator tests an argument.
    If the input is declared invalid, a ValidatorError is thrown.
    """

    def __init__(self):
        # Do nothing
        return

    def test(self, username):
        raise Exception("Cannot run validator directly.")


class UsernameValidator(Validator):

    def __init__(self):
        super(UsernameValidator, self).__init__()

    def test(self, username):
        if username in (None, ""):
            raise ValidationError("Username field was empty")


class PasswordValidator(Validator):

    MIN_PASSWORD_LENGTH = 6

    def __init__(self):
        super(PasswordValidator, self).__init__()
    
    def test(self, password):
        if password in (None, ""):
            raise ValidationError("Password field was empty.")
        if len(password) < self.MIN_PASSWORD_LENGTH:
            raise ValidationError(
                "Password must be at least %s characters."
                % self.MIN_PASSWORD_LENGTH
            )

class ValidatorFactory (object):
    """
    Exposes static methods to register and access custom validators
    This allows easy extension of the validator functionality
    """
    validator_map = {}

    @classmethod
    def register(cls, validator):
        if validator.label:
            cls.validator_map[validator.label.lower()] = validator

    @classmethod
    def create(cls, label, *args, **kwargs):
        validator = cls.validator_map.get(label.lower(), None)
        if validator:
            validator = validator(*args, **kwargs)
        else:
            pass
        return validator

import util.basicValidate
import util.customValidate