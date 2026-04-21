#!/bin/bash
set -euo pipefail

bucket="${AWS_BUCKET_NAME:-team4you-files}"
echo "checking/creating s3 bucket: $bucket"

awslocal s3api head-bucket --bucket "$bucket" 2>/dev/null || awslocal s3 mb "s3://$bucket"

echo "available buckets:"
awslocal s3 ls