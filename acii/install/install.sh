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

set -ex

OPENCV_VERSION="2.4.13.6"
PROTOBUF_VERSION="3.5.1"
LOG4CXX_VERSION="0.11.0"
ACTIVEMQ_CPP_VERSION="3.9.5"

AMQCPP_DIR="/usr"
LOG4CXX_DIR="/usr"

PROCS=$(nproc)

# Default C++ standard for building acii-client (override in CI with ACII_CXX_STANDARD=11)
: "${ACII_CXX_STANDARD:=17}"

if   [ -e /sbin/apk    ]; then
    ALPINE=1
elif [ -e /usr/bin/apt ]; then
    DEBIAN=1
fi

# If protobuf is already installed, check the installed major version.
if [ -n "$(which protoc 2>/dev/null)" ]; then
  if [ "$(protoc --version | awk '{print $2}' | cut -d. -f1)" -lt 3 ]; then
    echo
    echo "You have $(protoc --version) currently installed on your system."
    echo "Protobuf version 3 or greater is required for this application."
    echo "Please remove any previous versions before installing ACII."
    exit 1
  fi
fi

build_protoc()
{
  if [ -z "$(which protoc 2>/dev/null)" ]; then
    OLDPATH=$PWD
    if [ ! -f "protobuf-all-${PROTOBUF_VERSION}.tar.gz" ]; then
      curl -Lko "protobuf-all-${PROTOBUF_VERSION}.tar.gz" \
        "https://github.com/google/protobuf/releases/download/v${PROTOBUF_VERSION}/protobuf-all-${PROTOBUF_VERSION}.tar.gz"
    fi
    tar xf "protobuf-all-${PROTOBUF_VERSION}.tar.gz"
    cd "protobuf-${PROTOBUF_VERSION}"
    ./configure
    make clean
    make -j"$PROCS" install
    cd "$OLDPATH"
    rm -rf protobuf-*
  fi
}

build_log4cxx()
{
  LOG4CXX_DIR="/usr/local"
  OLDPATH=$PWD
  if [ ! -f "apache-log4cxx-${LOG4CXX_VERSION}.tar.gz" ]; then
    curl -Lko "apache-log4cxx-${LOG4CXX_VERSION}.tar.gz" \
      "https://archive.apache.org/dist/logging/log4cxx/${LOG4CXX_VERSION}/apache-log4cxx-${LOG4CXX_VERSION}.tar.gz"
  fi
  tar -zxf "apache-log4cxx-${LOG4CXX_VERSION}.tar.gz"
  cd "apache-log4cxx-${LOG4CXX_VERSION}"
  mkdir build && cd build
  cmake -DBUILD_TESTING=OFF -DCMAKE_BUILD_TYPE=RELEASE -DCMAKE_INSTALL_LIBDIR="${LOG4CXX_DIR}/lib" ..
  make -j1 -s install
  cd "$OLDPATH"
  rm -rf "apache-log4cxx-${LOG4CXX_VERSION}"*
}

build_opencv()
{
  if [ -z "$(which opencv_version 2>/dev/null)" ]; then
    OLDPATH=$PWD
    if [ ! -f "opencv-${OPENCV_VERSION}.tar.gz" ]; then
      curl -Lko "opencv-${OPENCV_VERSION}.tar.gz" \
        "https://github.com/opencv/opencv/archive/${OPENCV_VERSION}.tar.gz"
    fi
    tar -zxf "opencv-${OPENCV_VERSION}.tar.gz"
    cd "opencv-${OPENCV_VERSION}"

    # CMake 4+ can't set CMP0042 to OLD; OpenCV 2.4 tries to do that.
    sed -i 's/cmake_policy(SET CMP0042 OLD)/cmake_policy(SET CMP0042 NEW)/' CMakeLists.txt

    mkdir -p build
    cd build
    cmake -DCMAKE_BUILD_TYPE=RELEASE \
          -DBUILD_TESTS=OFF \
          -DBUILD_PERF_TESTS=OFF \
          -DBUILD_EXAMPLES=OFF \
          -DENABLE_PRECOMPILED_HEADERS=OFF \
          -DCMAKE_CXX_STANDARD=11 -DCMAKE_CXX_STANDARD_REQUIRED=ON \
          -DCMAKE_POLICY_VERSION_MINIMUM=3.5 \
          -DBUILD_opencv_python=OFF \
          -DBUILD_opencv_java=OFF \
          ..
    make -j1 -s install
    cd "$OLDPATH"
    rm -rf "opencv-${OPENCV_VERSION}"*
  fi
}

build_activemq_cpp()
{
  AMQCPP_DIR="/usr/local"
  OLDPATH=$PWD
  ACTIVEMQ_CPP_TARBALL="activemq-cpp-library-${ACTIVEMQ_CPP_VERSION}-src.tar.gz"
  ACTIVEMQ_CPP_URL="https://archive.apache.org/dist/activemq/activemq-cpp/${ACTIVEMQ_CPP_VERSION}/${ACTIVEMQ_CPP_TARBALL}"

  if [ -f "${ACTIVEMQ_CPP_TARBALL}" ] && ! tar -tzf "${ACTIVEMQ_CPP_TARBALL}" >/dev/null 2>&1; then
    echo "Removing invalid ${ACTIVEMQ_CPP_TARBALL}"
    rm -f "${ACTIVEMQ_CPP_TARBALL}"
  fi

  if [ ! -f "${ACTIVEMQ_CPP_TARBALL}" ]; then
    curl -fL --retry 3 -o "${ACTIVEMQ_CPP_TARBALL}" "${ACTIVEMQ_CPP_URL}"
  fi

  tar -zxf "${ACTIVEMQ_CPP_TARBALL}"
  cd "activemq-cpp-library-${ACTIVEMQ_CPP_VERSION}"
  ./configure
  make -j"$PROCS" install
  cd "$OLDPATH"
  rm -rf "activemq-cpp-library-${ACTIVEMQ_CPP_VERSION}"*
}

STARTPATH=$PWD

# install dependencies
if [ "$ALPINE" ]; then
  # Alpine protobuf (apk) depends on Abseil at runtime; protoc will fail without it
  deps="protobuf abseil-cpp curl apr apr-util"
  build_deps="alpine-sdk cmake protobuf-dev abseil-cpp-dev curl-dev apr-dev apr-util-dev"

  apk -v update > /tmp/apk-update.log 2>&1 || { tail -n 200 /tmp/apk-update.log; exit 15; }
  apk -v add --no-cache $deps $build_deps > /tmp/apk-add.log 2>&1 || { tail -n 200 /tmp/apk-add.log; exit 15; }

  build_log4cxx
elif [ "$DEBIAN" ]; then
  deps="openssl curl libapr1 cmake liblog4cxx-dev libapr1-dev"
  build_deps="g++ autoconf automake libtool libcurl4-openssl-dev"
  echo "/usr/local/lib" > /etc/ld.so.conf.d/usr-local.conf
  echo "deb [trusted=yes] http://archive.debian.org/debian stretch main non-free contrib" > /etc/apt/sources.list
  echo "deb-src [trusted=yes] http://archive.debian.org/debian stretch main non-free contrib" >> /etc/apt/sources.list
  echo "deb [trusted=yes] http://archive.debian.org/debian-security stretch/updates main non-free contrib" >> /etc/apt/sources.list
  apt update
  apt -y install ${deps} ${build_deps}
  build_protoc
fi

# download and build activemq-cpp
build_activemq_cpp

# download and build opencv
build_opencv

cd "$STARTPATH"

# build ACII
cd "$(dirname "$0")/../acii-client/"

rm -rf build
[ "$DEBIAN" ] && ldconfig

ACII_PROTO_DIR="$PWD/../../buffers/src/main/proto"

echo "=== proto dir check ==="
echo "Using ACII_PROTO_DIR=${ACII_PROTO_DIR}"
ls -la "$ACII_PROTO_DIR" || true
find "$ACII_PROTO_DIR" -maxdepth 1 -name '*.proto' -print || true

mkdir build
cd build
cmake -DCMAKE_POLICY_VERSION_MINIMUM=3.5 \
      -DCMAKE_CXX_STANDARD="${ACII_CXX_STANDARD}" \
      -DCMAKE_CXX_STANDARD_REQUIRED=ON \
      -DACII_PROTO_DIR="${ACII_PROTO_DIR}" \
      -DLOG4CXX_DIR="${LOG4CXX_DIR}" \
      -DAMQCPP_DIR="${AMQCPP_DIR}" \
      ..
make -j"$PROCS" install

[ "$ALPINE" ] && apk del ${build_deps}
[ "$DEBIAN" ] && apt -y remove ${build_deps} && apt -y autoremove

cd "$STARTPATH"
