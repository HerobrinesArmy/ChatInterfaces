#!/bin/bash
if [[ "$MESSAGE" == /mute* ]]
then
    COMMAND_GIVEN="1"
    MESSAGE_LENGTH=$( echo $MESSAGE | wc -w )
    if [ $MESSAGE_LENGTH -gt 1 ]
        then
        MUTE_ARG1=$( echo $MESSAGE | cut -d' ' -f2- )
        MUTE_ID=$( cat ${DIR}/session/tmp/${CHAT_ROOM}/LAST_JSON_INPUT | sed "s/.*\"user_id\":\"\([0-9]*\)\",\"user\":\".*${MUTE_ARG1}.*\".*/\1/gI" )
        curl $PROXY -s -b ${DIR}/session/cookie -c ${DIR}/session/cookie "http://herobrinesarmy.com/mute.php?o=1&m=${MUTE_ID}" >/dev/null 2>&1 &
        else
        echo "Enter your target next time."
    fi
fi
