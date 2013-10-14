#!/bin/bash
VERSION="2.3.1"
trap 'killChat' INT QUIT
DIR="${BASH_SOURCE}%/*}"
if [[ ! -d "$DIR" ]]
    then DIR="$PWD"
fi

. ${DIR}/lib/killChat.sh
. ${DIR}/lib/checkDeps.sh
. ${DIR}/lib/auth.sh

SESSION="${USER}${RANDOM}"

tmux -2 new-session -d -s $SESSION
# pane 0 is message pane
# pane 1 is user pane
# pane 2 is input pane
tmux split-window -v
tmux select-pane -t 0
tmux split-window -h
# input line
tmux resize-pane -t 2 -y 2
# user list
tmux resize-pane -t 1 -x 15
auth
echo "Main chats are  8613406 (main chat) and 3 (science chat)."
read -e -p "Enter the chat room number you wish to join: " -i "8613406" CHAT_ROOM
mkdir -p "${DIR}/session/tmp/"
mkdir "${DIR}/session/tmp/${CHAT_ROOM}/"
echo "" > ${DIR}/session/tmp/${CHAT_ROOM}/LAST_JSON_INPUT
tmux select-pane -t 0
tmux send-keys "${DIR}/getMessages.sh $CHAT_ROOM" C-m
tmux select-pane -t 1
tmux send-keys "${DIR}/users.sh $CHAT_ROOM" C-m
tmux select-pane -t 2
tmux send-keys "${DIR}/input.sh $CHAT_ROOM $VERSION" C-m
tmux attach-session -t $SESSION
