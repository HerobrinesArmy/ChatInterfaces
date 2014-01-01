#!/bin/bash
# The entire purpose of this script is to shut down chat
killChat ()
{
    rm -rf ${DIR}/session/tmp/${chat_room}/ >/dev/null 2>&1
    tmux kill-session -t "${USER}${chat_room}"
    exit 0
}
