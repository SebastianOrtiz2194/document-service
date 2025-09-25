#!/bin/bash
echo "Creating S3 bucket..."
awslocal s3 mb s3://document-storage
echo "Setting bucket ACL..."
awslocal s3api put-bucket-acl --bucket document-storage --acl public-read
echo "S3 bucket created successfully!"
