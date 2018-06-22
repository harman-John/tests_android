#!/bin/bash

projectHome=`pwd`

# Need to change appHome & apkSrcDir for your project
appHome="$projectHome/JBLHeadphone2"
apkSrcDir="$appHome/app/build/outputs/apk"

apkDstDir="$projectHome/build"

if [ ! -d $apkDstDir ]; then
	mkdir -p $apkDstDir
fi

function buildApp(){
	buildType=$1
	echo "buildType is " $buildType

	cd $appHome
	if [ $buildType == 'debug' ]; then
		./gradlew clean assembleDebug
	else
		./gradlew clean assembleRelease
	fi

	wait $!
	ret=$?

	echo "build $buildType ret=" $ret
	if [ $ret == 0 ]; then
		echo "copy $apkSrcDir/$buildType/*.apk ----> $apkDstDir"
		cp $apkSrcDir/$buildType/*.apk $apkDstDir
		echo "build $buildType version APP successfully"
	else
		echo "build $buildType version APP failed"
		exit 1	
	fi
}

buildApp debug
buildApp release
