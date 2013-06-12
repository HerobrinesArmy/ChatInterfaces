#/bin/bash
postMessage ()
{
    local ENCODED_MESSAGE=$( urlEncode "$1" )
    curl $PROXY -m 60 -L -b $DIR/session/cookie -c $DIR/session/cookie "http://herobrinesarmy.com/post_chat.php?c=${CHAT_ROOM}&o=1&m=${ENCODED_MESSAGE}" >/dev/null 2>&1
}
