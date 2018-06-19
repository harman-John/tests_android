#!/bin/bash

# Only need to config APP_HOME dir.
APP_HOME=`pwd`
APK_DIST_DIR="$APP_HOME/build"

echo "APP build start, app home = $APP_HOME"
if [ ! -d $APK_DIST_DIR ]; then
	mkdir -p $APK_DIST_DIR
fi

function buildApp(){
	buildType=$1
	echo "buildType is " $buildType

	if [ $buildType == 'debug' ]; then
		./gradlew clean assembleDebug &
	else
		./gradlew clean assembleRelease &
	fi

	wait $!
	ret=$?

	if [ $ret != 0 ]; then
		echo "ERROR ==========> APP Build Failed  - $buildType"
		exit 1
	fi
}

buildApp debug
buildApp release

echo "APP Build Successfully, then copy to $APK_DIST_DIR"
APK_FILE=`find $APP_HOME/app/build -name *.apk`
echo "Apk File Path = $APK_FILE"
for element in $APK_FILE
do
    cp $element $APK_DIST_DIR
done
echo "APP Copy Over - $buildType"
