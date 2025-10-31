#!/bin/sh

set -e

MINDUSTRY_VERSION=$(jq -r .minGameVersion mod.json)
MINDUSTRY_PATH="build/Mindustry/Mindustry-v${MINDUSTRY_VERSION}.jar"

mkdir -p "build/Mindustry"

[ -e "${MINDUSTRY_PATH}" ] || {
    if [ -n "${DOWNLOAD_MINDUSTRY}" ]; then
        wget "https://github.com/Anuken/Mindustry/releases/download/v${MINDUSTRY_VERSION}/Mindustry.jar" -O "${MINDUSTRY_PATH}"
    else
        echo "${MINDUSTRY_PATH} does not exist. Set \$DOWNLOAD_MINDUSTRY envirovment variable to download it."
        exit 1
    fi
}


rm -rf "build/adminbutton2"
javac --release 8 -d build $(find src -name '*.java') -classpath "${MINDUSTRY_PATH}"
jar -cf build/adminbutton2.jar mod.json icon.png bundles -C build adminbutton2

if [ -n "${ANDROID_HOME}" ]; then
    "$ANDROID_HOME/build-tools/36.0.0-rc3/d8" --min-api 14 build/adminbutton2.jar --classpath "${MINDUSTRY_PATH}" --classpath "${ANDROID_HOME}/platforms/android-16/android.jar" --output build/
    jar -uf build/adminbutton2.jar -C build classes.dex
else
    echo '$ANDROID_HOME is not set. Skipping Android version.'
fi
