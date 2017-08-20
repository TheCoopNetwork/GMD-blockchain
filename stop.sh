#!/bin/sh
APPLICATION="nxt-clone"
if [ -e ~/.${APPLICATION}/nxt.pid ]; then
    PID=`cat ~/.${APPLICATION}/nxt.pid`
    ps -p $PID > /dev/null
    STATUS=$?
    echo "stopping"
    while [ $STATUS -eq 0 ]; do
        kill `cat ~/.${APPLICATION}/nxt.pid` > /dev/null
        sleep 5
        ps -p $PID > /dev/null
        STATUS=$?
    done
    rm -f ~/.${APPLICATION}/nxt.pid
    echo "Nxt server stopped"
fi

