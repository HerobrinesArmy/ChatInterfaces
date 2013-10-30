#!/bin/bash
if [[ "$MESSAGE" == /profile* ]]
then
    COMMAND_GIVEN="1"
    MESSAGE_LENGTH=$( echo $MESSAGE | wc -w )
    if [ $MESSAGE_LENGTH -gt 1 ]
        then
        PROFILE_ARG1=$( echo $MESSAGE | cut -d' ' -f2- )
        PROFILE=$( sed "s/.*\"user_id\":\"\([0-9]*\)\",\"user\":\".*${PROFILE_ARG1}.*\".*/\1/gI" < ${DIR}/session/tmp/${CHAT_ROOM}/LAST_JSON_INPUT )
        echo "http://herobrinesarmy.enjin.com/profile/${PROFILE}"
        else
        echo "You must enter a name."
    fi
fi
