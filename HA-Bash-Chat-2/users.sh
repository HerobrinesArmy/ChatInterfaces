#!/bin/bash
DIR="${BASH_SOURCE%/*}"
if [[ ! -d "$DIR" ]]
    then DIR="$PWD"
fi
CHAT_ROOM="$1"
while :
do
USERS_ARG=$( sed 's/.*"users":{\(.*\)/\1/g' < ${DIR}/session/tmp/${CHAT_ROOM}/LAST_JSON_INPUT | sed 's/},/},\n/g' | sed 's/.*"user":"\(<[^>]*>\)*\([^<]*\)<[^>]*>[^"]*".*/\2/g' )
clear
echo -e "\033[0;32m[$( date -u +"%T" )]\033[0m\n\033[1;31m${USERS_ARG}\033[0m"
sleep 1
done
