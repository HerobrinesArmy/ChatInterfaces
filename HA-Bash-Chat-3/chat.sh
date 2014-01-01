#!/bin/bash
version="3.0.0_alpha1"
trap 'killChat' INT QUIT
DIR="${BASH_SOURCE}%/*}"
if [[ ! -d "$DIR" ]]; then
    DIR="$PWD"
fi

# Including things starts here
. ${DIR}/lib/killChat.sh
. ${DIR}/lib/auth.sh

# Auth and various setup required before tmux session is created
auth
echo "Main chats are  8613406 (main chat) and 3 (science chat)."
read -e -p "Enter the chat room number you wish to join: " -i "8613406" chat_room
mkdir -p "${DIR}/session/tmp/"
mkdir "${DIR}/session/tmp/${chat_room}/"
echo "" > ${DIR}/session/tmp/${chat_room}/last_json_input
session="${USER}${chat_room}"

# 0 = message
# 1 = userlist (max name width of 17 chars at this time)
# 2 = input
tmux -2 new-session -d -s $session \; split-window -v \; select-pane -t 0 \; split-window -h \; resize-pane -t 2 -y 2 \; resize-pane -t 1 -x 17 \; select-pane -t 0 \; send-keys "${DIR}/getMessages.sh $chat_room" C-m \; select-pane -t 1 \; send-keys "${DIR}/users.sh $chat_room" C-m \; select-pane -t 2 \; send-keys "${DIR}/input.sh $chat_room $version" C-m \; attach-session -t $session
