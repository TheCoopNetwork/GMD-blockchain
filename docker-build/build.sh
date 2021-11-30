#!/bin/bash
echo "Build script started"

rm -rf /output/*
rm -rf /output-docker-image/*

cd /srcgmd/
ls -lrth

bash ./compile.sh --skip-desktop
bash ./jar.sh
cp CoopNetwork.jar /output/
cp -r html /output/
cp -r lib /output/
cp -r conf /output/
#cp -r nxt_db /output/
mkdir -p /output/logs

jlink --add-modules "java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.naming,java.scripting,java.security.jgss,java.sql,java.transaction.xa,java.xml" --output ../output/jre-for-gmd
echo 'java -cp lib\*;conf;CoopNetwork.jar -Dnxt.runtime.dirProvider=nxt.env.DefaultDirProvider nxt.Nxt' > /output/start.bat
echo 'jlink --add-modules "java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.naming,java.scripting,java.security.jgss,java.sql,java.transaction.xa,java.xml" --output ../output/jre-for-gmd-win' > /output/get-jre-win.bat
echo '#!/bin/bash' > /output/start.sh
echo 'jre-for-gmd/bin/java -jar CoopNetwork.jar -cp lib\* -cp conf -Dnxt.runtime.dirProvider=nxt.env.DefaultDirProvider nxt.Nxt' >> /output/start.sh
chmod +x /output/start.sh

cp /docker-image/Dockerfile /output/
cd /output/
docker build -t "gasiminei/gmd-node" .
rm -f /output/Dockerfile
#docker image save -o /output-docker-image/gmd-node.tar gmd-node
cp -f /docker-image/docker-compose.yml /output-docker-image/docker-compose.yml
#gzip /output-docker-image/gmd-node.tar
mkdir -p /output-docker-image/conf
touch /output-docker-image/conf/nxt.properties


echo "Build script finished"
