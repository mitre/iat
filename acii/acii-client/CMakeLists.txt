#[[
  NOTICE

  This software (or technical data) was produced for the U. S. Government
  and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
  (May 2014) – Alternative IV (Dec 2007)

  (c) 2024 The MITRE Corporation. All Rights Reserved.
#]]

cmake_minimum_required(VERSION 3.5)
project(acii_component)

# Default to C++17, but allow CI to override via -DCMAKE_CXX_STANDARD=11
if(NOT DEFINED CMAKE_CXX_STANDARD)
  set(CMAKE_CXX_STANDARD 17)
endif()
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS ON)

option(EOC2_DEBUG "Build EOC2 with debug functionality" OFF)

if(EOC2_DEBUG)
  add_definitions(-DEOC2_DEBUG)
endif()

find_package(Protobuf REQUIRED)

# Alpine protobuf packages (v6+) are built against Abseil shared libs.
# Link Abseil DSOs explicitly to avoid "DSO missing from command line".
find_library(ABSL_CHECK_OP_LIB NAMES absl_log_internal_check_op PATHS /usr/lib /usr/local/lib)
find_library(ABSL_LOG_INTERNAL_MESSAGE_LIB NAMES absl_log_internal_message PATHS /usr/lib /usr/local/lib)
find_library(ABSL_LOG_INTERNAL_NULLGUARD_LIB NAMES absl_log_internal_nullguard PATHS /usr/lib /usr/local/lib)

include_directories(${PROTOBUF_INCLUDE_DIRS})
include_directories(${CMAKE_CURRENT_BINARY_DIR})

# IMPORTANT:
# Do NOT glob all protos; some of these files duplicate message definitions.
# Only list the "root" protos you want to compile.
set(ACII_PROTO_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../buffers/src/main/proto")
set(ProtoFiles
  "${ACII_PROTO_DIR}/Acii.proto"
  "${ACII_PROTO_DIR}/Iwp.proto"
)

foreach(p IN LISTS ProtoFiles)
  if(NOT EXISTS "${p}")
    message(FATAL_ERROR "Missing proto file: ${p}. ACII expects protobuf definitions from ../../buffers/src/main/proto.")
  endif()
endforeach()

protobuf_generate_cpp(PROTO_SRCS PROTO_HDRS ${ProtoFiles})

find_package(CURL REQUIRED)
include_directories(${CURL_INCLUDE_DIR})

find_package(OpenCV REQUIRED)
include_directories(${OpenCV_INCLUDE_DIRS})

set(LOG4CXX_DIR /usr CACHE PATH "Path to Log4cxx")
file(GLOB LOG4CXX_INCLUDE ${LOG4CXX_DIR}/include/log4cxx)
include_directories(${LOG4CXX_INCLUDE})
link_directories(${LOG4CXX_DIR}/lib)

set(AMQCPP_DIR /usr CACHE PATH "Path to AmqCpp")
file(GLOB AMQCPP_INCLUDE ${AMQCPP_DIR}/include/activemq-cpp-*)
include_directories(${AMQCPP_INCLUDE})
link_directories(${AMQCPP_DIR}/lib)

include_directories(include)
file(GLOB SRC src/*)
file(GLOB HEADERS include/*)

add_executable(aciiComponent
  aciiComponent.cpp
  ${SRC} ${HEADERS}
  ${PROTO_SRCS} ${PROTO_HDRS}
)

set(ACII_PROTOBUF_LIB "")
if(TARGET protobuf::libprotobuf)
  set(ACII_PROTOBUF_LIB protobuf::libprotobuf)
elseif(TARGET Protobuf::libprotobuf)
  set(ACII_PROTOBUF_LIB Protobuf::libprotobuf)
else()
  set(ACII_PROTOBUF_LIB ${PROTOBUF_LIBRARIES})
endif()

target_link_libraries(aciiComponent PRIVATE
  log4cxx
  activemq-cpp
  ${OpenCV_LIBS}
  ${CURL_LIBRARIES}
  ${ACII_PROTOBUF_LIB}
)

if(ABSL_CHECK_OP_LIB)
  target_link_libraries(aciiComponent PRIVATE ${ABSL_CHECK_OP_LIB})
endif()
if(ABSL_LOG_INTERNAL_MESSAGE_LIB)
  target_link_libraries(aciiComponent PRIVATE ${ABSL_LOG_INTERNAL_MESSAGE_LIB})
endif()
if(ABSL_LOG_INTERNAL_NULLGUARD_LIB)
  target_link_libraries(aciiComponent PRIVATE ${ABSL_LOG_INTERNAL_NULLGUARD_LIB})
endif()

install(FILES ${CMAKE_CURRENT_SOURCE_DIR}/config/acii.conf   DESTINATION /etc/iwp-acii)
install(FILES ${CMAKE_CURRENT_SOURCE_DIR}/config/conf.ini    DESTINATION /etc/iwp-acii)
install(FILES ${CMAKE_CURRENT_SOURCE_DIR}/config/logger.conf DESTINATION /etc/iwp-acii)

install(DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/trainedClassifiers
  DESTINATION ${CMAKE_INSTALL_PREFIX}/share/acii/acii-client
)

install(TARGETS aciiComponent DESTINATION ${CMAKE_INSTALL_PREFIX}/bin)
