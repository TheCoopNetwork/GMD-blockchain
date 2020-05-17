#!/bin/sh
APPLICATION="nxt-clone"
if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
    JAR=./jdk/bin/jar
elif [ -x ../jdk/bin/java ]; then
    JAVA=../jdk/bin/java
    JAR=../jdk/bin/jar
else
    JAVA=java
    JAR=jar
fi
${JAVA} -cp classes nxt.tools.ManifestGenerator
/bin/rm -f ${APPLICATION}.jar
${JAR} cfm ${APPLICATION}.jar resource/nxt.manifest.mf -C classes . || exit 1
/bin/rm -f ${APPLICATION}service.jar
${JAR} cfm ${APPLICATION}service.jar resource/nxtservice.manifest.mf -C classes . || exit 1

echo "jar files generated successfully"