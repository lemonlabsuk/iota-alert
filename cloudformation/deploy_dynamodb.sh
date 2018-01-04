#!/usr/bin/env bash

aws cloudformation create-stack \
    --capabilities CAPABILITY_IAM \
    --stack-name iota-alert-dynamodb \
    --template-body file://dynamodb.json
