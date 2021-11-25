#!/bin/bash
echo "Build script started"

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

#echo 'jlink --add-modules "java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.naming,java.scripting,java.security.jgss,java.sql,java.transaction.xa,java.xml" --output ../output/jre-for-gmd-win' > /output/get-jre-win.bat
#echo 'java -cp lib\*;conf;CoopNetwork.jar -Dnxt.runtime.dirProvider=nxt.env.DefaultDirProvider nxt.Nxt' > /output/start.bat
echo '#!/bin/bash' > /output/start.sh
echo 'jre-for-gmd/bin/java -jar CoopNetwork.jar -cp lib\* -cp conf -Dnxt.runtime.dirProvider=nxt.env.DefaultDirProvider nxt.Nxt' >> /output/start.sh
chmod +x /output/start.sh

echo "Build script finished"