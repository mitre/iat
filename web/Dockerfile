#
# NOTICE
# 
# This software (or technical data) was produced for the U. S. Government 
# and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
# (May 2014) – Alternative IV (Dec 2007)
# 
# (c) 2026 The MITRE Corporation. All Rights Reserved.
# 

FROM eclipse-temurin:17-jre-alpine-3.23

USER root

RUN addgroup -S -g 433 iat \
 && adduser  -S -u 431 -G iat -s /sbin/nologin iat

WORKDIR /app

COPY target/*.jar install/launch.sh /app/

RUN chown -R 431:433 /app \
 && chmod +x /app/launch.sh

VOLUME ["/app/config"]

USER 431:433

CMD ["sh", "/app/launch.sh"]
