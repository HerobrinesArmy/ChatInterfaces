Phoenix's Java Client
=====================

You will find the java source code for both the CLI client and GUI client here. I have also included the jar executables in /bin. Since the GUI was my ultimate goal, I will not be supporting the CLI client anymore. You are welcome to fork it, edit it, do what you want with it! However, I would like to focus on the GUI, and thusly, will not be answering anymore questions to do with it.

You can now run the GUI like any other application!
Commands:
* "/mute <username>" to mute a user (unmute coming :P)
* "/wolf" to post a random wolf (ability to select wolf coming)

## v1.3
* Client and it's dialogs are now centered
* Cleaned up menu
* You can now call the authentication dialog from the menu
* Quit now quits
* Ability to mute users with "/mute"
* Wolves!

## v1.2
* Support for the "/me" command
* User list now functions correctly, with offline users being removed
* If the application looses focus, a row of hyphens is appended to the chat area to notify you where you were last

## v1.1
* New colour scheme for easier reading
* User list bar on the right is populated with users in chat (however if a user 'leaves' they will remain there)

## v1.0
* Receives chat message and is able to post to chat
* Requires to be run with a terminal passing username and password as arguments

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
