import json
import urllib.request
import urllib.error

base = 'http://127.0.0.1:8080/api/v1'
creds = json.dumps({'username': 'operator01', 'password': 'operator01'}).encode('utf-8')
req = urllib.request.Request(base + '/auth/login', data=creds, headers={'Content-Type': 'application/json'})
try:
    with urllib.request.urlopen(req) as r:
        body = r.read().decode('utf-8')
        print('LOGIN OK', body)
        token = json.loads(body)['token']
except urllib.error.HTTPError as e:
    print('LOGIN ERROR', e.code, e.read().decode('utf-8'))
    raise

change = json.dumps({'currentPassword': 'operator01', 'newPassword': 'newpass123'}).encode('utf-8')
req = urllib.request.Request(base + '/auth/change-password', data=change, headers={'Content-Type': 'application/json', 'Authorization': f'Bearer {token}'}, method='POST')
try:
    with urllib.request.urlopen(req) as r:
        print('CHANGE OK', r.status, r.read().decode('utf-8'))
except urllib.error.HTTPError as e:
    print('CHANGE ERROR', e.code, e.read().decode('utf-8'))
