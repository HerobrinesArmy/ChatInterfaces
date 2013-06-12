#!/bin/bash
for LISTNAME in ${DIR}/data/lists/*; do
    COMMANDNAME=$( echo ${LISTNAME##*/} | cut -d'.' -f1 )
    if [[ "$MESSAGE" == /${COMMANDNAME}* ]]
        then
            COMMAND_GIVEN="1"
            postList "$MESSAGE" "$COMMANDNAME"
    fi
done
