#!/bin/bash
unameOut="$(uname -s)"
case "${unameOut}" in
    Linux*)     machine=Linux;;
    Darwin*)    machine=Mac;;
    CYGWIN*)    machine=Cygwin;;
    MINGW*)     machine=MinGw;;
    *)          machine="UNKNOWN:${unameOut}"
esac

echo "** Running on ${machine}"
echo "** Will purge the database locally ....."
read -r -n 1 -p Continue?;

echo "## Step1: Stop container"
docker ps -a
docker rm -f webapp-postgres-1|| true

echo "## Step2: Remove volume of container"
docker volume ls
docker volume rm webapp_postgres|| true

echo "** Will start docker compose and SpringBoot app in PRODUCTION mode....."
read -r -n 1 -p Continue?;
if [ "$machine" == "Mac" ]; then
  docker compose up -d postgres
elif [ "$(expr substr "$(uname -s)" 1 5)" == "Linux" ]; then
  docker-compose up -d postgres
fi
./mvnw spring-boot:run
