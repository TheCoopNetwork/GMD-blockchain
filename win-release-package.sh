#!/bin/bash
VERSION=$1
if [ -x ${VERSION} ];
then
	echo VERSION not defined
	exit 1
fi
APPLICATION="nxt-clone"
PACKAGE=${APPLICATION}-client-${VERSION}
echo PACKAGE="${PACKAGE}"
CHANGELOG=nxt-clone-client-${VERSION}.changelog.txt
OBFUSCATE=$2

FILES="changelogs conf html lib resource contrib logs"
FILES="${FILES} ${APPLICATION}.exe ${APPLICATION}service.exe"
FILES="${FILES} 3RD-PARTY-LICENSES.txt AUTHORS.txt LICENSE.txt"
FILES="${FILES} DEVELOPERS-GUIDE.md OPERATORS-GUIDE.md README.md README.txt USERS-GUIDE.md"
FILES="${FILES} mint.bat mint.sh run.bat run.sh run-tor.sh run-desktop.sh start.sh stop.sh compact.sh compact.bat sign.sh sign.bat passphraseRecovery.sh passphraseRecovery.bat pem.to.pkcs12.keystore.certbot.hook.sh"
FILES="${FILES} nxt.policy nxtdesktop.policy Wallet.url Dockerfile"

# unix2dos *.bat
echo compile
./compile.sh
rm -rf html/doc/*
rm -rf ${APPLICATION}
rm -rf ${PACKAGE}.jar
rm -rf ${PACKAGE}.exe
rm -rf ${PACKAGE}.zip
mkdir -p ${APPLICATION}/
mkdir -p ${APPLICATION}/logs
mkdir -p ${APPLICATION}/addons/src

if [ "${OBFUSCATE}" == "obfuscate" ];
then
echo obfuscate
proguard.bat @nxt.pro
mv ../nxt.map ../nxt.map.${VERSION}
mkdir -p ${APPLICATION}/src/
else
FILES="${FILES} classes src JPL-NRS.pdf"
FILES="${FILES} compile.sh javadoc.sh jar.sh package.sh"

echo javadoc
./javadoc.sh
fi
echo copy resources
cp installer/lib/JavaExe.exe ${APPLICATION}.exe
cp installer/lib/JavaExe.exe ${APPLICATION}service.exe
cp -a ${FILES} ${APPLICATION}
cp -a logs/placeholder.txt ${APPLICATION}/logs
echo gzip
for f in `find ${APPLICATION}/html -name *.gz`
do
	rm -f "$f"
done
for f in `find ${APPLICATION}/html -name *.html -o -name *.js -o -name *.css -o -name *.json  -o -name *.ttf -o -name *.svg -o -name *.otf`
do
	gzip -9c "$f" > "$f".gz
done
cd ${APPLICATION}
echo generate jar files
../jar.sh
echo package installer Jar
../installer/build-installer.sh ../${PACKAGE}
echo create installer exe
../installer/build-exe.bat ${PACKAGE}
echo create installer zip
cd -
zip -q -X -r ${PACKAGE}.zip ${APPLICATION} -x \*/.idea/\* \*/.gitignore \*/.git/\* \*.iml ${APPLICATION}/conf/nxt.properties ${APPLICATION}/conf/logging.properties ${APPLICATION}/conf/localstorage/\*
rm -rf ${APPLICATION}

echo creating change log ${CHANGELOG}
echo -e "Release $1\n" > ${CHANGELOG}
echo -e "https://www.jelurida.com/\n" >> ${CHANGELOG}
echo -e "sha256 checksums:\n" >> ${CHANGELOG}
sha256sum ${PACKAGE}.exe >> ${CHANGELOG}

sha256sum ${PACKAGE}.jar >> ${CHANGELOG}

if [ "${OBFUSCATE}" == "obfuscate" ];
then
echo -e "\n\nThis is an experimental release for testing only. Source code is not provided." >> ${CHANGELOG}
fi
echo -e "\n\nChange log:\n" >> ${CHANGELOG}

cat changelogs/${CHANGELOG} >> ${CHANGELOG}
echo >> ${CHANGELOG}
