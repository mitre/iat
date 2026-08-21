#!/usr/bin/env sh
set -eu

VERSION_TAG="pdm-models-v1.0.0"
RELEASE_URL="https://github.com/mitre/iat/releases/download/${VERSION_TAG}"
ARCHIVE="${VERSION_TAG}.tar.gz"
CHECKSUM="${VERSION_TAG}.sha256"

cleanup() {
  rm -f "$ARCHIVE" "$CHECKSUM"
}

trap cleanup EXIT

curl -fL --retry 3 -o "$ARCHIVE" "$RELEASE_URL/$ARCHIVE"
curl -fL --retry 3 -o "$CHECKSUM" "$RELEASE_URL/$CHECKSUM"

if command -v sha256sum >/dev/null 2>&1; then
  sha256sum -c "$CHECKSUM"
else
  shasum -a 256 -c "$CHECKSUM"
fi

tar -xzf "$ARCHIVE"
