if [ -f nginx.pid ]; then
    pid=`cat nginx.pid`
    nginx -p . -c nginx.conf -s stop || exit 255
    echo nginx pid $pid stopped.
fi

if [ "$1" == "stop" ]; then
    exit 0;
fi

nginx -p . -c nginx.conf || exit 255
echo nginx pid `cat nginx.pid` started. see http://localhost:8080
