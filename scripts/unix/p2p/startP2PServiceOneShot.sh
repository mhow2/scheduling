#!/bin/sh

workingDir=`dirname $0`

cd $PWD/..
. env.sh
cd $workingDir

#FT
if [ "$1" = "-ft" ]; then
	#-ft server_global ttc protocol
    shift
    FT="-Dproactive.ft=enable -Dproactive.ft.server.global=$1  -Dproactive.ft.ttc=$2 -Dproactive.ft.protocol=$3"
    shift
    shift
    shift
fi

echo
echo --- StartP2PService -------------------------------------
$JAVACMD -Dsun.net.spi.nameservice.provider.1=dns,sun $FT org.objectweb.proactive.extra.p2p.service.StartP2PService "$@"
   	
echo
echo ---------------------------------------------------------
