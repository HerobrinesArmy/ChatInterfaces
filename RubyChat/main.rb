#!/usr/bin/env ruby
# By awesome271828.

begin

require File.dirname(__FILE__) + '/lib/network.rb'
require 'curses'
require 'thread'

MAIN_CHAT = 8613406
MEETING_ROOM = 3
USER_MATCH = /<a.+>(?<name>.+)<\/a>/
WOLF = 'https://raw.github.com/HerobrinesArmy/ChatInterfaces/master/HA-Chat-bash/wolf.txt'


%w[HUP INT QUIT TERM].each do |sig|
    trap(sig) do |signal|
        Curses.close_screen
        puts 'Bye.'
        exit(signal)
    end
end

def center_x(width)
    ($width - width) / 2
end

def center_y(height)
    ($height - height) / 2
end

def put_bold(win, text)    
    win.attrset(Curses::A_BOLD)
    win.addstr(text)
    win.attrset(Curses::A_NORMAL)
end

def cf(text) # canonical form
    text.rstrip.downcase
end

def inner_window(mwin, win)
    mwin.subwin(win.maxy - 2, win.maxx - 2, win.begy + 1, win.begx + 1)
end

def init
    Curses.init_screen
    Curses.nocbreak
    Curses.nl
    Curses.echo
    srand
    main_win = Curses.stdscr
    $width = main_win.maxx
    $height = main_win.maxy

    u_box = main_win.subwin(3, 42, center_y(3) - 2, center_x(42))
    u_box.box(0, 0)
    main_win.setpos(center_y(3) - 3, center_x('Username'.length))
    put_bold(main_win, 'Username')
    u_input = inner_window(main_win, u_box)
    p_box = main_win.subwin(3, 42, center_y(3) + 2, center_x(42))
    p_box.box(0, 0)
    main_win.setpos(center_y(3) + 5, center_x('Password'.length))
    put_bold(main_win, 'Password')
    p_input = inner_window(main_win, p_box)
    main_win.refresh

    user = u_input.getstr
    Curses.noecho
    pass = p_input.getstr
    Curses.echo
    Curses.close_screen
    [user, pass]
end

class ClientError < StandardError; end

def error(message)
    Curses.close_screen
    fail(ClientError, message)
end

def extract_username(text)
    usr = text.match(USER_MATCH)
    error('Invalid server data received!') unless usr
    usr[:name]
end

info = init
puts 'Logging in...'
cookie = ChatInterfaces::Network.login(*info)
if ChatInterfaces::Network.check_auth(cookie)
    puts 'Logged in.'
else
    puts 'Login failed!'
    exit(2)
end

Curses.init_screen
main_win = Curses.stdscr
main_win.addstr('Room to join (c, m, or room #): ')
main_win.refresh

case op = cf(main_win.getstr)
when 'c'
    r = MAIN_CHAT
when 'm'
    r = MEETING_ROOM
else
    r = (op.to_i.zero? ? MAIN_CHAT : op.to_i)
end

main_win.clear
user_box = main_win.subwin($height - 1, 22, 0, $width - 22)
user_box.box(0, 0)
user_display = inner_window(main_win, user_box)
user_display.scrollok(true)
chat = main_win.subwin($height - 1, $width - 23, 0, 0)
chat.box(0, 0)
chat_display = inner_window(main_win, chat)
chat_display.scrollok(true)
main_win.refresh

def parse(output, msg)
    op = false
    case msg[1]
    when 'Inception horn'
        Curses.flash
        op = true
    else
        op = true
    end
    if op
        put_bold(output, "#{msg[0]}: ")
        output.addstr("#{msg[1]}\n")
    end
end

update = Thread.new do
    users = {}
    messages = []
    lmid = 0
    loop do
        m = ChatInterfaces::Network.get_messages(r, cookie, lmid)
        error('Failed to download messages!') unless m
        m['users'].each_value { |val| users[val['user_id']] = extract_username(val['user']) }
        m['messages'].each_value { |val| messages << [extract_username(val['user']), val['message']] } if m['messages']
        if lmid.zero?
            messages.each do |msg|
                put_bold(chat_display, "#{msg[0]}: ")
                chat_display.addstr("#{msg[1]}\n")
            end
        else
            messages.each { |msg| parse(chat_display, msg) }
        end
        chat_display.refresh
        user_display.setpos(user_display.begy, user_display.begx)
        user_display.clear
        users.each_value { |val| user_display.addstr("#{val}\n") }
        user_display.refresh
        messages = []
        lmid = m['lmid'].to_i
        sleep(1)
    end
end

def process(msg, room, cookie)
    case cf(msg)
    when '/wolf'
        $wolflist ||= Net::HTTP.get_response(URI(WOLF)).body.split("\n")
        msg = "[img]#{$wolflist[Random.rand($wolflist.size)]}[/img]"
    when '/logout'
        ChatInterfaces::Network.logout(cookie)
        Curses.close_screen
        puts 'Logged out.'
        exit
    end
    ChatInterfaces::Network.send_message(msg, room, cookie) unless msg.empty?
end

loop do
    main_win.setpos($height - 1, 0)
    main_win.clrtoeol
    process(main_win.getstr, r, cookie)
end

rescue => e
    $stderr.puts "Error: #{e.class}: #{e.message}"
    Curses.close_screen
    exit(1)
end