#!/bin/bash
killChat ()
{
    rm -rf ${DIR}/session/tmp/${CHAT_ROOM}/ >/dev/null 2>&1
    tmux kill-session
    exit 0
}
