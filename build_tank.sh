#!/bin/sh
mvn clean install -DskipTests=True
scp target/tank-0.0.1-SNAPSHOT.jar pano@47.102.43.107:/data/project/top4/backend/tank_tmp.jar
ssh pano@47.102.43.107 "cd /data/project/top4/backend; rm tank.jar.bak; mv tank.jar tank.jar.bak;mv tank_tmp.jar tank.jar"
ssh pano@47.102.43.107 "ps -ef| grep 'tank'|grep -v grep|cut -c 9-15|xargs kill -9"
ssh pano@47.102.43.107 "cd /data/project/top4/backend; nohup /usr/local/jdk/bin/java -jar tank.jar --spring.config.location=./application.properties 1>out.txt 2>&1 &"