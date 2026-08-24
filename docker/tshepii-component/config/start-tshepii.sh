#!/bin/sh
set -eu

sed \
  -e "s|\${ARTEMIS_USER}|${ARTEMIS_USER}|g" \
  -e "s|\${ARTEMIS_PASSWORD}|${ARTEMIS_PASSWORD}|g" \
  /etc/iwp-tshepii/tshepii.conf.template > /etc/iwp-tshepii/tshepii.conf

exec tshepii /etc/iwp-tshepii/tshepii.conf /etc/iwp-tshepii/log4cxx.conf