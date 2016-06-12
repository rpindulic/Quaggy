"""
Generally, it's considered better practice to raise custom exceptions
that are then handled by attached error-handlers. This centralizes 
error handling and reduces chances of something going wrong.

See: http://flask.pocoo.org/docs/0.10/patterns/apierrors/
"""

class APIException(Exception):
    def __init__(self, *args):
        """
        Define an exception in our API
        Optional parameters: status_code, message, and more_info link
        """
        self.status_code = args[0] if len(args) > 0 else 500
        self.message = args[1] if len(args) > 1 else "Something went wrong. Send us a bug report so this doesn\'t happen again!"
        self.more_info = args[2] if len(args) > 2 else "mailto:bugs@dev.quaggy.com"
        self.http_code = args[3] if len(args) > 3 else 500

    def to_dict(self):
        return {
            'status_code': self.status_code,
            'message': self.message,
            'more_info': self.more_info,
            'http_code': self.http_code
        }

class BadParameter(APIException):
    def __init__(self, resource_name, parameter):
        """
        This resource does not allow a given parameter
        """
        super(BadParameter, self).__init__(
            -1,
            "{} does not have a field '{}'".format(resource_name, parameter),
            "http://quaggy.com/api/docs/{}".format(resource_name)
            )

class BadType(APIException):
    def __init__(self, resource_name, parameter, expected_type):
        """
        This requires a given parameter to be a certain type
        """
        super(BadType, self).__init__(
            -2,
            "{} expects '{}' to be of type '{}'".format(resource_name, parameter, expected_type),
            "http://quaggy.com/api/docs/{}".format(resource_name)
            )

class NotImplemented(APIException):
    def __init__(self, resource_name, parameter, expected_type):
        """
        This requires a given parameter to be a certain type
        """
        super(BadType, self).__init__(
            -3,
            "This endpoint is not implemented.",
            "mailto:support@dev.quaggy.com"
            )

class MissingParameters(APIException):
    def __init__(self, resource_name, *parameters):
        """
        This resource requires a particular parameter
        """
        msg = ''
        if len(parameters) is 1:
            msg = "{} requires field: {}".format(resource_name, parameters[0])
        else:
            msg = "{} requires fields: {}".format(resource_name, ', '.join(parameters))
        super(MissingParameters, self).__init__(
            -4,
            msg,
            "http://quaggy.com/api/docs/{}".format(resource_name)
            )

class UserAlreadyExists(APIException):
    def __init__(self):
        super(UserAlreadyExists, self).__init__(
            -5,
            "User with this username already exists",
            "Try a different username"
            )

class InvalidCredentials(APIException):
    def __init__(self):
        super(InvalidCredentials, self).__init__(
            -5,
            "The Username/Password combination is incorrect.",
            "http://quaggy.com/api/reset"
            )

class NeedCredentials(APIException):
    def __init__(self):
        super(NeedCredentials, self).__init__(
            -6,
            "You have not logged in, or a previous login has expired.",
            "http://quaggy.com/api/login"
            )

class HaveCredentials(APIException):
    def __init__(self):
        super(HaveCredentials, self).__init__(
            -6,
            "Already logged in. Please log out and try again.",
            "http://quaggy.com/api/logout"
            )

class ValidationError(APIException):
    def __init__(self, message):
        """
        Did not passed a defined validation rule
        """
        super(ValidationError, self).__init__(
            -7,
            message,
            "http://quaggy.com/api/docs"
            )

class BadValue(APIException):
    def __init__(self, field_name, valid_values, value):
        """
        This field can only take a predefined list of values
        """
        super(BadValue, self).__init__(
            -8,
            "{} cannot be {}. Choose from: {}".format(field_name, value, ', '.join(valid_values)),
            "http://quaggy.com/api/docs/{}".format(field_name)
            )