#!venv/bin/python

import json, requests

BASEURL = 'http://localhost:5000'

print '\nStarting Tests @ {}...\n'.format(BASEURL)
s = requests.session()
url = BASEURL + "/api/filters"

# Signup
print "\n Signing up as test user"
data = {
    'username': 'test',
    'password': 'test_pw'
}
print s.post(BASEURL + '/api/login', json=data).json()

print "\n Trying to add a filter"
data = {'deal_filter': 
  {
  "HistoryDays": 5,
  "BuyMode": "Instant",
  "SellMode": "Bid",
  "SortBy": "MeanProfit",
  "SortOrder": "Desc",
  "Types": 
  [
    "CraftingMaterial",
    "Bag",
    "Mini",
    "Gizmo"
  ],

  "Bounds": 
  {
    "MeanProfit": 
    {
      "Min": 0.1
    },
    
    "OurBuyPrice":
    {
      "Min": 70
    },

    "NumBuyOrders": 
    {
      "Min": 200
    },

    "NumSellOrders": 
    {
      "Min": 200
    }
  }
}}
print s.post(url, json=data).json()

print "Making sure that our filter was added"
print s.get(url).json()

print "Testing applying our filter"
print s.get(BASEURL + '/api/features/filter?filter_name=deal_filter').json()