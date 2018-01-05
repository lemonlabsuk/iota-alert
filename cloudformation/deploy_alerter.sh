#!/usr/bin/env bash

while getopts "e:v:" OPT
do
  case $OPT in
    e) ENVIRONMENT="$OPTARG" ;;
    v) VERSION="$OPTARG" ;;
  esac
done

if [ "$ENVIRONMENT" == "" ]; then
	echo "-e <dev,prod,etc> is required"
	exit 1
fi

if [ "$VERSION" == "" ]; then
	echo "-v <e.g. 1> is required"
	exit 1
fi


STACK_PARAMS=$(aws cloudformation describe-stacks --stack-name iota-alert-infrastructure-$ENVIRONMENT | jq -r '.Stacks[].Outputs | map(.OutputKey + "=" + .OutputValue)[]')
for STACK_PARAM in $STACK_PARAMS; do
  eval "export $STACK_PARAM"
  echo "$STACK_PARAM"
done

aws cloudformation create-stack --capabilities CAPABILITY_IAM --stack-name iota-alert-alerter-$VERSION-$ENVIRONMENT --template-body file://alerter.json --parameters ParameterKey=Vpc,ParameterValue=$Vpc ParameterKey=Subnet,ParameterValue=$Subnet ParameterKey=AlerterVersion,ParameterValue=$VERSION
