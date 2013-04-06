import urllib.request as request
import http.cookiejar
import html.entities
import urllib.parse
import threading
import readline
import getpass
import random
import socket
import json
import time
import re

#message.[message_id, user, message, time, user_id]

class ChatClient:
    def __init__(self):
        cj = http.cookiejar.CookieJar()
        self.opener = request.build_opener(request.HTTPCookieProcessor(cj),
                                           request.HTTPRedirectHandler)
        self.username = ''
        self.password = ''
        self.lmid = '0'
        self.chatroom = '8613406'
        self.domain = 'http://'
        self.domain += socket.getaddrinfo('herobrinesarmy.com', 'http')[0][4][0]
        self.htmlre = re.compile(r'\<(?:[^\>\"\']|(?:\"(?:[^\"\\]|(?:\\.))*\")\
|(?:\'(?:[^\'\\]|(?:\\.))*\'))*\>')
        self.htmlentre = re.compile(r'&([^;]+);')
        self.onreceive = []
        self.doautohandle = False

    def startautohandle(self):
        self.doautohandle = True
        self.ahthread = threading.Thread(target=self.autohandler)
        self.ahthread.start()

    def stopautohandle(self):
        self.doautohandle = False

    def autohandler(self):
        if not self.isauthed(): return
        msgs = self.getmessages()
        while self.doautohandle:
            for msg in msgs:
                text = self.unescape(msg['message'])
                user = self.striphtml(msg['user'])
                for handler in self.onreceive:
                    handler(text, user)
            time.sleep(1)
            msgs = self.getmessages()

    def setchannel(self, channel):
        self.chatroom = channel
        self.lmid = '0'

    def auth(self, username=None, password=None):
        if username: self.username = username
        if password: self.password = password
        if self.isauthed():
            return True
        else:
            if not self.username:
                self.username = input('Username: ')
                self.password = getpass.getpass()
            elif not self.password:
                self.password = getpass.getpass()
            data = bytes('user=' + self.username + '&pass=' + self.password,
                         'utf-8')
            self.opener.open(self.domain + '/auth.php', data, timeout=60)
        if self.isauthed():
            return True
        return False

    def isauthed(self):
        return self.opener.open(self.domain + '/amiauth').read() != b'Nope.'

    def getmessages(self):
        url = self.domain + '/update_chat2.php?c=' + self.chatroom
        url += '&l=' + self.lmid + '&p=0'
        response = self.opener.open(url, timeout=60).read().decode('utf-8')
        try:
            parsed = json.loads(response[1:-1])
        except ValueError:
            return []
        self.lmid = parsed['lmid']
        self.users = parsed['users']
        try:
            return list(parsed['messages'].values())
        except KeyError:
            return []

    def postmessage(self, text):
        encoded = urllib.parse.quote(text.strip()[:512])
        if encoded == '': return False
        if not self.auth():
            return False
        self.opener.open(self.domain + '/post_chat.php?c=' + self.chatroom +
                         '&o=1&m=' + encoded, timeout=60)

    def striphtml(self, text):
        return self.htmlre.sub('', text)

    def unescape(self, text):
        return self.htmlentre.sub(lambda x:
                html.entities.entitydefs[x.group(1)], text)


if __name__ == '__main__':
    def printmid(text):
        buffer = readline.get_line_buffer()
        print('\r' + text + ' ' * min(len(buffer) - len(text) + 4, 79 - len(text))
              + '\n  > ' + buffer, end='')
        readline.redisplay()

    with open('../HA-Chat-bash/wolf.txt', 'r') as f:
        wolves = [x.strip() for x in f.readlines()]
    random.shuffle(wolves)
    nextwolf = 0
    wolfnum = len(wolves)
    try:
        client = ChatClient()
        client.auth()
        client.onreceive.append(lambda text, user: printmid(user + ': ' + text))
        client.startautohandle()
        cmd = ''
        while cmd not in ['/exit', '/quit']:
            if cmd == '':
                pass
            elif cmd.startswith('/wolf'):
                client.postmessage('[img]' + wolves[nextwolf] + '[/img]')
                nextwolf = (nextwolf + 1) % wolfnum
            elif cmd.startswith('/eval '):
                print(str(eval(cmd[5:]))[:512])
            else:
                client.postmessage(cmd)
            cmd = input('  > ')
    except Exception as e:
        client.stopautohandle()
        client.ahthread.join()
        raise e
    client.stopautohandle()
    client.ahthread.join()
