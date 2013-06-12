#!/bin/bash
if [[ "$MESSAGE" == /hug* ]]
then
    COMMAND_GIVEN="1"
    if [ $( echo $MESSAGE | wc -w ) -gt 1 ]
        then
        HUG_ARG1=$( echo $MESSAGE | cut -d' ' -f2- )
        if [ "$HUG_ARG1" == "all" ]
            then
            postMessage "@[you] :hug:" &
            else
            postMessage "@${HUG_ARG1} :hug:" &
        fi
        else
        echo "You must enter something."
    fi
fi
