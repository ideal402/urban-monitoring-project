#!/bin/bash

echo "--- [1/2] API 서버 빌드 및 업로드 시작 ---"
docker build --platform linux/amd64 --target api-runner -t ideal402/seoul-api:latest .
docker push ideal402/seoul-api:latest

echo "--- [2/2] Batch 서버 빌드 및 업로드 시작 ---"
docker build --platform linux/amd64 --target batch-runner -t ideal402/seoul-batch:latest .
docker push ideal402/seoul-batch:latest

echo "--- 모든 작업이 완료되었습니다! ---"