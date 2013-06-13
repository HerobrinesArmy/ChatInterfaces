#!/bin/bash -i
trap 'killChat' INT QUIT
DIR="${BASH_SOURCE%/*}"
if [[ ! -d "$DIR" ]]
    then DIR="$PWD"
fi
VERSION="$2"
CHAT_ROOM="${1}"
# Including scripts
. ${DIR}/lib/postMessage.sh
. ${DIR}/lib/urlEncode.sh
. ${DIR}/lib/auth.sh
. ${DIR}/lib/killChat.sh
. ${DIR}/lib/postList.sh

COMMAND_GIVEN="0"

while :; do
    read -e MESSAGE
    for f in ${DIR}/commands/*; do
        . $f
    done
    if [[ "$MESSAGE" == /ex* ]]
        then
            killChat
        else if [ "$COMMAND_GIVEN" == "0" ]
            then
                postMessage "$MESSAGE" &
        fi
    fi
    COMMAND_GIVEN="0"
done
