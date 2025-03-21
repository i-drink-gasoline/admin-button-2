#!/bin/sh

set -e

[ -n "$ANDROID_HOME" ] || { echo '$ANDROID_HOME is not set' && exit 1; }
[ -n "$MINDUSTRYJAR" ] || { echo '$MINDUSTRYJAR is not set' && exit 1; }

mkdir -p build
javac --release 8 -d build $(find src -name '*.java') -classpath /utmp/Mindustry.jar
jar -cf build/adminbutton2.jar mod.json icon.png -C build adminbutton2
"$ANDROID_HOME/build-tools/36.0.0-rc3/d8" --min-api 14 build/adminbutton2.jar --classpath "$MINDUSTRYJAR" --classpath "$ANDROID_HOME/platforms/android-16/android.jar" --output build/
jar -uf build/adminbutton2.jar -C build classes.dex
