#!/bin/bash
COMMANDNAME=$( echo $MESSAGE | cut -d'/' -f2- | cut -d' ' -f1 )
if [[ "$MESSAGE" == /${COMMANDNAME}* ]] && [[ -e ${DIR}/data/lists/${COMMANDNAME}.txt ]]
    then
        COMMAND_GIVEN="1"
        postList "$MESSAGE" "$COMMANDNAME"
fi
