#!/usr/bin/env bash
#
# NOTICE
#
# This software (or technical data) was produced for the U. S. Government and
# is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
# (May 2014) - Alternative IV (Dec 2007).
#
# (c) 2026 The MITRE Corporation. All Rights Reserved.
#

set -euo pipefail

IAT_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
DEPENDENCY_ROOT=${IAT_DEPENDENCY_ROOT:-"$IAT_ROOT/../iat-dependencies"}
PLATFORM=${IAT_PLATFORM:-linux/amd64}
CA_FILE=${IAT_CA_FILE:-}

WEB_IMAGE=${IAT_WEB_IMAGE:-ghcr.io/mitre/iat/iwp-web:26.08}
ACII_IMAGE=${IAT_ACII_IMAGE:-ghcr.io/mitre/iat/iwp-acii:26.08}
BIQT_IMAGE=${IAT_BIQT_IMAGE:-ghcr.io/mitre/iat/iwp-biqt:26.08}
ANNOTATION_IMAGE=${IAT_ANNOTATION_IMAGE:-ghcr.io/mitre/iat/iwp-annotation:26.08}
TSHEPII_IMAGE=${IAT_TSHEPII_IMAGE:-ghcr.io/mitre/iat/iwp-tshepii:26.08}
PDM_IMAGE=${IAT_PDM_IMAGE:-ghcr.io/mitre/iat/iwp-pdm:26.08}

require_command() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Required command not found: $1" >&2
    exit 1
  }
}

clone_if_missing() {
  local repository=$1
  local directory=$2

  if [ ! -d "$directory/.git" ]; then
    git clone "$repository" "$directory"
  fi
}

build_image() {
  local image=$1
  local dockerfile=$2
  local context=$3
  local -a build_args

  build_args=(--platform "$PLATFORM" -t "$image" -f "$dockerfile")

  if [ -n "$CA_FILE" ]; then
    if [ ! -f "$CA_FILE" ]; then
      echo "IAT_CA_FILE does not identify a readable certificate file: $CA_FILE" >&2
      exit 1
    fi
    build_args+=(--secret "id=ca_file,src=$CA_FILE")
  fi

  DOCKER_BUILDKIT=1 docker build "${build_args[@]}" "$context"
}

require_command docker
require_command git
require_command mvn

mkdir -p "$DEPENDENCY_ROOT"
clone_if_missing https://github.com/ebts/jet.git "$DEPENDENCY_ROOT/jet"
clone_if_missing https://github.com/mitre/biqt.git "$DEPENDENCY_ROOT/biqt"

(
  cd "$DEPENDENCY_ROOT/jet/Jet"
  mvn install
)

(
  cd "$DEPENDENCY_ROOT/biqt/java"
  # BIQT's Java tests require the separately built libbiqtapi native library.
  # IAT needs the Java artifact, so install it without running those upstream tests.
  mvn install -DskipTests
)

(
  cd "$IAT_ROOT"
  mvn -DskipTests package
)

(
  cd "$IAT_ROOT/pdm"
  ./download-models.sh
)

build_image "$ACII_IMAGE" "$IAT_ROOT/acii/Dockerfile" "$IAT_ROOT"
build_image "$BIQT_IMAGE" "$IAT_ROOT/biqt/Dockerfile" "$IAT_ROOT/biqt"
build_image "$ANNOTATION_IMAGE" "$IAT_ROOT/inference/core/Dockerfile-CPU" "$IAT_ROOT/inference/core"
build_image "$TSHEPII_IMAGE" "$IAT_ROOT/tshepii/Dockerfile" "$IAT_ROOT"
build_image "$PDM_IMAGE" "$IAT_ROOT/pdm/Dockerfile" "$IAT_ROOT/pdm"
build_image "$WEB_IMAGE" "$IAT_ROOT/web/Dockerfile" "$IAT_ROOT/web"

echo "All IAT artifacts and CPU Docker images were built successfully."
