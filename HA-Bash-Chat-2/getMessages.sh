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
        if ! [[ "$JSON_INPUT" =~ "Could not connect.*" ]] && [[ "$JSON_INPUT" =~ .*\"messages\":.* ]]
            then
            if [ -n "$JSON_INPUT" ]
                then
                LMID=$( echo $JSON_INPUT | sed 's/.*"lmid":"\([0-9]*\)",.*/\1/g' )
                echo "$JSON_INPUT" > ${DIR}/session/tmp/${CHAT_ROOM}/LAST_JSON_INPUT
            	preparse=$( echo "$JSON_INPUT" | grep -Po '"[0-9]*":{"message_id":.*?},' | sed 's/&amp;/\&/g' | sed 's/&lt;/</g' | sed 's/&gt;/>/g' | sed 's/&quot;/"/g' | sed "s/\]\[/\] \[/g" | sed 's/\\\//\//g' | sed 's/\[img\]\([^\[]*\)\[\/img\]/\1 /Ig' | sed 's/\[youtube\]\([^\[]*\)\[\/youtube\]/\1 /Ig' )
            	readarray array1 <<< "$preparse"
            fi


            i=0
            while [ $i -lt ${#array1[@]} ]
                do  
                    USERNAME=$( echo "${array1[${i}]}" | sed 's/.*"user":"\(<[^>]*>\)*\([^<]*\)<[^>]*>[^"]*".*/\2/g' )
                    INCOMING_MESSAGE=$( echo "${array1[${i}]}" | sed 's/.*"message":"\(.*\)",.*/\1/g' )
                    MESSAGE_TIME=$( echo "${array1[${i}]}" | sed 's/.*"time":"\(.*\)".*/\1/g' )
                    MESSAGE_ID=$( echo "${array1[${i}]}" | sed 's/"\([0-9]*\)".*/\1/g' )
                    if [ -n "$INCOMING_MESSAGE" ] && [ "$LMID" -ge "$MESSAGE_ID" ]
                        then
                            (
                            if [ "$FIRSTRUN" == "false" ]
                                then
                                    GLOBIGNORE=""
                                    for f in ${DIR}/bots/*; do
                                        . $f
                                    done
                            fi
                            ) &
                            if [[ "$INCOMING_MESSAGE" == /me* ]]
                                then
                                    USERNAME_CHANGED=$( echo -e "\033[1;34m${USERNAME}\033[0m" )
                                    OUTPUT=$( echo "$INCOMING_MESSAGE" | sed -e "s/\/me/\*${USERNAME_CHANGED}/1" )
                                    echo -en "\033[0;32m[${MESSAGE_TIME}]\033[0m"; echo "$OUTPUT"
                                else
                                    OUTPUT=$( echo -en "\033[0;32m[${MESSAGE_TIME}]\033[0m"; echo -en "\033[1;34m${USERNAME}\033[0m: "; echo "${INCOMING_MESSAGE}" )
                                    echo "$OUTPUT"
                            fi
                            ((i++))
                    fi
                done
        fi
        FIRSTRUN="false"
    done
