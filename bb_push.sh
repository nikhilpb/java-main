#!/bin/bash

message=${1:-"Default"}
git commit -a -m "$message"
git push bitbucket master
