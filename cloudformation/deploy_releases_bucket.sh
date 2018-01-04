#!/usr/bin/env bash

aws cloudformation create-stack \
    --capabilities CAPABILITY_IAM \
    --stack-name iota-alert-releases-bucket \
    --template-body file://releases_bucket.json
