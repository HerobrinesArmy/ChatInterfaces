#!/bin/bash
DIR="${BASH_SOURCE%/*}"
if [[ ! -d "$DIR" ]]
    then DIR="$PWD"
fi
GLOBIGNORE="*"
CHAT_ROOM="${1}"
# Including things
. ${DIR}/lib/urlEncode.sh
. ${DIR}/lib/postMessage.sh
. ${DIR}/lib/postList.sh
. ${DIR}/lib/killChat.sh

mkdir -p "${DIR}/bots/"

LMID="0"
while :
    do
        JSON_INPUT=$( curl $PROXY -m 60 -s -L -b ${DIR}/session/cookie -c ${DIR}/session/cookie "http://herobrinesarmy.com/update_chat2.php?c=${1}&l=${LMID}&p=0" 2>/dev/null )
        if [ -n "$JSON_INPUT" ]
            then
            LMID=$( echo $JSON_INPUT | sed 's/.*"lmid":"\([0-9]*\)",.*/\1/g' )
            echo "$JSON_INPUT" > ${DIR}/session/tmp/${CHAT_ROOM}/LAST_JSON_INPUT
        fi
        INCOMING_MESSAGE=$( echo $JSON_INPUT | sed s/'"users":.*'/''/ | sed 's/},/},\n/g' | sed 's/.*"[0-9]*":{"message_id":"[0-9]*","user_id":"[0-9]*","user":"\(<[^>]*>\)*\([^<]*\)<[^>]*>[^"]*","message":"\([^"]*\)".*/\2: \3/g' | sed '$d' | sed 's/&amp;/\&/g' | sed 's/&lt;/</g' | sed 's/&gt;/>/g' | sed 's/&quot;/"/g' | sed "s/\]\[/\] \[/g" | sed 's/\\\//\//g' | sed 's/\[img\]\([^\[]*\)\[\/img\]/\1 /Ig' | sed 's/\[youtube\]\([^\[]*\)\[\/youtube\]/\1 /Ig' | sed -e 's/\([^:]*[^:]\)/\\033\[1;34m\1\\033\[0m/1' | sed -e 's/\([^:]*[^:]\): \/me \?/\*\1 /1' | sed 's/\(.\)/\1\x00/g' | sed 's/`//g' )
        if [ -n "$INCOMING_MESSAGE" ]
            then
                if [ "$LMID" != "$LMID_PREVIOUS" ]
                    then
                        echo -e "\r\033[K$INCOMING_MESSAGE"
                        (
                        GLOBIGNORE=""
                        for f in ${DIR}/bots/*; do
                            . $f
                        done
                        ) &
                        LMID_PREVIOUS="$LMID"
                fi
        fi
    done
