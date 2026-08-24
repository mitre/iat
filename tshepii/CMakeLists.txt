#
# NOTICE
# 
# This software (or technical data) was produced for the U. S. Government
# and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
# (May 2014) – Alternative IV (Dec 2007)
# 
# (c) 2026 The MITRE Corporation. All Rights Reserved.
# 

cmake_minimum_required(VERSION 2.8)
project(CryptDetector)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -std=gnu++0x -Wno-deprecated-declarations -Wno-sign-compare")

find_package(OpenCV REQUIRED)
set(CD_SOURCE
    src/Configuration.cpp
    src/CryptDetector.cpp
    src/CryptMatcher.cpp
    src/Functions.cpp
    src/Iris.cpp
    src/IrisSegmenter.cpp
    src/OsiCircle.cpp
    src/OsiProcessings.cpp
    src/Client.cpp
    src/configparser.cpp
)

find_package(Protobuf REQUIRED)
include_directories(${PROTOBUF_INCLUDE_DIRS})
include_directories(${CMAKE_CURRENT_BINARY_DIR})

# Use the IAT's shared Protocol Buffer schemas rather than maintaining a
# component-local copy.
set(PROTOPATH "${CMAKE_CURRENT_SOURCE_DIR}/../buffers/src/main/proto")
set(PROTOFILES
    "${PROTOPATH}/Tshepii.proto"
    "${PROTOPATH}/Iwp.proto"
)

# Fail early if the path or files are wrong
if(NOT EXISTS "${PROTOPATH}")
  message(FATAL_ERROR "Shared proto directory not found: ${PROTOPATH}")
endif()

foreach(_pf IN LISTS PROTOFILES)
  if(NOT EXISTS "${_pf}")
    message(FATAL_ERROR "Proto file not found: ${_pf}")
  endif()
endforeach()

# Pick protoc executable variable name (depends on CMake/FindProtobuf)
set(_PROTOC "${PROTOBUF_PROTOC_EXECUTABLE}")
if(NOT _PROTOC)
  set(_PROTOC "${Protobuf_PROTOC_EXECUTABLE}")
endif()
if(NOT _PROTOC)
  message(FATAL_ERROR "Could not determine protoc executable from FindProtobuf")
endif()

# Generated outputs
set(GENERATED_PROTO_SRCS
  "${CMAKE_CURRENT_BINARY_DIR}/Tshepii.pb.cc"
  "${CMAKE_CURRENT_BINARY_DIR}/Iwp.pb.cc"
)
set(GENERATED_PROTO_HDRS
  "${CMAKE_CURRENT_BINARY_DIR}/Tshepii.pb.h"
  "${CMAKE_CURRENT_BINARY_DIR}/Iwp.pb.h"
)

# Generate sources as part of the build (not only at configure time)
add_custom_command(
  OUTPUT ${GENERATED_PROTO_SRCS} ${GENERATED_PROTO_HDRS}
  COMMAND "${_PROTOC}"
          --cpp_out "${CMAKE_CURRENT_BINARY_DIR}"
          --proto_path "${PROTOPATH}"
          ${PROTOFILES}
  DEPENDS ${PROTOFILES}
  COMMENT "Generating C++ sources from .proto files"
  VERBATIM
)

add_custom_target(generate_protos DEPENDS ${GENERATED_PROTO_SRCS} ${GENERATED_PROTO_HDRS})

if(PROTOBUF_VERBOSE)
  message(STATUS "Using Protocol Buffers includes: ${PROTOBUF_INCLUDE_DIRS}")
  message(STATUS "Using protoc: ${_PROTOC}")
endif()
# PROTOBUF_GENERATE_CPP(PROTO_SRCS PROTO_HDRS ${PROTOFILES})

if(PROTOBUF_VERBOSE)
  message(STATUS "Using Protocol Buffers ${Protobuf_VERSION}")
  message(STATUS "protobuf sources and header ${PROTO_SRCS} ${PROTO_HDRS}")
endif()

find_package(CURL REQUIRED)
IF(CURL_FOUND)
  INCLUDE_DIRECTORIES(${CURL_INCLUDE_DIR})
  SET(requiredlibs ${requiredlibs} ${CURL_LIBRARIES} )
ELSE(CURL_FOUND)
  MESSAGE(FATAL_ERROR "Could not find the CURL library and development files.")
ENDIF(CURL_FOUND)

set(LOG4CXX_DIR /usr CACHE PATH "Path to Log4cxx")
file(GLOB LOG4CXX_INCLUDE ${LOG4CXX_DIR}/include/log4cxx)
include_directories(${LOG4CXX_INCLUDE})
link_directories(${LOG4CXX_DIR}/lib)

set(AMQCPP_DIR /usr CACHE PATH "Path to AmqCpp")
file(GLOB AMQCPP_INCLUDE ${AMQCPP_DIR}/include/activemq-cpp-*)
include_directories(${AMQCPP_INCLUDE})
link_directories(${AMQCPP_DIR}/lib)

add_executable(tshepii
  ${CD_SOURCE}
  ${GENERATED_PROTO_SRCS}
)
add_dependencies(tshepii generate_protos)
target_link_libraries(tshepii log4cxx activemq-cpp
                      ${OpenCV_LIBS} ${PROTOBUF_LIBRARIES}
                          ${CURL_LIBRARIES})
