// #######################################################################
// NOTICE
// 
// This software (or technical data) was produced for the U. S. Government
// and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
// (May 2014) – Alternative IV (Dec 2007)
// 
// (c) 2024 The MITRE Corporation. All Rights Reserved.
// #######################################################################

#define _GLIBCXX_USE_NANOSLEEP 1

#include <activemq/library/ActiveMQCPP.h>
#include <activemq/core/ActiveMQConnectionFactory.h>

#include <log4cxx/propertyconfigurator.h>
#include <log4cxx/logger.h>
#include <log4cxx/logmanager.h>
#include <log4cxx/rollingfileappender.h>
#include <log4cxx/simplelayout.h>

#include <algorithm>
#include <iostream>
#include <string>
#include <chrono>
#include <thread>
#include <map>
#include <vector>

#include "include/Client.h"
#include "include/configparser.h"

using std::cout;
using std::endl;
using std::map;
using std::string;
using std::transform;
using std::vector;
using std::chrono::seconds;
using std::this_thread::sleep_for;

int main(int argc, char *argv[])
{
    static vector<string> required_params = {"broker-URI",
                                             "artemis-user",
                                             "artemis-password",
                                             "acii-request-queue",
                                             "acii-response-queue",
                                             "classifier-config",
                                             "allowed-img-exts"};

    if (argc < 4)
    {
        cout << "Usage: aciiComponent";
        cout << " <Component Config>";
        cout << " <LOG4CXX Config>";
        cout << " <Keep classifier in memory(true|false)>";
        // Optional. If not set, defaults to file in log4cxx config
        cout << endl;
        return 0;
    }

    string component_config(argv[1]);
    string log4cxx_config(argv[2]);
    string keep_class_in_mem_str(argv[3]);

    log4cxx::PropertyConfigurator::configure(log4cxx_config);

    // Set file of log to
    if (argc > 4)
    {
        log4cxx::LoggerPtr logger = log4cxx::Logger::getRootLogger();
        log4cxx::FileAppenderPtr appender = logger->getAppender("R");
        log4cxx::helpers::Pool p;
        appender->setFile(argv[4]);
        appender->activateOptions(p);
    }

    log4cxx::LoggerPtr logger =
        log4cxx::Logger::getLogger("acii.main");


    LOG4CXX_INFO(logger, "Starting aciiComponent");
    LOG4CXX_INFO(logger, "Component config: " << component_config);
    LOG4CXX_INFO(logger, "Log4cxx config: " << log4cxx_config);

    ConfigParser cp;
    map<string, string> params;
    bool success = cp.parseParams(component_config, params);
    if (!success)
    {
        LOG4CXX_ERROR(logger, "Error loading acii component config. Exiting.");
        return 1;
    }

    // Check for required config parameters
    for (int i = 0; i < required_params.size(); ++i)
    {
        if (params.find(required_params[i]) == params.end())
        {
            LOG4CXX_ERROR(logger,
                          "Component config does not contain all required parameters");
            return 1;
        }
    }

    // Assume alowed img extensions are contained in comma-delimited string
    char delim = ',';
    vector<string> allowed_img_extensions;
    cp.parseDelimitedString(
        allowed_img_extensions, params["allowed-img-exts"], delim);

    // Convert string-booleans to bool
    transform(keep_class_in_mem_str.begin(), keep_class_in_mem_str.end(),
              keep_class_in_mem_str.begin(), ::tolower);
    bool keep_class_in_mem = (keep_class_in_mem_str == "false") ? false : true;

    activemq::library::ActiveMQCPP::initializeLibrary();

    LOG4CXX_INFO(logger, "Creating Client");

    Client c = Client(params["broker-URI"],
                      params["artemis-user"],
                      params["artemis-password"],
                      params["acii-request-queue"],
                      params["acii-response-queue"],
                      params["classifier-config"],
                      params["acii-output-path"],
                      allowed_img_extensions,
                      keep_class_in_mem);

    c.runRequestConsumer();

    while (true)
    {
        sleep_for(seconds(1));
    }

    activemq::library::ActiveMQCPP::shutdownLibrary();
    return 0;
}
