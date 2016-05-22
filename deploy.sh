#!/bin/bash -eu
sbt clean &&\
sbt assembly && \
aws s3 cp target/scala-2.11/lambda-twilio-assembly-0.1-SNAPSHOT.jar s3://$BUCKET_NAME/ && \
aws lambda update-function-code \
  --function-name $FUNCTION_NAME \
  --s3-bucket $S3_BUCKET --s3-key lambda-twilio-assembly-0.1-SNAPSHOT.jar

#aws lambda create-function \
#  --function-name $FUNCTION_NAME \
#  --zip-file fileb://target/scala-2.11/lambda-twilio-assembly-0.1-SNAPSHOT.jar \
#  --role arn:aws:iam::$AWS_ID:role/$LAMBDA_ROLE \
#  --handler com.github.skohar.lambdatwilio.App::handler \
#  --runtime java8 \
#  --timeout 30 \
#  --memory-size 512
