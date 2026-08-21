#!/bin/sh
set -eu

sed \
  -e "s|\${ARTEMIS_USER}|${ARTEMIS_USER}|g" \
  -e "s|\${ARTEMIS_PASSWORD}|${ARTEMIS_PASSWORD}|g" \
  /etc/iwp-acii/acii.conf.template > /etc/iwp-acii/acii.conf

exec aciiComponent /etc/iwp-acii/acii.conf /etc/iwp-acii/logger.conf false /var/log/acii.log