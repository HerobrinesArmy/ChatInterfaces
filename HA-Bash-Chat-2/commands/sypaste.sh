#!/bin/bash
if [[ "$MESSAGE" = /pastebin* ]]
then
COMMAND_GIVEN="1"
# Clearing all variables before using them, for safety
FILE=""
TEXT=""
TITLE=""
NAME=""
LANG=""
PRIVATE=""
EXPIRATION=""

MESSAGE_LENGTH=$( echo "$MESSAGE" | wc -w )

if [ $MESSAGE_LENGTH -gt 1 ]
    then
    PASTE_ARG=$( echo "$MESSAGE" | cut -d' ' -f2 )
    fi

# Getting the file name
FILE="$PASTE_ARG"
# Setting the file text
TEXT=$( urlEncode "$( cat "$FILE" )" )
# Setting the post title
TITLE="-d title=${PASTE_ARG}"
# Setting the username to the currently logged in account
NAME="-d name=${USER}"
# Setting language
LANG=""
# Setting privacy
PRIVATE=""
# Setting paste expiration
EXPIRATION=""

# Posting the file and getting the link for it
PASTE_LINK=$( curl -s -d text="$TEXT" $TITLE $NAME $LANG $PRIVATE $EXPIRATION http://paste.syfaro.net/api/create )
postMessage "$PASTE_LINK"
fi
