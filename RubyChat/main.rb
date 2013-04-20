#!/usr/bin/env ruby
# By awesome271828.

begin

require_relative './lib/network.rb'
require 'curses'
require 'thread'
require 'cgi'

LIB = File.dirname(File.expand_path(__FILE__)) + '/lib/'
MAIN_CHAT = 8613406
MEETING_ROOM = 3
USER_MATCH = /<a.+tag-(?<tag>\d+)'>(?<name>.+)<\/a>/
WOLF = 'https://raw.github.com/HerobrinesArmy/ChatInterfaces/master/HA-Chat-bash/wolf.txt'
SONGS = {
    'Tiger' => 'http://www.youtube.com/watch?v=btPJPFnesV4',
    'Rave' => 'http://www.youtube.com/watch?v=w8kLkMgdzy0',
    'Dark Knight theme' => 'http://www.youtube.com/watch?v=Z_DSq-LhOyU',
    'Epic' => 'http://www.youtube.com/watch?v=k-2IT8rcdj0',
    'Turtle' => 'http://www.youtube.com/watch?v=Kr4yHEVc0eU'
        }
RANK_CSS = {
            '228373' => Curses.color_pair(Curses::COLOR_CYAN),
            '228361' => Curses.color_pair(Curses::COLOR_BLUE),
            '228640' => Curses.color_pair(Curses::COLOR_MAGENTA),
            '228677' => Curses.color_pair(Curses::COLOR_RED),
            '232043' => Curses.color_pair(Curses::COLOR_GREEN),
            '271886' => Curses.color_pair(Curses::COLOR_WHITE),
            '228080' => Curses.color_pair(Curses::COLOR_YELLOW),
            '241995' => Curses.color_pair(Curses::COLOR_BLUE),
            '223534' => Curses.color_pair(Curses::COLOR_RED),
            '000000' => Curses.color_pair(Curses::COLOR_WHITE)
           }

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

def put_bold(win, text, attrs = 0)
    win.attron(Curses::A_BOLD | attrs) { win.addstr(text) }
end

def cf(text) # canonical form
    text.rstrip.downcase
end

def rc(text, command) # remove command
    text.sub(/\A\/#{command} /i, '')
end

def inner_window(mwin, win)
    mwin.subwin(win.maxy - 2, win.maxx - 2, win.begy + 1, win.begx + 1)
end

def init
    Curses.init_screen
    if Curses.has_colors?
        Curses.start_color
        Curses.colors.times do |c|
            Curses.init_pair(c, c, Curses::COLOR_BLACK)
        end
        if Curses.colors >= 18 && Curses.can_change_color?
            Curses.init_color(Curses::COLOR_CYAN, 831, 416, 0)
            Curses.init_color(Curses::COLOR_CYAN + 8, 831, 416, 0)
            Curses.init_color(Curses::COLOR_BLUE, 220, 533, 1000)
            Curses.init_color(Curses::COLOR_BLUE + 8, 220, 533, 1000)
            Curses.init_color(Curses::COLOR_MAGENTA, 510, 196, 612)
            Curses.init_color(Curses::COLOR_MAGENTA + 8, 510, 196, 612)
            Curses.init_color(16, 867, 141, 137)
            RANK_CSS['228677'] = Curses.color_pair(16)
            Curses.init_color(Curses::COLOR_GREEN, 0, 459, 0)
            Curses.init_color(Curses::COLOR_GREEN + 8, 0, 459, 0)
            Curses.init_color(17, 651, 651, 651)
            RANK_CSS['271886'] = Curses.color_pair(17)
            Curses.init_color(Curses::COLOR_YELLOW, 1000, 1000, 0)
            Curses.init_color(Curses::COLOR_YELLOW + 8, 1000, 1000, 0)
            Curses.init_color(Curses::COLOR_RED, 820, 149, 149)
            Curses.init_color(Curses::COLOR_RED + 8, 820, 149, 149)
        end
    end
    Curses.nocbreak
    Curses.nl
    Curses.echo
    Thread.abort_on_exception = true
    srand
    main_win = Curses.stdscr
    $width = main_win.maxx
    $height = main_win.maxy
    $users = {}
    $muted = Hash.new(false)
    $user_access = Mutex.new
    $message_queue = Queue.new
    $status_queue = Queue.new
    $draw_queue = Queue.new
    $wolflist = Net::HTTP.get_response(URI(WOLF)).body.split("\n")
    $music_playing = nil

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

def draw(&job)
    $draw_queue << job
end

def play_sound(name)
    if $music_playing.nil?
        $music_playing = name
        Thread.new do
            %x[vlc --play-and-exit --sout '#display{novideo=true}' -I dummy #{name} 2>/dev/null &]
            sleep(5)
            $music_playing = nil
        end
    end
end

def error(message)
    Curses.close_screen
    fail(ClientError, message)
end

def extract_username(text)
    usr = text.match(USER_MATCH)
    error('Invalid server data received!') unless usr
    [usr[:name], usr[:tag]]
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
user_box = main_win.subwin($height - 2, 22, 1, $width - 22)
user_box.box(0, 0)
user_display = inner_window(main_win, user_box)
user_display.scrollok(true)
chat = main_win.subwin($height - 2, $width - 23, 1, 0)
chat.box(0, 0)
chat_display = inner_window(main_win, chat)
chat_display.scrollok(true)
status = main_win.subwin(1, $width, 0, 0)
status.attron(Curses::A_BOLD)
main_win.refresh

def rot13(str)
    str.chars.map do |c|
        if c.between?(?A, ?z)
            b = c.between?(?A, ?Z) ? ?A.ord : ?a.ord
            n = (c.ord - b + 13).%(26) + b 
            n.chr
        else
            c
        end
    end.join('')
end

def parse(output, msg, first_time)
    op = false
    msg[1] = CGI.unescapeHTML(msg[1])
    msg[1] = msg[1].gsub(/\[(?:img|youtube|url)\]/i, '').gsub(/\[\/(?:img|youtube|url)\]/i, ' ')
    if msg[1] == 'Inception horn' && !first_time
        Thread.new { `cvlc --play-and-exit -I dummy http://inception.davepedu.com/inception.mp3 2>/dev/null &` }
        op = true
    elsif SONGS.has_key?(msg[1]) && !first_time
        play_sound(SONGS[msg[1]])
        op = true
    elsif msg[1].start_with?('/me')
        put_bold(output, "*#{msg[0].first} ", RANK_CSS[msg[0].last])
        output.addstr("#{msg[1].sub(/\/me ?/, '')}\n")
    elsif msg[1].start_with?('r: ')
        msg[1] = rot13(msg[1].sub(/\Ar: /, ''))
        msg[0][1] = '000000'
        parse(output, msg, first_time)
    else
        op = true
    end
    if op
        unless first_time || $wolflist.nil?
            $wolflist.each_with_index do |w, i|
                if msg[1].include?(w)
                    $status_queue << "Wolf ##{i.succ} detected."
                    $auto_wolf = i
                end
            end
        end
        put_bold(output, "#{msg[0].first}: ", RANK_CSS[msg[0].last])
        output.addstr("#{msg[1]}\n")
    end
end

Thread.new do
    messages = []
    old_users = {}
    tmp = nil
    lmid = 0
    lid = 0
    loop do
        m = ChatInterfaces::Network.get_messages(r, cookie, lmid)
        error('Failed to download messages!') unless m
        $user_access.synchronize do
            old_users = $users
            $users = {}
            messages = []
            m['users'].each_value do |val|
                id = val['user_id']
                name = extract_username(val['user'])
                $users[id] = name
                $status_queue << "#{name.first} has joined." unless old_users.has_key?(id) || old_users.empty?
            end
            old_users.each { |k, v| $status_queue << "#{v.first} has left." unless $users.has_key?(k) }
            if tmp = m['messages']
                tmp = tmp.sort
                tmp.each { |info| messages << [extract_username(info.last['user']), info.last['message']] unless info.first.to_i <= lid }
            end
            first_time = lmid.zero?
            draw do
                messages.each { |msg| parse(chat_display, msg, first_time) unless $muted[msg[0].first] }
                chat_display.refresh
                user_display.setpos(user_display.begy, user_display.begx)
                user_display.clear
                u = $users.to_a.sort do |a, b|
                    if a[1].last > b[1].last
                        1
                    elsif a[1].last < b[1].last
                        -1
                    else
                        a[1].first.downcase <=> b[1].first.downcase
                    end
                end
                u.each do |val|
                    user_display.attron(RANK_CSS[val[1].last] | ($muted[val[1].first] ? Curses::A_STANDOUT : 0)) { user_display.addstr("#{val[1].first}\n") }
                end
                user_display.refresh
            end
        end
        lmid = m['lmid'].to_i
        lid = [tmp.last.first.to_i, lid].max if tmp
        sleep(1)
    end
end

def filter(name, msg)
    IO.popen(LIB + name, 'r+') do |filter|
        filter.puts msg
        filter.close_write
        filter.gets.chomp
    end
end

def process(msg, room, cookie, output)
    c = cf(msg)
    m = msg.downcase
    if c.start_with?('/wolf')
        c = c.split(' ')[1]
        case c
        when nil
           $prev_wolf = Random.rand($wolflist.size)
           msg = "[img]#{$wolflist[$prev_wolf]}[/img]#{$prev_wolf.succ}"
        when 'count'
            $status_queue << "There are #{$wolflist.size} wolves loaded."
            msg = ''
        when 'previous'
            $status_queue << "The last wolf picked was #{$prev_wolf.nil? ? '[none]' : $prev_wolf.succ}."
            msg = ''
        when 'last'
            $status_queue << "The last detected wolf was #{$auto_wolf.nil? ? '[none]' : $auto_wolf.succ}."
            msg = ''
        else
            if a = $wolflist[c.to_i.pred]
                msg = "[img]#{a}[/img]#{(c.to_i.pred % $wolflist.size).succ}"
                $prev_wolf = c.to_i.pred % $wolflist.size
            else
                $status_queue << "Invalid command or wolf number entered."
                msg = ''
            end
        end
    elsif c == '/logout'
        ChatInterfaces::Network.logout(cookie)
        Curses.close_screen
        puts 'Logged out.'
        exit
    elsif c == '/exit'
        Curses.close_screen
        puts 'Bye!'
        exit
    elsif m.start_with?('/s ')
        msg = filter('chef', rc(msg, 's')) + (Random.rand(10).zero? ? ' Bork Bork Bork!' : '')
    elsif m.start_with?('/j ')
        msg = filter('jive', rc(msg, 'j'))
    elsif m.start_with?('/v ')
        msg = filter('valspeak', rc(msg, 'v'))
    elsif m.start_with?('/t ')
        msg = rc(msg, 't').downcase.gsub(/[^a-z\d]+/, '').prepend('#')
    elsif m.start_with?('/profile ')
        msg = rc(msg, 'profile')
        id = ''
        $users.each do |k, v|
            if msg == v[0]
                id = k
                break
            end
        end
        if id.empty?
            $status_queue << "User \"#{msg}\" not found."
        else
            op = "#{msg}: http://herobrinesarmy.enjin.com/profile/#{id}\n"
            draw do
                output.addstr(op)
                output.refresh
            end
        end
        msg = ''
    elsif m.start_with?('/mute ')
        msg = rc(msg, 'mute')
        $user_access.synchronize do
            if $muted[msg]
                $status_queue << "User #{msg} has already been muted."
            elsif $users.values.map(&:first).include?(msg)
                $muted[msg] = true
                $status_queue << "#{msg} has been muted."
            else
                $status_queue << "User \"#{msg}\" not found."
            end
        end
        msg = ''
    elsif m.start_with?('/unmute ')
        msg = rc(msg, 'unmute')
        $user_access.synchronize do
            if !$muted[msg]
                 $status_queue << "User #{msg} is not muted."
            elsif $users.values.map(&:first).include?(msg)
                $muted[msg] = false
                $status_queue << "#{msg} has been unmuted."
            else
                $status_queue << "User \"#{msg}\" not found."
            end
        end
        msg = ''
    elsif c.start_with?('/r ')
        msg = "r: #{rot13(rc(msg, 'r'))}"
    end
    unless msg.empty?
        msg = msg.gsub(/(?<!\\)\\n/, "\n").gsub(/\\\\n/, '\n')
        ChatInterfaces::Network.send_message(msg, room, cookie)
    end
end

Thread.new do
    loop do
        process(*($message_queue.pop))
        sleep(2.5)
    end
end

Thread.new do
    loop do
        m = $status_queue.pop
        draw do
            status.addstr(m)
            status.refresh
        end
        sleep(3)
        draw do
            status.setpos(0, 0)
            status.clear
            status.refresh
        end
    end
end

Thread.new { loop { $draw_queue.pop.call } }

ChatInterfaces::Network.init
main_win.timeout = 0 
Curses.noecho
Curses.cbreak
Curses.nonl

loop do
    draw do
        main_win.setpos($height - 1, 0)
        main_win.clrtoeol
    end
    msg = ''
    ptr = 0
    c = ''
    while c != "\r" do
        case c
        when '' 
        when 127.chr
            ptr = [ptr.pred, 0].max
            msg[ptr] = ''
            draw do
                main_win.setpos(main_win.cury, [main_win.curx.pred, 0].max)
                main_win.delch
            end
        else
            backup = msg[ptr]
            msg[ptr] = c
            ptr += 1
            draw do
                begin
                    main_win.addch(c.ord)
                rescue => e
                    msg[ptr] = backup
                    ptr -= 1
                end
            end
        end
        draw do
            a = main_win.getch
            begin
                c = a.nil? ? '' : a.chr
            rescue => e
                c = ''
            end
        end
        sleep(0.01)
    end
    $message_queue << [msg, r, cookie, chat_display]
end

rescue => e
    Curses.close_screen
    $stderr.puts "Error: #{e.class}: #{e.message}"
    exit(1)
end
