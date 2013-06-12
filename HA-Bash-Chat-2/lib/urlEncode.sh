#!/bin/bash
urlEncode ()
{
    local ENCODED_MESSAGE=$( echo -ne "$1" | xxd -plain | tr -d '\n' | sed 's/\(..\)/%\1/g' | tr '[:lower:]' '[:upper:]'  | sed -f $DIR/lib/urlencode.sed )
    echo -ne "$ENCODED_MESSAGE"
}
