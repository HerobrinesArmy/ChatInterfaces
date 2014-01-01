#!/bin/bash
urlEncode ()
{
    local encoded_message=$( echo -ne "${1}" | xxd -plain | tr -d '\n' | sed 's/\(..\)/%\1/g' | sed -f $DIR/lib/urlencode.sed )
    echo -ne "${encoded_message}"
}
