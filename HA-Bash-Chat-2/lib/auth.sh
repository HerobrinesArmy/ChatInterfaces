#!/bin/bash
auth ()
{
    IS_AUTHED=$( curl ${PROXY} -m 60 -s -b ${DIR}/session/cookie -c ${DIR}/session/cookie "http://herobrinesarmy.com/amiauth" )
    if [ "$IS_AUTHED" = "Nope." ]
        then
            if [ "$HAS_AUTHED" = "1" ]
                then
                    curl ${PROXY} -m 60 -b ${DIR}/session/cookie -c ${DIR}/session/cookie "http://herobrinesarmy.com/amiauth"
                else
                    read -p "Username: " USER
                    read -s -p "Password: " PASS
                    echo
                    curl ${PROXY} -b ${DIR}/session/cookie -c ${DIR}/session/cookie -d "user=${USER}&pass=${PASS}" http://herobrinesarmy.com/auth.php
                    HAS_AUTHED="1"
                fi
    fi
}
