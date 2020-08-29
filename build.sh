#!/bin/bash

OS=${1:?"Need OS type (windows/linux/mac)"}

echo "Starting build..."

if [ "$OS" == "windows" ]; then
    J_ARG="@jpackage/windows"
elif [ "$OS" == "linux" ]; then
    J_ARG="@jpackage/linux"
else
    J_ARG="@jpackage/mac"
fi

clj -A:uberjar
jpackage @jpackage/common $J_ARG
