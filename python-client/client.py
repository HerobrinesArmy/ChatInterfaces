import urllib.request as request
import http.cookiejar
import html.entities
import urllib.parse
import threading
import readline
import getpass
import random
import socket
import queue
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
        self.lastsend = 0
        self.postqueue = queue.Queue()
        self.run = False

    def postloop(self):
        while self.run:
            item = self.postqueue.get()
            self.postmessage(item)
            time.sleep(2)

    def start(self):
        if not self.auth(): return
        self.run = True
        self.getthread = threading.Thread(target=self.getloop, daemon=True)
        self.getthread.start()
        self.postthread = threading.Thread(target=self.postloop, daemon=True)
        self.postthread.start()

    def stop(self):
        self.run = False
        self.postqueue.join()
        self.postthread.join()
        self.getthread.join()

    def post(self, text):
        self.postqueue.put(text)

    def getloop(self):
        msgs = self.getmessages()
        while self.run:
            for msg in msgs:
                text = self.unescape(msg['message'])
                user = self.striphtml(msg['user'])
                for handler in self.onreceive:
                    handler(text, user)
            time.sleep(1)
            msgs = self.getmessages()

    def setchannel(self, channel):
        if not self.postqueue.empty():
            return False
        if channel == 'main':
            channel = '8613406'
        if channel == 'meeting room':
            channel = '3'
        if channel == 'wolf':
            channel = '490'
        self.channel = channel
        self.lmid = '0'
        return True

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
        return self.isauthed()

    def isauthed(self):
        return self.opener.open(self.domain + '/amiauth').read() == b'Yeah.'

    def getmessages(self):
        url = self.domain + '/update_chat2.php?c=' + self.chatroom
        url += '&l=' + self.lmid + '&p=0'
        try:
            response = self.opener.open(url, timeout=60).read().decode('utf-8')
        except:
            pass
        try:
            parsed = json.loads(response[1:-1])
        except ValueError:
            return []
        self.lmid = parsed['lmid']
        self.users = parsed['users']
        try:
            msgs = list(parsed['messages'].items())
            msgs.sort()
            return [b for a, b in msgs]
        except KeyError:
            return []

    def postmessage(self, text):
        encoded = urllib.parse.quote(text.strip()[:512])
        if encoded == '': return False
        if time.time() - self.lastsend < 2:
            time.sleep(2)
        self.opener.open(self.domain + '/post_chat.php?c=' + self.chatroom +
                         '&o=1&m=' + encoded, timeout=60)
        self.lastsend = time.time()

    def striphtml(self, text):
        return self.htmlre.sub('', text)

    def unescape(self, text):
        return self.htmlentre.sub(lambda x:
                html.entities.entitydefs[x.group(1)], text)


if __name__ == '__main__':
    def printmid(text):
        buffer = readline.get_line_buffer()
        print('\r' + text + ' ' * min(len(buffer) - len(text) + 4, 79 -
                                      len(text)) + '\n  > ' + buffer, end='')
        readline.redisplay()
    def printchat(text, user):
        if text.startswith('/me'):
            text = '* ' + user + text[3:]
        else:
            text = user + ': ' + text
        printmid(text)

    with open('../HA-Chat-bash/wolf.txt', 'r') as f:
        wolves = [x.strip() for x in f.readlines()]
    lastwolf = 0
    wolfcount = len(wolves)
    try:
        client = ChatClient()
        if not client.auth():
            print('Could not authorize.')
            exit()
        client.onreceive.append(printchat)
        client.start()
        cmd = ''
        while cmd not in ['/exit', '/quit']:
            if cmd == '':
                pass
            elif cmd.startswith('/wolf'):
                c = cmd[6:].strip()
                if c.startswith('count'):
                    printmid('Number of wolves loaded: ' + str(wolfcount))
                elif c.startswith('previous'):
                    printmid('Last posted wolf was: ' + str(lastwolf + 1))
                else:
                    if ' ' in c: c = c[:c.find(' ')]
                    try:
                        lastwolf = int(c) - 1
                        if lastwolf < 0 or lastwolf >= wolfcount:
                            raise ValueError
                    except ValueError:
                        lastwolf = random.randint(0, wolfcount - 1)
                    client.post('[img]' + wolves[lastwolf] + '[/img]')
            elif cmd.startswith('/eval '):
                print(str(eval(cmd[6:]))[:512])
            elif cmd.startswith('/robo '):
                client.post(' '.join('-'.join(list(x))
                                     for x in cmd[6:].split(' ')))
            elif cmd.startswith('/room '):
                if not client.setchannel(cmd[6:]):
                    printmid('WARNING: Could not switch channels because ' +
                             'the post queue is not empty.')
            else:
                client.post(cmd)
            cmd = input('  > ')
    except Exception as e:
        #client.stop()
        raise e
    #client.stop()
