#!/bin/sh
set -eu

: "${ARTEMIS_USER:?ARTEMIS_USER must be set}"
: "${ARTEMIS_PASSWORD:?ARTEMIS_PASSWORD must be set}"

template=/etc/tshepii/tshepii.conf
config=/tmp/tshepii.conf

while IFS= read -r line || [ -n "$line" ]; do
  case "$line" in
    "artemis-user=\${ARTEMIS_USER}")
      printf 'artemis-user=%s\n' "$ARTEMIS_USER"
      ;;
    "artemis-password=\${ARTEMIS_PASSWORD}")
      printf 'artemis-password=%s\n' "$ARTEMIS_PASSWORD"
      ;;
    *)
      printf '%s\n' "$line"
      ;;
  esac
done < "$template" > "$config"

exec tshepii "$config" /etc/tshepii/log4cxx.conf
