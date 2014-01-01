#!/bin/bash
trap 'killChat' INT QUIT
DIR="${BASH_SOURCE}%/*}"
if [[ ! -d "$DIR" ]]; then
    DIR="$PWD"
fi

chat_room="${1}"
version="${2}"

# Including things
. ${DIR}/lib/postMessage.sh
. ${DIR}/lib/urlEncode.sh
. ${DIR}/lib/auth.sh
. ${DIR}/lib/killChat.sh
. ${DIR}/lib/postList.sh

# This variable is set to 1 when a command is triggered, so the client knows
# not to send the command text to chat
command_given="0"

while :; do
    read -e message
    for f in ${DIR}/commands/*; do
        . $f
    done
    if [[ "${message}" == /ex* ]]; then
        killChat
    else if [[ "${command_given}" == "0" ]]; then
        postMessage "${message}" &
    fi
    fi
    command_given="0"
done
