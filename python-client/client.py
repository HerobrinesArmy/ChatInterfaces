#!/usr/bin/python3.3

import urllib.request as request
import http.cookiejar
import html.entities
import urllib.parse
import subprocess
import threading
import readline
import getpass
import random
import socket
import queue
import json
import time
import re


class ChatClient:
    def __init__(self):
        cj = http.cookiejar.CookieJar()
        self.opener = request.build_opener(request.HTTPCookieProcessor(cj),
                                           request.HTTPRedirectHandler)
        self.lmid = '0'
        self.chatroom = '8613406'
        self.domain = 'http://'
        self.domain += socket.getaddrinfo('herobrinesarmy.com', 'http')[0][4][0]
        self.htmlre = re.compile(r'\<(?:[^\>\"\']|(?:\"(?:[^\"\\]|(?:\\.))*\")\
|(?:\'(?:[^\'\\]|(?:\\.))*\'))*\>')
        self.htmlentre = re.compile(r'&([^;]+);')
        self.onreceive = []
        self.onuserchange = []
        self.postqueue = queue.Queue()
        self.lastsend = 0
        self.run = False
        self.firstdone = False
        self.users = set()
        self.lastusers = set()

    def postloop(self):
        while self.run:
            item = self.postqueue.get()
            self.postmessage(item)
            time.sleep(3)

    def start(self):
        if not self.auth(): return
        self.run = True
        self.getthread = threading.Thread(target=self.getloop, daemon=True)
        self.getthread.start()
        self.postthread = threading.Thread(target=self.postloop, daemon=True)
        self.postthread.start()

    def stop(self):
        #DOESN'T WORK
        self.run = False
        self.postqueue.join()
        self.postthread.join()
        self.getthread.join()

    def post(self, text):
        if text != '':
            self.postqueue.put(text)
            return True
        else:
            return False

    def getloop(self):
        while self.run:
            msgs = self.getmessages()
            for msg in msgs:
                text = self.unescape(msg['message'])
                user = self.striphtml(msg['user'])
                sendtime = msg['time']
                msgid = msg['message_id']
                userid = msg['user_id']
                for handler in self.onreceive:
                    handler(text, user, sendtime, msgid, userid)
            self.firstdone = True
            time.sleep(1)

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
        if self.isauthed():
            return True
        else:
            if not username:
                username = input('Username: ')
                password = getpass.getpass()
            elif not password:
                password = getpass.getpass()
            data = bytes('user=' + username + '&pass=' + password,
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
            return []
        try:
            parsed = json.loads(response[1:-1])
        except ValueError:
            return []
        self.lmid = parsed['lmid']
        self.lastusers = self.users
        self.users = set([self.striphtml(x['user'])
                          for x in parsed['users'].values()])
        if self.users != self.lastusers:
            for handler in self.onuserchange:
                handler(self.lastusers, self.users)
        try:
            msgs = list(parsed['messages'].items())
        except KeyError:
            return []
        msgs.sort()
        return [b for a, b in msgs]

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


class Cooldown:
    def __init__(self, cooldown):
        self.cooldown = cooldown
        self.last = 0

    def cast(self):
        if time.time() - self.cooldown > self.last:
            self.last = time.time()
            return True
        else:
            return False

    def left(self):
        return max(self.cooldown - time.time() + self.last, 0)


class Poster:
    def __init__(self):
        self.lists = {}
        #self.lists[cmd] = (list, single, plural, last, count)

    def add(self, cmd, file=None, single=None, plural=None):
        if file == None:
            file = cmd
        if single == None:
            single = cmd
        if plural == None:
            plural = cmd + 's'
        try:
            with open(file, 'r') as f:
                tmp = [x.strip() for x in f.readlines()]
                self.lists[cmd] = [tmp, single, plural, 0, len(tmp)]
        except IOError:
            pass
        return self

    def run(self, c):
        c = c.strip().lower().split(' ') + ['', '']
        try:
            l, single, plural, last, count = self.lists[c[0]]
        except KeyError:
            return ''
        if c[1].startswith('count'):
            return 'Number of ' + plural + ' loaded: ' + str(count)
        elif c[1].startswith('previous'):
            return 'Last posted ' + single + ' was: ' + str(last)
        else:
            try:
                last = int(c[1]) - 1
                if last < 0 or last >= count:
                    raise ValueError
            except ValueError:
                last = random.randint(0, count - 1)
            self.lists[c[0]] = (l, single, plural, last, count)
            return '[img]' + l[last] + '[/img]'


class ChatBot:
    def __init__(self, client, poster=None):
        self.client = client
        client.onreceive.append(self.handler)
        self.poster = poster
        self.on = False
        self.banlist = set()
        self.ponies = ['http://herobrinesarmy.com/smileys/biaAf.gif',
                       'http://i.imgur.com/FTizd6W.gif',
                       ':pony:']

    def post(self, text):
        self.client.post(text)

    def handler(self, text, user, sendtime, msgid, userid):
        if not self.on: return
        original = text
        loc = text.lower().find('@lucusbot ')
        if any([x in text for x in self.ponies]):
            self.post(':pokeball:')
        elif 'lucus' in text.lower() and loc == -1 and user != 'Lucus':
            if ':hug:' in text.lower() or ':manhug:' in text.lower():
                self.post('/me hugs ' + user + ' :hug:')
            elif ':tighthug:' in text.lower():
                self.post('/me hugs ' + user + ' :hug: <3')
            elif '::stare:' in text.lower():
                self.post('@' + user + ' :stare:')
            elif ':stare:' in text.lower():
                self.post('@' + user + ' ::stare:')
        elif ':allthethings:' in text:
            self.post(':att:*')
        elif loc != -1 and user not in self.banlist:
            text = text[loc + 10:].strip()
            if self.poster: tmp = self.poster.run(text)
            if self.poster and tmp != '':
                self.post('/meBot: ' + tmp)
            elif text.lower().startswith('exit'):
                self.on = False
            elif text.lower().startswith('ping'):
                self.post('/meBot: My ping time to herobrinesarmy.com is ' +
                          ping())

    def localcmd(self, cmd):
        if cmd.startswith('ban '):
            self.banlist.add(cmd[4:])
        elif cmd.startswith('unban '):
            self.banlist.discard(cmd[6:])
        elif cmd.startswith('on'):
            self.on = True
        elif cmd.startswith('off'):
            self.on = False
        elif cmd.startswith('status'):
            print('ChatBot is ' + ('on' if self.on else 'off'))


def bold(text):
    return '\033[1m' + text + '\033[22m'

def italic(text):
    return '\033[3m' + text + '\033[23m'

def underline(text):
    return '\033[4m' + text + '\033[24m'

def strikethrough(text):
    return '\033[9m' + text + '\033[29m'

rules = [
    ('[b]', '\033[1m'), ('[/b]', '\033[22m'),
    ('[i]', '\033[3m'), ('[/i]', '\033[23m'),
    ('[u]', '\033[4m'), ('[/u]', '\033[24m'),
    ('[s]', '\033[9m'), ('[/s]', '\033[29m'),
    ]

def stylize(text):
    for rule in rules:
        text = text.replace(*rule)
    return text

def ping(url='herobrinesarmy.com'):
    try:
        p = subprocess.Popen(['ping', '-c1', url],
                             stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        l = [x.decode('utf-8') for x in iter(p.stdout.readline, b'')]
        return [x for x in l if x.startswith('rtt')][0].split('/')[4]
    except:
        return 'failure'


if __name__ == '__main__':
    def printmid(text):
        if text == '': return
        l = len(text.replace('\033[1m', '').replace('\033[0;0m', ''))
        buffer = readline.get_line_buffer()
        print('\r' + text + ' ' * (79 - l) + '\n  > ' + buffer, end='')
        readline.redisplay()
    def printchat(text, user, sendtime, msgid, userid):
        user = bold(user)
        text = stylize(text)
        if text.startswith('/me'):
            text = '* ' + user + text[3:]
        else:
            text = user + ': ' + text
        text = sendtime + ' ' + text
        printmid(text)

    def userhandler(lastusers, users):
        left = lastusers - users
        joined = users - lastusers
        if left:
            printmid('Users left: ' +
                     ', '.join([bold(user) for user in list(left)]))
        if joined:
            printmid('Users joined: ' +
                     ', '.join([bold(user) for user in list(joined)]))

    client = ChatClient()
    poster = Poster().add('wolf', plural='wolves')
    lastusers = set()
    if not client.auth():
        print('Could not authorize.')
        exit()
    client.onreceive.append(printchat)
    client.onuserchange.append(userhandler)
    client.start()
    cmd = input('  > ')
    while not client.firstdone:
        time.sleep(0)
    bot = ChatBot(client, poster)
    while cmd not in ['/exit', '/quit']:
        if cmd == '':
            pass
        elif cmd.startswith('/bot '):
            bot.localcmd(cmd[5:])
        elif cmd.startswith('/user'):
            print(', '.join([bold(user) for user in list(client.users)]))
        elif cmd.startswith('/calc '):
            try:
                client.post(cmd[6:] + ' = ' + str(eval(cmd[6:])))
            except:
                print('That did not work...')
        elif cmd.startswith('/wolf'):
            tmp = poster.run(cmd[1:])
            if tmp.startswith('[img]'):
                client.post(tmp)
            else:
                print(tmp)
        elif cmd.startswith('/eval '):
            print(str(eval(cmd[6:])))
        elif cmd.startswith('/ping'):
            print('Ping time to herobrinesarmy.com: ' + ping())
        elif cmd.startswith('/robo '):
            client.post(' '.join('-'.join(list(x))
                                 for x in cmd[6:].split(' ')))
        elif cmd.startswith('/room '):
            if not client.setchannel(cmd[6:]):
                print('WARNING: Could not switch channels because ' +
                         'the post queue is not empty.')
        else:
            client.post(cmd)
        cmd = input('  > ')
