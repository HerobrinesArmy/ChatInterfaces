#!/bin/bash
if [[ "$MESSAGE" == /paste ]]
then
    COMMAND_GIVEN="1"
    PASTE_CONTENT=$( xclip -o -selection clip-board )
    postMessage "[code]${PASTE_CONTENT}[/code]" &
fi
