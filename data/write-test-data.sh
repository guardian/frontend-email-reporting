#!/usr/bin/env bash
aws dynamodb batch-write-item --request-items file://$1 --profile frontend --region eu-west-1
