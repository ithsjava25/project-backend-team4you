#!/bin/bash
set -euo pipefail

bucket="${AWS_BUCKET_NAME:-team4you-files}"
echo "checking/creating s3 bucket: $bucket"

if awslocal s3api head-bucket --bucket "$bucket" 2>/dev/null; then
  echo "bucket already exists: $bucket"
else
  echo "creating bucket: $bucket"
  awslocal s3 mb "s3://$bucket"
fi

echo "available buckets:"
awslocal s3 ls