JavaClient (Phoenix)
===================================

So, for a third time, I am restarting this project. However, I am doing it alone.

Watch this space.

RubyChat (awesome271828)
=====================

You need Ruby 2.0 or above on Linux to run it. It won't work on Windows out of the box, but contact me and I can help you get it set up.
`./main.rb` or `ruby main.rb` will launch it. A 32-bit pre-compiled package has been provided for your convenience.
Install it with `sudo dpkg -i ruby-2.0.0-p0-1_i386.deb`.

The window will auto-fit itself to your initial terminal size. If the terminal is too small, an error will be thrown.
After you've launched it, do not resize your terminal; bad things will happen.

Right now, the supported commands are
* Message filters:
    * /t message
    * /r message
    * /s message
    * /v message
    * /j message
* /wolf
  * /wolf count
  * /wolf previous
  * /wolf number
  * /wolf last (last auto-detected wolf)
* /logout
* /exit
* /mute user
* /unmute user

Typing `\n` anywhere will insert a newline, but typing `\\n` will simply display in chat as "\n".

`/wolf number` will accept non-positive arguments. /wolf 0 is the last wolf, /wolf -1 is the second to last, and so on.

More commands will be added over time.
