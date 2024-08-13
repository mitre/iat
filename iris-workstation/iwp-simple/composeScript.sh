#!/bin/sh

#
# NOTICE
# 
# This software (or technical data) was produced for the U. S. Government 
# and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
# (May 2014) – Alternative IV (Dec 2007)
# 
# (c) 2024 The MITRE Corporation. All Rights Reserved.
# 


killAllDocker() {
  echo "Killing all IWP Docker Containers..."
  docker kill iwp
  docker kill activemq
  docker kill acii
  docker kill biqt
  docker kill contact
  docker kill tshepii
  docker kill iwp_mysql
}

pruneSystem() {
  echo "Pruning Docker System..."
  docker system prune -f
}

pruneVolumes() {
  echo "Pruning Docker Volumes..."
  docker volume prune -f --all
}

dockerUp(){
  echo "Starting Docker Containers..."
  docker-compose up
}

case $1 in
	refresh)
	  killAllDocker && pruneSystem && pruneVolumes && dockerUp
	  ;;
	start)
	  dockerUp
	  ;;
	kill)
	  killAllDocker && pruneSystem && pruneVolumes
	  ;;
	prune)
	  pruneSystem && pruneVolumes
	  ;;
  help)
    echo "Arguments [refresh|kill|start|prune]"
    ;;
	*)
		echo "Unknown argument [refresh|kill|start|prune]"

esac