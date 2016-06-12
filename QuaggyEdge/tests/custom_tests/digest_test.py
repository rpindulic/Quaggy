#!venv/bin/python

import json, requests

BASEURL = 'http://localhost:5000'

print '\nStarting Tests @ {}...\n'.format(BASEURL)
s = requests.session()
url = BASEURL + "/api/features?Id=24&HistoryDays=9&BuyMode=Instant&SellMode=Bid"

# # Signup
# print "\n Signing up as test user"
# data = {
#     'username': 'test',
#     'password': 'test_pw'
# }
# print s.post(BASEURL + '/api/signup', json=data).json()

print "\n Trying to query feature vector info"
print s.get(url).json()