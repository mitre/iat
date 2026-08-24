/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

#ifndef ACII_CLIENT_INCLUDE_CLIENT_H_
#define ACII_CLIENT_INCLUDE_CLIENT_H_

#include <opencv/cv.h>
#include <curl/curl.h>

#include <activemq/library/ActiveMQCPP.h>
#include <activemq/transport/DefaultTransportListener.h>
#include <activemq/util/Config.h>

#include <cms/Connection.h>
#include <cms/ExceptionListener.h>
#include <cms/MessageListener.h>
#include <cms/Session.h>
#include <cms/TextMessage.h>

#include <log4cxx/logger.h>
#include <log4cxx/logmanager.h>
#include <log4cxx/propertyconfigurator.h>
#include <log4cxx/rollingfileappender.h>
#include <log4cxx/simplelayout.h>

#include <cstdio>
#include <string>
#include <vector>

#include "EOC2Manager.h"

using activemq::transport::DefaultTransportListener;
using cms::BytesMessage;
using cms::CMSException;
using cms::Connection;
using cms::DeliveryMode;
using cms::Destination;
using cms::ExceptionListener;
using cms::Message;
using cms::MessageConsumer;
using cms::MessageListener;
using cms::MessageProducer;
using cms::Session;
using EOC2::EOC2Manager;
using std::string;
using std::vector;

class Client : public MessageListener, public ExceptionListener, public DefaultTransportListener
{

private:
       Session *session;
       Connection *connection;
       Destination *request_dest;
       Destination *response_dest;
       MessageConsumer *consumer;
       MessageProducer *producer;
       string broker_uri;
       string artemis_user;
       string artemis_password;
       string requests_dest_name;
       string response_dest_name;
       string output_path;
       EOC2Manager *manager;
       vector<string> allowed_img_extensions;

       Client &operator=(const Client &);
       void cleanup();
       string getImage(string id, string url,
                       const string bytes);

public:
       ~Client();
       Client(const string &broker_uri,
              const string &artemis_user,
              const string &artemis_password,
              const string &requests_dest_name,
              const string &response_dest_name,
              string &eye_class_config_path,
              const string &output_path,
              vector<string> allowed_img_extensions,
              bool keep_class_in_mem = true);

       Client(const string &broker_uri,
              const string &artemis_user,
              const string &artemis_password,
              const string &requests_dest_name,
              const string &response_dest_name,
              string &eye_class_config_path,
              vector<string> allowed_img_extensions,
              bool keep_class_in_mem = true);

       void runRequestConsumer();
       void onMessage(const Message *message);
       void onException(const CMSException &ex);
       void transportInterrupted();
       void transportResumed();
       bool imgAllowed(string &imgExt);
       static size_t write_data(void *contents, size_t size, size_t nmemb,
                                FILE *stream);
};

#endif // ACII_CLIENT_INCLUDE_CLIENT_H_
