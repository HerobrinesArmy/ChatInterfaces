#!/bin/bash
if [[ "$MESSAGE" == /resize* ]]
then
    COMMAND_GIVEN="1"
    tmux resize-pane -t 2 -y 2
    tmux resize-pane -t 1 -x 15
fi
