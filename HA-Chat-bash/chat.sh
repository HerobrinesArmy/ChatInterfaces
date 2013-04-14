#!/bin/bash -i
# Version 1.3.9
trap 'kill ${GETMESSAGES_PID} >/dev/null 2>&1; rm cookie >/dev/null 2>&1; rm LAST_JSON_INPUT >/dev/null 2>&1; exit 0;' INT QUIT
GLOBIGNORE="*"
VERSION="1.3.9"

# The postMessage function takes a single argument of the data you would like to post, and urlencodes and posts it
postMessage ()
{
    local ENCODED_MESSAGE=$( urlEncode "$1" )
    auth
    curl $PROXY -m 60 -L -b cookie -c cookie "http://herobrinesarmy.com/post_chat.php?c=${CHAT_ROOM}&o=1&m=${ENCODED_MESSAGE}" >/dev/null 2>&1
}

# The getMessages function is for getting messages. It replaces the getmessages.sh script.
getMessages ()
{
    LMID="0"
    while :
        do
            local JSON_INPUT=$( curl $PROXY -m 60 -s -L -b cookie -c cookie "http://herobrinesarmy.com/update_chat2.php?c=${1}&l=${LMID}&p=0" )
            if [ -n "$DUMP_JSON" ]
                then
                echo "$JSON_INPUT" > $DUMP_JSON
                break
            fi
            if [ -n "$JSON_INPUT" ]
                then
                LMID=$( echo $JSON_INPUT | sed 's/.*"lmid":"\([0-9]*\)",.*/\1/g' )
                echo "$JSON_INPUT" > LAST_JSON_INPUT
            fi
            local INCOMING_MESSAGE=$( echo $JSON_INPUT | sed s/'"users":.*'/''/ | sed 's/},/},\n/g' | sed 's/.*"[0-9]*":{"message_id":"[0-9]*","user_id":"[0-9]*","user":"\(<[^>]*>\)*\([^<]*\)<[^>]*>[^"]*","message":"\([^"]*\)".*/\2: \3/g' | sed '$d' | sed 's/&amp;/\&/g' | sed 's/&lt;/</g' | sed 's/&gt;/>/g' | sed 's/&quot;/"/g' | sed "s/\]\[/\] \[/g" | sed 's/\\\//\//g' | sed 's/\[img\]\([^\[]*\)\[\/img\]/\1 /Ig' | sed 's/\[youtube\]\([^\[]*\)\[\/youtube\]/\1 /Ig' | sed -e 's/\([^:]*[^:]\)/\\033\[1;34m\1\\033\[0m/1' | sed -e 's/\([^:]*[^:]\): \/me \?/\*\1 /1' | sed 's/\(.\)/\1\x00/g' )
            if [ -n "$INCOMING_MESSAGE" ]
                then
                    if [ "$LMID" != "$LMID_PREVIOUS" ]
                        then
                            echo -e "\r\033[K$INCOMING_MESSAGE"
                            LMID_PREVIOUS="$LMID"
                                if [ -n "$LOGFILE" ]
                                    then
                                        echo -e "$INCOMING_MESSAGE" >> $LOGFILE
                                fi
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
    IS_AUTHED=$( curl $PROXY -m 60 -s -b cookie -c cookie http://herobrinesarmy.com/amiauth )
    if [ "$IS_AUTHED" = "Nope." ]
        then
            if [ "$HAS_AUTHED" = "1" ]
                then
                    curl $PROXY -b cookie -c cookie -d "user=${USER}&pass=${PASS}" http://herobrinesarmy.com/auth.php
            else
                echo "Your session has expired, you will need to log in again."
                read -p "Enter your username: " USER
                read -s -p "Enter your password: " PASS
                echo
                curl $PROXY -b cookie -c cookie -d "user=${USER}&pass=${PASS}" http://herobrinesarmy.com/auth.php
                HAS_AUTHED="1"
            fi
    fi
}

# The following code is for processing arguments to the script
while getopts ":p:l:j:" OPTION
    do
        case $OPTION in
            p)
                PROXY="-x $OPTARG"
                ;;
            l)
                LOGFILE="$OPTARG"
                echo "==================NEW LOG SESSION STARTING===================" >> $LOGFILE
                ;;
            j)
                DUMP_JSON="$OPTARG"
                auth
                echo "The main chats are 8613406 (main chat) and 3 (science chat)."
                read -e -p "Enter the chat room number you wish to join: " -i "8613406" CHAT_ROOM
                getMessages $CHAT_ROOM
                exit 0
                ;;
            \?)
                echo "Invalid option: -$OPTARG"
                exit 1
                ;;
            :)
                echo "-$OPTARG requires an argument."
                exit 1
        esac
    done

PASTEBIN_DEV_KEY="172ab6f8293eaa46c3d527975b9a1813"

auth
echo "The main chats are 8613406 (main chat) and 3 (science chat)."
read -e -p "Enter the chat room number you wish to join: " -i "8613406" CHAT_ROOM

echo "" > LAST_JSON_INPUT
getMessages $CHAT_ROOM &
GETMESSAGES_PID=$!

WOLF_LINES=$( cat wolf.txt | wc -l )
while :
    do
        read -e MESSAGE
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
                        postMessage "[img]${WOLF_LINK}[/img]$WOLF_NUMBER" &
                fi
                if [[ "$WOLF_ARG1" =~ ^[0-9]+$ ]]
                    then
                        if [ $WOLF_ARG1 -le $WOLF_LINES ]
                            then
                                WOLF_NUMBER="$WOLF_ARG1"
                                WOLF_LINK=$( sed -n ${WOLF_NUMBER}p "wolf.txt" )
                                postMessage "[img]${WOLF_LINK}[/img]$WOLF_NUMBER" &
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
                    MUTE_ID=$( cat LAST_JSON_INPUT | sed "s/.*\"user_id\":\"\([0-9]*\)\",\"user\":\".*${MUTE_ARG1}.*\".*/\1/gI" )
                    curl $PROXY -s -b cookie -c cookie "http://herobrinesarmy.com/mute.php?o=1&m=${MUTE_ID}" >/dev/null 2>&1
                    else
                    echo "You need to enter your target."
                fi
                ;;
            /unmute*)
                MESSAGE_LENGTH=$( echo $MESSAGE | wc -w )
                if [ $MESSAGE_LENGTH -gt 1 ]
                    then
                    UNMUTE_ARG1=$( echo $MESSAGE | cut -d' ' -f2- )
                    UNMUTE_ID=$( cat LAST_JSON_INPUT | sed "s/.*\"user_id\":\"\([0-9]*\)\",\"user\":\".*${UNMUTE_ARG1}.*\".*/\1/gI" )
                    curl $PROXY -s -b cookie -c cookie "http://herobrinesarmy.com/mute.php?o=0&m=${UNMUTE_ID}" >/dev/null 2>&1
                    else
                    echo "You need to enter your target."
                fi
                ;;
            /profile*)
                MESSAGE_LENGTH=$( echo $MESSAGE | wc -w )
                if [ $MESSAGE_LENGTH -gt 1 ]
                    then
                    PROFILE_ARG1=$( echo $MESSAGE | cut -d' ' -f2- )
                    PROFILE=$( cat LAST_JSON_INPUT | sed "s/.*\"user_id\":\"\([0-9]*\)\",\"user\":\".*${PROFILE_ARG1}.*\".*/\1/gI")
                    echo "http://herobrinesarmy.enjin.com/profile/${PROFILE}"
                    else
                    echo "You must enter a name."
                fi
                ;;
            /users)
                USERS_ARG=$( cat LAST_JSON_INPUT | sed 's/.*"users":{\(.*\)/\1/g' | sed 's/},/},\n/g' | sed 's/.*"user":"\(<[^>]*>\)*\([^<]*\)<[^>]*>[^"]*".*/\2/g' )
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
                    PASTEBIN_LINK=$( curl $PROXY -s -d "api_dev_key=${PASTEBIN_DEV_KEY}" -d "api_option=paste" -d "api_paste_code=${PASTE_ENCODED}" "http://pastebin.com/api/api_post.php" )
                    postMessage "$PASTEBIN_LINK" &
                    else
                    PASTE_CONTENT=$( xclip -o -selection clip-board )
                    PASTE_ENCODED=$( urlEncode "$PASTE_CONTENT" )
                    PASTEBIN_LINK=$( curl $PROXY -s -d "api_dev_key=${PASTEBIN_DEV_KEY}" -d "api_option=paste" -d "api_paste_code=${PASTE_ENCODED}" "http://pastebin.com/api/api_post.php" )
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
                ping -c1 herobrinesarmy.com | grep rtt | cut -d'/' -f5
                ;;
            /ex*)
                kill $GETMESSAGES_PID >/dev/null 2>&1
                rm cookie >/dev/null 2>&1
                rm LAST_JSON_INPUT >/dev/null 2>&1
                exit 0
                ;;
            *)
                postMessage "$MESSAGE" &
                ;;
        esac
    done
