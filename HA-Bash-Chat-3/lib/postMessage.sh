#!/bin/bash
postMessage ()
{
    local encoded_message=$( urlEncode "$1" )
    curl -m 60 -L -b $DIR/session/cookie -c $DIR/session/cookie "http://herobrinesarmy.com/post_chat.php?c=${chat_room}&o=1&m=${encoded_message}" >/dev/null 2>&1
}
