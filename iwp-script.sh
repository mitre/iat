#!/bin/bash

#
# NOTICE
# 
# This software (or technical data) was produced for the U. S. Government 
# and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
# (May 2014) – Alternative IV (Dec 2007)
# 
# (c) 2024 The MITRE Corporation. All Rights Reserved.
# 

IAT_DIR="$(cd "$(dirname "$0")" && pwd)"
IW_DIR="${IAT_DIR}/web"
IW_TARGET_SRC="${IW_DIR}/target"
IW_JAR_NAME="web-2.0.0.jar"
IW_ENV_FILE="${IAT_DIR}/docker/.env"


build() {
	echo "Building the iris workstation..."
	cd "$IAT_DIR" || exit 1
	mvn clean package -DskipTests
}

killIrisWs() {
	echo "Killing the iris workstation..."
	ps -ef | grep "[j]ava -jar ${IW_JAR_NAME}"
	ps -ef | grep "[j]ava -jar ${IW_JAR_NAME}" | awk '{ print $2 }' | xargs -I {} kill {}
}

startService() {
	echo
	echo "Starting service..."

	if [[ ! -d "$IW_TARGET_SRC" ]]; then
		echo "Creating ${IW_TARGET_SRC}"
		mkdir -p "$IW_TARGET_SRC" || exit 1
	fi

	cd "$IW_TARGET_SRC" || exit 1

	if [[ -f "$IW_ENV_FILE" ]]; then
		echo "Loading environment from ${IW_ENV_FILE}"
		IW_PRESET_JASYPT_ENCRYPTOR_PASSWORD="${JASYPT_ENCRYPTOR_PASSWORD}"
		set -a
		source "$IW_ENV_FILE"
		set +a
		if [[ -z "$JASYPT_ENCRYPTOR_PASSWORD" && -n "$IW_PRESET_JASYPT_ENCRYPTOR_PASSWORD" ]]; then
			export JASYPT_ENCRYPTOR_PASSWORD="$IW_PRESET_JASYPT_ENCRYPTOR_PASSWORD"
		fi
	else
		echo "Unable to find ${IW_ENV_FILE}. Create it from docker/.env.template before starting."
		exit 1
	fi

	# Jasypt expects JASYPT_ENCRYPTOR_PASSWORD to contain the decryption password.
	# Passing the password on the command line can expose it to process-listing tools.
	# To run without a prompt or command-line password, set this environment variable:
	# export JASYPT_ENCRYPTOR_PASSWORD="yourpass"

	# Use JASYPT_ENCRYPTOR_PASSWORD from the argument when it is provided.
	if [[ -n "$1" ]]; then
			echo "Using JASYPT_ENCRYPTOR_PASSWORD passed from the command line"
		export JASYPT_ENCRYPTOR_PASSWORD=$1

	# Otherwise, use JASYPT_ENCRYPTOR_PASSWORD from the environment when it is set.
	elif [[ -n "$JASYPT_ENCRYPTOR_PASSWORD" ]]; then
		echo "Using JASYPT_ENCRYPTOR_PASSWORD from environment variable"
	# If JASYPT_ENCRYPTOR_PASSWORD is not set or provided, prompt for it.
	else
		echo "Enter jasypt encryption password: "
		read -s JASYPT_ENCRYPTOR_PASSWORD
		export JASYPT_ENCRYPTOR_PASSWORD
	fi

	if [[ ! -f "$IW_JAR_NAME" ]]; then
		echo "Unable to find ${IW_JAR_NAME}. Run './iwp-script.sh build' first."
		exit 1
	fi

	nohup java -jar "$IW_JAR_NAME" > /tmp/iwp-web.log &
	tail -f /tmp/iwp-web.log
}

pauseForKill() {
	echo "Sleeping for .5 seconds"
	sleep .5
}

case $1 in 
	build)
		build
		;;
	kill)
		killIrisWs
		;;
	full)
		killIrisWs && build && startService $2
		;;
	start)
		startService $2
		;;
	restart)
		killIrisWs && pauseForKill && startService
		;;
  help)
    echo "Arguments [build|kill|full|start|restart] [Jasypt encryption password]"
    ;;
	*)
		echo "Unknown argument [build|kill|full|start|restart] [Jasypt encryption password]"
esac
