#!/bin/bash -i
# Version 1.1.0
trap 'echo "Use the /exit or /logout command instead"' INT QUIT

VERSION="1.1.0"

# The postMessage function takes a single argument of the data you would like to post, and urlencodes and posts it
postMessage ()
{
    local ENCODED_MESSAGE=$( urlEncode "$1" )
    curl -m 60 -L -b cookie -c cookie "http://herobrinesarmy.com/post_chat.php?c=${CHAT_ROOM}&o=1&m=${ENCODED_MESSAGE}" >/dev/null 2>&1
}

# The getMessages function is for getting messages. It replaces the getmessages.sh script.
getMessages ()
{
    LMID="0"
    while :
        do
            local JSON_INPUT=$( curl -m 40 -s -L -b cookie -c cookie "http://herobrinesarmy.com/update_chat2.php?c=${1}&l=${LMID}&p=0" )
            if [ -n "$JSON_INPUT" ]
                then
                LMID=$( echo $JSON_INPUT | sed "s/,/\\`echo -e '\n\r'`/g" | grep '"lmid":"' | cut -d '"' -f 4 )
            fi
            local INCOMING_MESSAGE=$( echo $JSON_INPUT | sed s/'"users":.*'/''/ | sed 's/,\"/\\\r\n\"/g' | sed s'/..$//' | grep '"user":\|"message":' | cut -d '"' -f4- | sed 's/\\\//\//g'  | sed 's/\(.*\)./\1/' | sed 's/<[^>]\+>//g' | sed "s/\]\[/\] \[/g" | sed "s/\[[^]]*\]//g" | awk '{ if ( ( NR % 2 ) == 0 ) { printf("%s\n",$0) } else { printf("%s: ",$0) } }' | sed 's/&amp;/\&/g' | sed 's/&lt;/</g' | sed 's/&gt;/>/g' | sed 's/&quot;/"/g' |  sed -e 's/\([^:]*[^:]\)/\\033\[1;34m\1\\033\[0m/1' | sed -e 's/\([^:]*[^:]\): \/me/\*\1/1' )
            if [ -n "$INCOMING_MESSAGE" ]
                then
                    if [ "$LMID" != "$LMID_PREVIOUS" ]
                        then
                            echo -e "\r\033[K$INCOMING_MESSAGE"
                            LMID_PREVIOUS="$LMID"
            fi
            fi
        done
}

# The urlEncode function properly url encodes messages
urlEncode ()
{
    local ENCODED_MESSAGE=$( echo -ne "$1" | xxd -plain | tr -d '\n' | sed 's/\(..\)/%\1/g' | tr '[:lower:]' '[:upper:]'  | sed -f urlencode.sed )
    echo -ne "$ENCODED_MESSAGE"
}

# The auth function checks if you are authed, and if you are not, it authenticates you with the server
auth ()
{
    IS_AUTHED=$( curl -s -b cookie -c cookie http://herobrinesarmy.com/amiauth )
    if [ "$IS_AUTHED" = "Nope." ]
        then
            if [ "$HAS_AUTHED" = "1" ]
                then
                    curl -b cookie -c cookie -d "user=${USER}&pass=${PASS}" http://herobrinesarmy.com/auth.php
            else
                echo "Your session has expired, you will need to log in again."
                read -p "Enter your username: " USER
                read -s -p "Enter your password: " PASS
                echo
                curl -b cookie -c cookie -d "user=${USER}&pass=${PASS}" http://herobrinesarmy.com/auth.php
                HAS_AUTHED="1"
            fi
    fi
}

PASTEBIN_DEV_KEY="172ab6f8293eaa46c3d527975b9a1813"

auth
echo "The main chats are 8613406 (main chat) and 3 (science chat)."
read -e -p "Enter the chat room number you wish to join: " -i "8613406" CHAT_ROOM

getMessages $CHAT_ROOM &
GETMESSAGES_PID=$!

WOLF_LINES=$( cat wolf.txt | wc -l )
while :
    do
        read -e MESSAGE
        auth
        case "$MESSAGE" in
            /wolf*)
                MESSAGE_LENGTH=$( echo $MESSAGE | wc -w )
                if [ $MESSAGE_LENGTH -gt 1 ]
                    then
                        WOLF_ARG1=$( echo ${MESSAGE} | cut -d' ' -f2 )
                fi
                
                if [ -z "$WOLF_ARG1" ]
                    then
                        WOLF_NUMBER=$[ ( $RANDOM % $WOLF_LINES ) + 1 ]
                        WOLF_LINK=$( sed -n ${WOLF_NUMBER}p "wolf.txt" )
                        postMessage "[img]${WOLF_LINK}[/img]" &
                fi
                if [[ "$WOLF_ARG1" =~ ^[0-9]+$ ]]
                    then
                        if [ $WOLF_ARG1 -le $WOLF_LINES ]
                            then
                                WOLF_NUMBER="$WOLF_ARG1"
                                WOLF_LINK=$( sed -n ${WOLF_NUMBER}p "wolf.txt" )
                                postMessage "[img]${WOLF_LINK}[/img]" &
                        fi
                        if [ "$WOLF_ARG1" -gt "$WOLF_LINES" ]
                            then
                                echo "The number you have entered is too large, the max number allowed is ${WOLF_LINES}."
                        fi
                fi
                if [ "$WOLF_ARG1" = "count" ]
                    then
                        echo "There are currently $WOLF_LINES wolves available."
                fi
                if [ "$WOLF_ARG1" = "previous" ]
                    then
                        echo "The number of the last wolf posted is ${WOLF_NUMBER}."
                fi
                WOLF_ARG1=""
                ;;
            /mute*)
                MESSAGE_LENGTH=$( echo $MESSAGE | wc -w )
                if [ $MESSAGE_LENGTH -gt 1 ]
                    then
                    MUTE_ARG1=$( echo $MESSAGE | cut -d' ' -f2- )
                    MUTE_ID=$( curl -s -L -b cookie -c cookie "http://herobrinesarmy.com/update_chat2.php?c=${CHAT_ROOM}&l=0" | sed 's/^.\(.*\).$/\1/' | sed "s/,/\\`echo -e '\n\r'`/g"| sed "s/:{/\\`echo -e '\n\r'`/g" | grep '"user":\|"user_id":' | cut -d "{" -f 2 | cut -d "}" -f 2 | sed 's/<[^>]\+>//g' | cut -d '"' -f 4 | sed '/^$/d' | awk '!_[$0]++' | sed '$!N;s/\n/ /' | grep -i "$MUTE_ARG1" | cut -f 1 -d ' ' )
                    curl -s -b cookie -c cookie "http://herobrinesarmy.com/mute.php?o=1&m=${MUTE_ID}" >/dev/null 2>&1
                    else
                    echo "You need to enter your target."
                fi
                ;;
            /unmute*)
                MESSAGE_LENGTH=$( echo $MESSAGE | wc -w )
                if [ $MESSAGE_LENGTH -gt 1 ]
                    then
                    UNMUTE_ARG1=$( echo $MESSAGE | cut -d' ' -f2- )
                    UNMUTE_ID=$( curl -s -L -b cookie -c cookie "http://herobrinesarmy.com/update_chat2.php?c=${CHAT_ROOM}&l=0" | sed 's/^.\(.*\).$/\1/' | sed "s/,/\\`echo -e '\n\r'`/g"| sed "s/:{/\\`echo -e '\n\r'`/g" | grep '"user":\|"user_id":' | cut -d "{" -f 2 | cut -d "}" -f 2 | sed 's/<[^>]\+>//g' | cut -d '"' -f 4 | sed '/^$/d' | awk '!_[$0]++' | sed '$!N;s/\n/ /' | grep -i "$MUTE_ARG1" | cut -f 1 -d ' ' )
                    curl -s -b cookie -c cookie "http://herobrinesarmy.com/mute.php?o=0&m=${UNMUTE_ID}" >/dev/null 2>&1
                    else
                    echo "You need to enter your target."
                fi
                ;;
            /profile*)
                MESSAGE_LENGTH=$( echo $MESSAGE | wc -w )
                if [ $MESSAGE_LENGTH -gt 1 ]
                    then
                    PROFILE_ARG1=$( echo $MESSAGE | cut -d' ' -f2- )
                    PROFILE=$( curl -s -L -b cookie -c cookie "http://herobrinesarmy.com/update_chat2.php?c=${CHAT_ROOM}&l=0" | sed 's/^.\(.*\).$/\1/' | sed "s/,/\\`echo -e '\n\r'`/g"| sed "s/:{/\\`echo -e '\n\r'`/g" | grep '"user":\|"user_id":' | cut -d "{" -f 2 | cut -d "}" -f 2 | sed 's/<[^>]\+>//g' | cut -d '"' -f 4 | sed '/^$/d' | awk '!_[$0]++' | sed '$!N;s/\n/ /' | grep -i "$PROFILE_ARG1" | cut -f 1 -d ' ' )
                    echo "http://herobrinesarmy.enjin.com/profile/${PROFILE}"
                    else
                    echo "You must enter a name."
                fi
                ;;
            /users)
                USERS_ARG=$( curl -s -b cookie -c cookie "http://herobrinesarmy.com/update_chat2.php?c=${CHAT_ROOM}&l=0" | sed 's/^.\(.*\).$/\1/' | sed s/'.*"users":'/''/ | sed "s/,/\\`echo -e '\n\r'`/g"| sed "s/:{/\\`echo -e '\n\r'`/g" | grep '"user":' | cut -d "{" -f 2 | cut -d "}" -f 2 | sed 's/<[^>]\+>//g' | cut -d '"' -f 4 | sed '/^$/d' )
                echo -e "\033[1;31m${USERS_ARG}\033[0m"
                ;;
            /hug*)
                MESSAGE_LENGTH=$( echo $MESSAGE | wc -w )
                if [ $MESSAGE_LENGTH -gt 1 ]
                    then
                    HUG_ARG1=$( echo $MESSAGE | cut -d' ' -f2- )
                    if [ "$HUG_ARG1" = "all" ]
                        then
                        postMessage "@[you] :hug:" &
                        else
                        postMessage "@${HUG_ARG1} :hug:" &
                    fi
                    else
                    echo "You must enter something."
                fi
                ;;
            /paste)
                PASTE_CONTENT=$( xclip -o -selection clip-board )
                postMessage "[code]${PASTE_CONTENT}[/code]" &
                ;;
            /pastebin*)
                MESSAGE_LENGTH=$( echo $MESSAGE | wc -w )
                if [ $MESSAGE_LENGTH -gt 1 ]
                    then
                    PASTE_FILENAME=$( echo $MESSAGE | cut -d' ' -f2- )
                    PASTE_CONTENT=$( cat "$PASTE_FILENAME" )
                    PASTE_ENCODED=$( urlEncode "$PASTE_CONTENT" )
                    PASTEBIN_LINK=$( curl -s -d "api_dev_key=${PASTEBIN_DEV_KEY}" -d "api_option=paste" -d "api_paste_code=${PASTE_ENCODED}" "http://pastebin.com/api/api_post.php" )
                    postMessage "$PASTEBIN_LINK"
                    else
                    PASTE_CONTENT=$( xclip -o -selection clip-board )
                    PASTE_ENCODED=$( urlEncode "$PASTE_CONTENT" )
                    PASTEBIN_LINK=$( curl -s -d "api_dev_key=${PASTEBIN_DEV_KEY}" -d "api_option=paste" -d "api_paste_code=${PASTE_ENCODED}" "http://pastebin.com/api/api_post.php" )
                    postMessage "$PASTEBIN_LINK" &
                fi
                ;;
            /cmd*)
                MESSAGE_LENGTH=$( echo $MESSAGE | wc -w )
                if [ $MESSAGE_LENGTH -gt 1 ]
                    then
                    CMD_ARG1=$( echo $MESSAGE | cut -d' ' -f2- )
                    eval $CMD_ARG1
                    else
                        echo "You realize that you have to enter a command, right?"
                fi
                ;;
            /no)
                postMessage "[img]http://i.imgur.com/AkGfK4Z.jpg[/img]" &
                ;;
            /version)
                echo "$VERSION"
                ;;
            /ping)
                ping -c1 herobrinesarmy.com > /dev/null && echo "PONG!" || echo "FAILURE!"
                ;;
            /logout)
                kill $GETMESSAGES_PID
                rm cookie
                exit 0
                ;;
            /ex*)
                kill $GETMESSAGES_PID
                exit 0
                ;;
            *)
                postMessage "$MESSAGE" &
                ;;
        esac
    done
