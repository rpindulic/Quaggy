from flask import request, jsonify
from werkzeug import HTTP_STATUS_CODES

from util.apiexceptions import APIException

'''
Customized JSON errors for standard HTTPExceptions raised
by the framework
'''
http_errors = {
    404: {
        'message': 'The resource you requested does not exist',
        'more_info': 'http://cloudchef.com/api/docs/endpoints'
    },
    405: {
        'message': 'The resource you requested does not allow that method',
        'more_info': 'http://cloudchef.com/api/docs/endpoints'
    },
    500: {
        'message': 'Something went wrong. Send us a bug report so this doesn\'t happen again!',
        'more_info': 'mailto:bugs@dev.cloudchef.com'
    }
}

def handleHttpError (error):
    print error
    if error.code in http_errors:
        json_fields = http_errors[error.code]
    else:
        json_fields = http_errors[500]
    json_fields['status'] = 'HTTP{}'.format(error.code)

    response = jsonify(json_fields)

    # Check if user wants the error code in header
    if not request.args.get('no_http_codes'):
        response.status_code = error.code
    return response

def handleAPIError (exp):
    if isinstance(exp, APIException):
        response = jsonify(exp.to_dict())
    else:
        # This isn't an APIException: log it, and don't scare the user
        print exp
        response = jsonify(APIException().to_dict())

    # Check if user wants the error code in header
    if not request.args.get('no_http_codes'):
        response.status_code = exp.http_code
    return response


def attachErrorHandlers (app):
    # Attach HTTP error handlers
    for code in HTTP_STATUS_CODES:
        app.register_error_handler(code, handleHttpError)

    app.register_error_handler(APIException, handleAPIError)


