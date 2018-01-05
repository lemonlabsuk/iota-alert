#!/usr/bin/env bash

while getopts "e:" OPT
do
  case $OPT in
    e) ENVIRONMENT="$OPTARG" ;;
  esac
done

if [ "$ENVIRONMENT" == "" ]; then
	echo "-e <dev,prod,etc> is required"
	exit 1
fi

aws cloudformation create-stack --stack-name iota-alert-infrastructure-$ENVIRONMENT --template-body file://infrastructure.json
