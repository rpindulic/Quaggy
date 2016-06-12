#!venv/bin/python
from functools import wraps
from flask import Flask, request, jsonify, session

import error_handler, argparse
from util import apiexceptions
from user import User
from datastore import Datastore
import sys, json

import authenticate

app = Flask(__name__)

# TODO: Generate an actual key and push it into a config file
app.secret_key = '7182398172398172371927395301238'
error_handler.attachErrorHandlers(app)

'''
Wrapper over Flask.jsonify
'''
def ValidResponse(**kwargs):
    fields = {
        'status_code': 0,
        'message': 'OK'
    }
    fields.update(kwargs)
    return jsonify(fields)

"Custom decorators to ensure login/logout using default flask sessions"
# Thanks: http://flask.pocoo.org/docs/0.10/patterns/viewdecorators/
# Using Flask's `sessions` object described here:
# http://flask.pocoo.org/docs/0.10/quickstart/#sessions

def login_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'uid' not in session:
            raise apiexceptions.NeedCredentials()

        return f(*args, **kwargs)
    return decorated_function


def logout_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'uid' in session:
            raise apiexceptions.HaveCredentials()
        return f(*args, **kwargs)
    return decorated_function


def parse_json(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if request.method == 'POST':
            jsonData = request.get_json()
        if request.method == 'GET':
            jsonData = request.args.to_dict()
        if jsonData is None:
            jsonData = {}
        return f(jsonData, *args, **kwargs)
    return decorated_function

#
# Credentials
#

@app.route('/api/signup', methods=['POST'])
@logout_required
@parse_json
def signup(jsonData):
    username, password = authenticate._get_user_and_pass_from_json(jsonData)

    if User.fromName(username) is not None:
        raise apiexceptions.UserAlreadyExists()

    user = User.create()
    user.name = username
    user.set_password(password)
    user.commit()

    session['uid'] = user.name

    return ValidResponse(user=user.to_json())


@app.route('/api/login', methods=['POST'])
@logout_required
@parse_json
def login(jsonData):
    username, password = authenticate._get_user_and_pass_from_json(jsonData)
    user = User.fromName(username)
    if user is None or password is None or not user.verify_password(password):
        raise apiexceptions.InvalidCredentials()

    session['uid'] = user.name

    return ValidResponse(user=user.to_json())


@app.route('/api/logout', methods=['POST'])
@login_required
def logout():
    del session['uid']
    return ValidResponse()


@app.route('/api/reset', methods=['POST'])
@logout_required
def reset_password():
    return apiexceptions.NotImplemented()

#
# Filters
#

@app.route('/api/filters', methods=['GET', 'POST'])
@login_required
@parse_json
def add_filter(jsonData):
    user = User.fromName(session['uid'])
    if request.method == 'POST':
        filters = authenticate._get_filters_from_json(jsonData)
        # Add each of these filters to our stored filters
        for name, fil in filters.iteritems():
            user.filters[name] = fil
        user.commit()
    return ValidResponse(filters=user.filters)

#
# Backend Operations
#

@app.route('/backend/digest', methods=['POST'])
@parse_json
def add_digest(jsonData):
    for key, val in jsonData.iteritems():
        pieces = key.split(':')
        Datastore.cache[pieces[2]][pieces[3]][pieces[1]][pieces[0]][pieces[4]] = val
    return ValidResponse(message='OK')

@app.route('/backend/cache', methods=['GET'])
def get_all():
    return ValidResponse(all=Datastore.cache)

#
# Modeling and Prediction
#

@app.route('/api/features', methods=['GET'])
@login_required
@parse_json
def get_features(jsonData):
    plan = authenticate._get_plan_from_json(jsonData)
    fv = Datastore.cache[plan.get('BuyMode')][plan.get('SellMode')][str(plan.get('HistoryDays'))][str(plan.get('Id'))]
    return ValidResponse(feature_vector=fv)

@app.route('/api/features/filter', methods=['GET'])
@login_required
@parse_json
def apply_filter(jsonData):
    user = User.fromName(session['uid'])
    filter_name = authenticate._get_filter_name_from_json(jsonData)
    filter_obj = user.filters.get(filter_name)
    if filter_obj is None:
        raise ValidationError(filter_name + ' is not a valid filter for this user')
    # fvs maps from id -> fv
    fvs = Datastore.cache[filter_obj['BuyMode']][filter_obj['SellMode']][str(filter_obj['HistoryDays'])]
    chosen = {}
    # Perform the filter
    for iid, fv in fvs.iteritems():
        # Filter for correct item type
        item_type = Datastore.item_types[int(float(fv.get('ItemType')))]
        if item_type not in filter_obj['Types']:
            continue
        # Filter for all bounds
        for f, finfo in filter_obj.get('Bounds').iteritems():
            fmax = finfo.get('Max')
            fmin = finfo.get('Min')
            if fmax is not None and fv.get(f) > fmax:
                continue
            if fmin is not None and fv.get(f) < fmin:
                continue
        # Add to map
        chosen[iid] = fv
    # TODO: Sort by specified sort type
    return ValidResponse(results=chosen)

#
# Play PingPong!
#

@app.route('/api/ping', methods=['GET', 'POST'])
@parse_json
def ping(jsonData):
    return ValidResponse(message='pong', payload=jsonData)

@app.route('/api/secure_ping', methods=['GET', 'POST'])
@login_required
@parse_json
def secure_ping(jsonData):
    return ValidResponse(message='secure_pong', payload=jsonData)

@app.route('/api/insecure_ping', methods=['GET', 'POST'])
@logout_required
@parse_json
def insecure_ping(jsonData):
    return ValidResponse(message='insecure_pong', payload=jsonData)


"""
Main entry point of our application. Any flags required are
passed in through `args`
"""
def build_app(**kwargs):

    print "Initializing app with flags: {}".format(kwargs)

    Datastore.initialize()

    return app

if __name__ == '__main__':
    # Start the test server if need-be
    parser = argparse.ArgumentParser(description="Run server")
    parser.add_argument('--test', dest='test', action='store_true', help='Run server in test mode')
    args = parser.parse_args()

    app = build_app(**(vars(args)))
    app.run()
