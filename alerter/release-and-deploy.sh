#!/usr/bin/env sh

set -e

if [ -z "$1" ]; then
    echo "Please pass environment name as argument"
fi

if [ ! -z "$(git status -s)" ]; then
    echo "You have uncommitted changes. Cannot release!"
    exit 1
fi

# Update version.sbt to new version number
VER=$(grep ^version version.sbt | grep -Eo [0-9]+)
NEW_VER=$((VER+1))

echo "Current version is $VER, new version is $NEW_VER"

sed -i '' "s/$VER/$NEW_VER/g" version.sbt

# Run tests and package new JAR
set +e
sbt assembly
if [ $? -ne 0 ]; then
  echo "sbt assembly failed! Reverting back to version $VER!"
  git checkout version.sbt
  exit 1
fi
set -e

# Commit new version to git!
git commit -am"Bump alerter to version $NEW_VER"
git tag alerter-$NEW_VER
git push --tags origin master

# Upload to S3
aws s3 cp "target/scala-2.12/iota-alerter-assembly-$NEW_VER.jar" "s3://iota-alert-releases/iota-alerter-$NEW_VER.jar"

# Deploy!
../cloudformation/deploy_alerter.sh -e $1 -v $NEW_VER
