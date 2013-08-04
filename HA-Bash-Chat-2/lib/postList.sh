#!/bin/bash
# $1 is the message string passed in plain
# $2 is the name of the list (minus the .txt)
postList ()
{
    MESSAGE="$1"
    LIST="${DIR}/data/lists/${2}.txt"
    MESSAGE_LENGTH=$( echo ${MESSAGE} | wc -w )
    LIST_LINES=$( wc -l < $LIST )

    if [ $MESSAGE_LENGTH -gt 1 ]
        then
            LIST_ARG1=$( echo ${MESSAGE} | cut -d' ' -f2 )
    fi

    if [ -z "${LIST_ARG1}" ]
        then
            LIST_NUMBER=$[ ( $RANDOM % $LIST_LINES ) + 1 ]
            LIST_LINK=$( sed -n ${LIST_NUMBER}p "$LIST" )
            echo $LIST_NUMBER > ${DIR}/session/tmp/${CHAT_ROOM}/${2}_last
            postMessage "[img]${LIST_LINK}[/img]${LIST_NUMBER}-${2}" &
    fi

    if [[ "${LIST_ARG1}" =~ ^[0-9]+$ ]]
        then
            if [ $LIST_ARG1 -le $LIST_LINES ]
                then
                    LIST_NUMBER="$LIST_ARG1"
                    LIST_LINK=$( sed -n ${LIST_NUMBER}p "$LIST" )
                    echo $LIST_NUMBER > ${DIR}/session/tmp/${CHAT_ROOM}/${2}_last
                    postMessage "[img]${LIST_LINK}[/img]${LIST_NUMBER}-${2}" &
            fi
            if [ "$LIST_ARG1" -gt "$LIST_LINES" ]
                then
                    echo "The number you have entered is too large, the max allowed number is ${LIST_LINES}."
            fi
    fi

    if [ "$LIST_ARG1" = "count" ]
        then
            echo "There are currently $LIST_LINES pictures available."
    fi

    if [ "$LIST_ARG1" = "previous" ]
        then
            echo "The number of the last picture posted is $( < ${DIR}/session/tmp/${CHAT_ROOM}/${2}_last )."
    fi

    LIST_ARG1=""
}
