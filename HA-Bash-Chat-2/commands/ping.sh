#!/bin/bash
if [[ "$MESSAGE" == /ping ]]
then
    COMMAND_GIVEN="1"
    ping -c1 herobrinesarmy.com | grep rtt | cut -d'/' -f5
fi
