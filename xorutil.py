#xor
import base64


KEY = b"FIRETECH"
KEY_LENGTH = 8

def xor(name):
    name = bytes(name,encoding='utf8')
    xoredName = ''
    for i in range(len(name)):
        xoredName += chr(name[i]^KEY[i%KEY_LENGTH])
    return xoredName

def base64encode(name):
    return(base64.b64encode(name.encode(encoding='utf-8')))
    
def base64decode(name):
    return(base64.b64decode(name))

file = open('names.txt','r')
while (True):
    name = file.readline()
    if not name:
        break
    print(base64encode(xor(name.strip())).decode())

