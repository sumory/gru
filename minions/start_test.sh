#!/bin/sh

# 'dev' or 'prod', check the folder config for more detail 
export NODE_ENV=test

echo 'Please be sure you have install all needed modules'


echo 'start @test mode'
pm2 stop minions -f
pm2 start app.js -n minions


