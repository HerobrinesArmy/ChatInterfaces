#!/bin/bash
if [[ "$MESSAGE" == /unmute* ]]
then
    COMMAND_GIVEN="1"
    MESSAGE_LENGTH=$( echo $MESSAGE | wc -w )
    if [ $MESSAGE_LENGTH -gt 1 ]
        then
        UNMUTE_ARG1=$( echo $MESSAGE | cut -d' ' -f2- )
        UNMUTE_ID=$( cat ${DIR}/session/tmp/${CHAT_ROOM}/LAST_JSON_INPUT | sed "s/.*\"user_id\":\"\([0-9]*\)\",\"user\":\".*${UNMUTE_ARG1}.*\".*/\1/gI" )
        curl $PROXY -s -b ${DIR}/session/cookie -c ${DIR}/session/cookie "http://herobrinesarmy.com/mute.php?o=0&m=${UNMUTE_ID}" >/dev/null 2>&1 &
        else
        echo "Enter your target next time."
    fi
fi
