#!/bin/bash
sbt assembly
aws s3 cp target/scala-2.11/lambda-twilio-assembly-0.1-SNAPSHOT.jar s3://$S3_BUCKET/
aws lambda update-function-code --function-name $FUNCTION_NAME --s3-bucket $S3_BUCKET --s3-key home-ip-assembly-0.1-SNAPSHOT.jar
