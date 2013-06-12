#!/bin/bash
DIR="${BASH_SOURCE%/*}"
if [[ ! -d "$DIR" ]]
    then DIR="$PWD"
fi
CHAT_ROOM="$1"
while clear
do
USERS_ARG=$( cat ${DIR}/session/tmp/${CHAT_ROOM}/LAST_JSON_INPUT | sed 's/.*"users":{\(.*\)/\1/g' | sed 's/},/},\n/g' | sed 's/.*"user":"\(<[^>]*>\)*\([^<]*\)<[^>]*>[^"]*".*/\2/g' )
echo -e "\033[1;31m${USERS_ARG}\033[0m"
sleep 5
done
