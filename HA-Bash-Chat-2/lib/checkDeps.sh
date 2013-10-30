#!/bin/bash
. ${DIR}/lib/killChat.sh
DEPS="tmux
curl
sed
grep
cut
ping
tr
xxd"
for x in $DEPS
do
command -v $x >/dev/null 2>&1 || { echo >&2 "I require $x but it's not installed.  Aborting."; killChat; exit 1; }
done
