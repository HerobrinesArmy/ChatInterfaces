#!/bin/bash
if [[ "$MESSAGE" == /version ]]
then
    COMMAND_GIVEN="1"
    echo $VERSION
fi
