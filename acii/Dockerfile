#
# NOTICE
# 
# This software (or technical data) was produced for the U. S. Government
# and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
# (May 2014) – Alternative IV (Dec 2007)
# 
# (c) 2026 The MITRE Corporation. All Rights Reserved.
# 
FROM alpine:3.23

COPY acii /acii
COPY buffers/src/main/proto /buffers/src/main/proto

WORKDIR /acii/install

# Building on some infrastructure requires the installation of a certificate authority. This is OPTIONAL
# for external systems and will only be installed when the ca_file secret is provided in the build command.
RUN --mount=type=secret,id=ca_file,required=false \
    if [[ -f /run/secrets/ca_file ]]; then \
       cat /run/secrets/ca_file >> /etc/ssl/certs/ca-certificates.crt; \
    fi
    
RUN ./install.sh && rm -rf /acii /buffers /apache-log4cxx-* /activemq-cpp-library-* /opencv-*

CMD [ "aciiComponent", "/etc/iwp-acii/acii.conf", "/etc/iwp-acii/logger.conf", "false", "/var/log/acii.log" ]
