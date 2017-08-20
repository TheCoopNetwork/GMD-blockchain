#!/bin/sh
APPLICATION="nxt-clone"
java -cp classes nxt.tools.ManifestGenerator
/bin/rm -f ${APPLICATION}.jar
jar cfm ${APPLICATION}.jar resource/nxt.manifest.mf -C classes . || exit 1
/bin/rm -f ${APPLICATION}service.jar
jar cfm ${APPLICATION}service.jar resource/nxtservice.manifest.mf -C classes . || exit 1

echo "jar files generated successfully"