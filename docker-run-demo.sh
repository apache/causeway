#!/bin/bash

NAME="apache-isis-demo-app" 
IMAGE="apacheisis/demo-springboot:latest" 
PORT=8080

echo ""
echo "killing..."
docker kill $NAME 2>/dev/null

echo ""
echo "removing ..."
docker rm $NAME 2>/dev/null

echo ""
echo "pulling..."
docker pull $IMAGE

echo ""
echo "running..."
docker run -d -p$PORT:8080 -ePROTOTYPING=true --name $NAME $IMAGE

sleep 1
echo ""
echo "status..."
docker ps

echo ""
echo "logs..."
docker logs -f $NAME
