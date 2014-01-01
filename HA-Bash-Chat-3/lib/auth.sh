#!/bin/bash
auth ()
{
    is_authed=$( curl -m 60 -s -b ${DIR}/session/cookie -c ${DIR}/session/cookie "http://herobrinesarmy.com/amiauth" )
    if [[ "${is_authed}" == "Nope." ]]; then
        if [[ "${has_authed}" == "1" ]]; then
            curl -m 60 -b ${DIR}/session/cookie -c ${DIR}/session/cookie "http://herobrinesarmy.com/amiauth"
        else
            read -p "Username: " user
            read -s -p "Password: " pass
            echo
            curl -b ${DIR}/session/cookie -c ${DIR}/session/cookie -d "user=${user}&pass=${pass}" http://herobrinesarmy.com/auth.php
            has_authed="1"
        fi
    fi
}
