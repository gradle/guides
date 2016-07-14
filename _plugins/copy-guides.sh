#!/usr/bin/env bash

GUIDES_DIR=_guides

for guide in $(find $GUIDES_DIR -type d -mindepth 1 -maxdepth 1 | xargs basename); do
    cp -rv $GUIDES_DIR/$guide/build/asciidoc/html5/ _site/$guide
done
