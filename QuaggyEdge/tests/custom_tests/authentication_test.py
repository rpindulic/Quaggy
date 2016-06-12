#!venv/bin/python

import pycurl, json, requests

BASEURL = 'http://localhost:5000'

print '\nStarting Tests @ {}...\n'.format(BASEURL)

c = pycurl.Curl()
c.setopt(c.COOKIEFILE, '')  # Without this, login will work but won't be saved for next request
c.setopt(c.HTTPHEADER, ['Content-type: application/json'])  # To pass JSON form data

# Signup
print "\n Signing up as test user"
c.setopt(c.URL, BASEURL + "/api/signup")
c.setopt(c.POSTFIELDS, json.dumps({
    'username': 'test',
    'password': 'test_pw'
    }))
c.perform()

# Try secure ping
print "\n Secure ping"
c.setopt(c.URL, BASEURL + "/api/secure_ping")
c.setopt(c.POST, 0)
c.perform()

# Logout
print "\n Logging out"
c.setopt(c.URL, BASEURL + '/api/logout')
c.setopt(c.POSTFIELDS, json.dumps({}))
c.perform()

# Try secure ping
print "\n Testing secure ping again (should fail)."
c.setopt(c.URL, BASEURL + "/api/secure_ping")
c.setopt(c.POST, 0)
c.perform()

# Log back in again
print "\n Logging back in again"
c.setopt(c.URL, BASEURL + "/api/login")
c.setopt(c.POSTFIELDS, json.dumps({
    'username': 'test',
    'password': 'test_pw'
    }))
c.perform()

# Try secure ping again
print "\n Testing secure ping again"
c.setopt(c.URL, BASEURL + "/api/secure_ping")
c.setopt(c.POST, 0)
c.perform()