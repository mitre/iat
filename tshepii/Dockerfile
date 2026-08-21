#
# NOTICE
# 
# This software (or technical data) was produced for the U. S. Government
# and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
# (May 2014) – Alternative IV (Dec 2007)
# 
# (c) 2026 The MITRE Corporation. All Rights Reserved.
# 

FROM debian:trixie-slim

ARG ACTIVEMQ_CPP_VERSION=3.9.5
ARG OPENCV_VERSION=2.4.13.7
ARG DEBIAN_FRONTEND=non-interactive

SHELL ["/bin/sh", "-euxc"]

RUN apt-get update && apt-get install -y --no-install-recommends \
      ca-certificates wget curl \
      build-essential cmake pkg-config \
      autoconf automake libtool \
      protobuf-compiler libprotobuf-dev \
      libapr1-dev \
      libcurl4-openssl-dev \
      liblog4cxx-dev \
      zlib1g-dev \
      libjpeg62-turbo-dev libpng-dev libtiff-dev \
    && rm -rf /var/lib/apt/lists/*

# Build ActiveMQ-CPP
RUN curl -fsSL "https://archive.apache.org/dist/activemq/activemq-cpp/${ACTIVEMQ_CPP_VERSION}/activemq-cpp-library-${ACTIVEMQ_CPP_VERSION}-src.tar.gz" \
  | tar xz \
 && cd "activemq-cpp-library-${ACTIVEMQ_CPP_VERSION}" \
 && ./configure \
 && make -j"$(nproc)" \
 && make install \
 && cd / \
 && rm -rf "activemq-cpp-library-${ACTIVEMQ_CPP_VERSION}"

# Build OpenCV
RUN curl -fsSLk "https://github.com/opencv/opencv/archive/${OPENCV_VERSION}.tar.gz" \
  | tar xz \
 && mkdir -p "opencv-${OPENCV_VERSION}/build" \
 && cd "opencv-${OPENCV_VERSION}/build" \
 && cmake \
      -D CMAKE_CXX_FLAGS="-std=gnu++11" \
      -D WITH_OPENEXR=OFF \
      -D BUILD_opencv_videostab=OFF \
      -D BUILD_opencv_ts=OFF \
      .. \
 && make -j"$(nproc)" \
 && make install \
 && cd / \
 && rm -rf "opencv-${OPENCV_VERSION}"

###############################################################  

COPY tshepii /tshepii
COPY buffers/src/main/proto /buffers/src/main/proto

ENV TSHEPII_CONFIG_FILE=/etc/tshepii/tshepii.conf
ENV LOG4CXX_CONFIGURATION=/etc/tshepii/log4cxx.conf

RUN mkdir -p /tshepii/build \
 && cd /tshepii/build \
 && cmake -DAMQCPP_DIR=/usr/local .. \
 && make -j"$(nproc)" \
 && install -m 0755 tshepii /usr/local/bin/tshepii \
 && mkdir -p /etc/tshepii \
 && cp -a /tshepii/config/* /etc/tshepii/ \
 && install -m 0755 /tshepii/start-tshepii.sh /usr/local/bin/start-tshepii

CMD ["/usr/local/bin/start-tshepii"]
