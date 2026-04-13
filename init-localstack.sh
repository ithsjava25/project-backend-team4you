#!/bin/bash
set -euo pipefail
bucket="${AWS_BUCKET_NAME:-team4you-files}"
awslocal s3api head-bucket --bucket "$bucket" 2>/dev/null || awslocal s3 mb "s3://$bucket"