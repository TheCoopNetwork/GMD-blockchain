#!/bin/bash

if [ "$#" -lt 2 ]; then
  echo "Usage: beta-release-package BETA_VERSION_NUMBER BUILD_FILE..."
  echo "BETA_VERSION_NUMBER is a 3-digit number"
  echo "BUILD_FILE are one or more files to be included in the package"
	exit 1
fi

BETA_VERSION_NUMBER=$1

PUBLIC_VERSION=$(grep nxt.version ./conf/nxt-default.properties | cut -d'=' -f2)

PACKAGE_NAME=nxt-${PUBLIC_VERSION}-b${BETA_VERSION_NUMBER}.zip
echo PACKAGE_NAME="${PACKAGE_NAME}"

git log -n1 --format="%h" > commit_hash.txt

zip -q -X ${PACKAGE_NAME} commit_hash.txt "${@:2}"

rm commit_hash.txt