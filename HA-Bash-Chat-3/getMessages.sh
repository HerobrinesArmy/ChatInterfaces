#!/bin/bash
DIR="${BASH_SOURCE}%/*}"
if [[ ! -d "$DIR" ]]; then
    DIR="$PWD"
fi
GLOBIGNORE="*"
chat_room="${1}"

# Including things starts here
. ${DIR}/lib/urlEncode.sh
. ${DIR}/lib/postMessage.sh
. ${DIR}/lib/postList.sh
. ${DIR}/lib/killChat.sh

mkdir -p "${DIR}/bots/"

lmid="0"

while :; do
    json_input=$( curl -m 60 -s -L -b ${DIR}/session/cookie -c ${DIR}/session/cookie "http://herobrinesarmy.com/update_chat2.php?c=${chat_room}&l=${lmid}&p=0" 2>/dev/null )
    if ! [[ "$json_input" =~ "Could not connect.*" ]] && [[ "$json_input" =~ .*\"messages\":.* ]]; then
        if [[ -n "$json_input" ]]; then
            lmid=$( ${DIR}/JSON.sh -b <<<$( sed -e 's/(\(.*\))/\1/g' <<<${json_input} ) | grep -Po '\["lmid"\].*' | cut -f2 | sed 's/"\(.*\)"/\1/g' )
            echo "$json_input" > ${DIR}/session/tmp/${chat_room}/last_json_input

