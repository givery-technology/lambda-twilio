#!/bin/bash -eu
sbt clean &&\
sbt assembly && \
aws lambda update-function-code \
  --function-name $FUNCTION_NAME \
  --zip-file fileb://target/scala-2.11/lambda-twilio-assembly-0.1-SNAPSHOT.jar

#aws lambda create-function \
#  --function-name $FUNCTION_NAME \
#  --zip-file fileb://target/scala-2.11/lambda-twilio-assembly-0.1-SNAPSHOT.jar \
#  --role arn:aws:iam::$AWS_ID:role/$LAMBDA_ROLE \
#  --handler com.github.skohar.lambdatwilio.App::handler \
#  --runtime java8 \
#  --timeout 30 \
#  --memory-size 512
