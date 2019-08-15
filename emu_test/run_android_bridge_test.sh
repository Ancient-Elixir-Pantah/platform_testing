#!/bin/bash
# It is to be used with BYOB setup to run tests on cloud VMs.
# The script will run ADB tests.
#
# It takes 1 command line argument.
# DIST_DIR => Absolute path for the distribution directory.
#
# It will return 0 if it is able to execute tests, otherwise
# it will return 1.
#
# Owner: akagrawal@google.com

DIST_DIR=$1

function run_with_timeout () {
   ( $1 $2 ) & pid=$!
   ( sleep $3 && kill -HUP $pid ) 2>/dev/null & watcher=$!
   if wait $pid 2>/dev/null; then
      pkill -HUP -P $watcher
      wait $watcher
   else
      echo "Test time out."
      # kill the process tree for test
      pkill -9 -g $pid
      exit 1
   fi
}

echo "Checkout adt-infra repo"
# $ADT_INFRA has to be set on the build machine. It should have absolute path
# where adt-infra needs to be checked out.
rm -rf $ADT_INFRA
git clone https://android.googlesource.com/platform/external/adt-infra -b emu-master-dev $ADT_INFRA

BUILD_DIR="out/prebuilt_cached/builds"

export ANDROID_HOME=$SDK_EMULATOR
export ANDROID_SDK_ROOT=$SDK_SYS_IMAGE

echo "Setup new ADB"
mv $ANDROID_SDK_ROOT/platform-tools $DIST_DIR/
unzip -o $BUILD_DIR/* -d $ANDROID_SDK_ROOT

echo "Run ADB tests from $ADT_INFRA"
cmd="$ADT_INFRA/emu_test/utils/run_test_android_bridge.sh"
run_with_timeout $cmd $DIST_DIR 5400

echo "Cleanup platform-tools"
rm -rf $ANDROID_SDK_ROOT/platform-tools
mv $DIST_DIR/platform-tools $ANDROID_SDK_ROOT/

echo "Cleanup prebuilts"
rm -rf /buildbot/prebuilt/*

exit 0
