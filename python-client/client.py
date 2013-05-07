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
        self.username = ''
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
        self.userc = {}
        self.tagtotermc = {
            '228373': '\033[38;5;172m',    #c60 'd46a00', Aerospace
            '228361': '\033[38;5;075m',    #39f '3888ff', Science
            '228640': '\033[38;5;133m',    #939 '82329c', Transport
            '228677': '\033[38;5;196m',    #f00 'dd2423', Fleet
            '232043': '\033[38;5;028m',    #060 '007500', Scavenging
            '271886': '\033[38;5;145m',    #999 'a6a6a6', Unenlisted
            '228080': '\033[38;5;226m',    #ff0 'ffff00', High Command
            '223534': '\033[38;5;160m',    #c00 'd12626', Herobrine
            }                              #Default is /033[39m


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
        if username: self.username = username
        if self.isauthed():
            return True
        else:
            if not username:
                username = input('Username: ')
                password = getpass.getpass()
                self.username = username
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
        self.users = set([x['user_id'] for x in parsed['users'].values()])
        [self.adduserc(u) for u in parsed['users'].values()]
        try:
            [self.adduserc(u) for u in parsed['messages'].values()]
        except KeyError:
            pass
        if self.users != self.lastusers:
            for handler in self.onuserchange:
                handler(self.lastusers, self.users)
        try:
            msgs = list(parsed['messages'].items())
        except KeyError:
            return []
        msgs.sort()
        return [b for a, b in msgs]

    def adduserc(self, u):
        tagl = u['user'].find('tag-')
        name = self.tagtotermc[u['user'][tagl + 4:tagl + 10]]
        name += self.striphtml(u['user']) + '\033[39m'
        self.userc[u['user_id']] = name

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

    def add(self, cmd, file=None, single=None, plural=None, pre='[img]',
            post='[/img]'):
        if file == None:
            file = cmd
        if single == None:
            single = cmd
        if plural == None:
            plural = cmd + 's'
        try:
            with open(file, 'r') as f:
                tmp = [x.strip() for x in f.readlines()]
                self.lists[cmd] = (tmp, single, plural, 0, len(tmp), pre, post)
        except IOError:
            pass
        return self

    def run(self, c):
        c = c.strip().lower().split(' ') + ['', '']
        try:
            l, single, plural, last, count, pre, post = self.lists[c[0]]
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
            self.lists[c[0]] = (l, single, plural, last, count, pre, post)
            return pre + l[last] + post


class ChatBot:
    def __init__(self, client):
        global player, poster
        self.client = client
        client.onreceive.append(self.handler)
        self.poster = poster
        self.player = player
        self.on = self.client.username == 'Lucus'
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
                          ping() + 'ms')
            elif text.lower().startswith('song status'):
                status = self.player.status()
                if status == '':
                    self.post('/meBot: Nothing playing right now.')
                else:
                    self.post('/meBot: Current song playing: [youtube]' +
                              status + '[/youtube]')

    def localcmd(self, cmd):
        if cmd.startswith('ban '):
            self.banlist.add(cmd[4:])
        elif cmd.startswith('unban '):
            self.banlist.discard(cmd[6:])
        elif cmd.startswith('on'):
            self.on = True
            print('ChatBot is on')
        elif cmd.startswith('off'):
            self.on = False
            print('ChatBot is off')
        elif cmd.startswith('status'):
            print('ChatBot is ' + ('on' if self.on else 'off'))


class MusicPlayer:
    def __init__(self):
        self.playproc = None
        self.playing = ''
        self.on = True

    def play(self, url):
        if self.playproc != None and self.playproc.poll() == None:
            return False
        if url == '' or not self.on:
            return False
        self.playing = url
        self.playproc = subprocess.Popen(['cvlc', url, '--no-video',
                                          '--play-and-exit'],
                                         stdout=subprocess.DEVNULL,
                                         stderr=subprocess.DEVNULL)
        return True

    def stop(self):
        self.playproc.terminate()

    def status(self):
        if self.playproc.poll() != None:
            self.playing = ''
        return self.playing

    def localcmd(self, c):
        c = c.split(' ') + ['', '']
        if c[0] == 'play':
            self.play(c[1])
        elif c[0] == 'stop':
            self.stop()
        elif c[0] == 'status':
            print('Currently playing: ' + self.status())


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

def getuser(usrid):
    return client.userc[usrid]

def ping(url='herobrinesarmy.com'):
    try:
        p = subprocess.Popen(['ping', '-c1', url],
                             stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        l = [x.decode('utf-8') for x in iter(p.stdout.readline, b'')]
        return [x for x in l if x.startswith('rtt')][0].split('/')[4]
    except:
        return 'failure'

def run(cmd):
    p = subprocess.Popen(cmd.split(' '), stdout=subprocess.PIPE)
    return [x.decode('utf-8') for x in iter(p.stdout.readline, b'')]


if __name__ == '__main__':
    def printmid(text):
        if text == '': return
        l = len(text)
        buffer = readline.get_line_buffer()
        print('\r' + text + ' ' * (79 - l) + '\n  > ' + buffer, end='')
        readline.redisplay()
    def printchat(text, user, sendtime, msgid, userid):
        global lastsong
        ys = text.find('[youtube]')
        ye = text.find('[/youtube]')
        if text.startswith('/meBot: Current song playing:'):
            ys = -1
        otext = text
        user = getuser(userid)
        text = stylize(text)
        if text.startswith('/me'):
            text = '* ' + user + text[3:]
        else:
            text = user + ': ' + text
        text = sendtime + ' ' + text
        printmid(text + '\033[0m')
        if ys != -1:
            lastsong = (otext + ' ')[ys + 9:ye]
            printmid('Type /play to listen to this song')

    def userhandler(lastusers, users):
        left = lastusers - users
        joined = users - lastusers
        if left:
            printmid('Users left: ' +
                     ', '.join([getuser(user) for user in list(left)]))
        if joined:
            printmid('Users joined: ' +
                     ', '.join([getuser(user) for user in list(joined)]))

    client = ChatClient()
    poster = Poster().add('wolf', plural='wolves')
    poster.add('song', pre='[youtube]http://youtube.com/watch?v=',
               post='[/youtube]')
    player = MusicPlayer()
    lastusers = set()
    lastsong = ''
    if not client.auth():
        print('Could not authorize.')
        exit()
    client.onreceive.append(printchat)
    client.onuserchange.append(userhandler)
    client.start()
    cmd = input('  > ')
    while not client.firstdone:
        time.sleep(0)
    bot = ChatBot(client)
    while cmd not in ['/exit', '/quit']:
        if cmd == '':
            pass
        elif cmd.startswith('/bot '):
            bot.localcmd(cmd[5:])
        elif cmd.startswith('/calc '):
            try:
                client.post(cmd[6:] + ' = ' + str(eval(cmd[6:])))
            except:
                print('That did not work...')
        elif cmd.startswith('/eval '):
            print(str(eval(cmd[6:])))
        elif cmd.startswith('/music '):
            player.localcmd(cmd[7:])
        elif cmd == '/n':
            client.post(':ninja:')
        elif cmd.startswith('/ping'):
            print('Ping time to herobrinesarmy.com: ' + ping() + 'ms')
        elif cmd.startswith('/play'):
            if cmd[6:].strip().startswith('random'):
                player.play(poster.run('song')[9:-10])
            else:
                player.play(lastsong)
        elif cmd.startswith('/robo '):
            client.post(' '.join('-'.join(list(x))
                                 for x in cmd[6:].split(' ')))
        elif cmd.startswith('/room '):
            if not client.setchannel(cmd[6:]):
                print('WARNING: Could not switch channels because ' +
                         'the post queue is not empty.')
        elif cmd.startswith('/stop'):
            player.stop()
        elif cmd.startswith('/user'):
            print(', '.join([getuser(user) for user in list(client.users)]))
        elif cmd.startswith('/wolf'):
            tmp = poster.run(cmd[1:])
            if tmp.startswith('[img]'):
                client.post(tmp)
            else:
                print(tmp)
        else:
            client.post(cmd)
        cmd = input('  > ')
