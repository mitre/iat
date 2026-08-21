// #######################################################################
// NOTICE
// 
// This software (or technical data) was produced for the U. S. Government
// and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
// (May 2014) – Alternative IV (Dec 2007)
// 
// (c) 2024 The MITRE Corporation. All Rights Reserved.
// #######################################################################

#include "Client.h"
#include <curl/curl.h>
#include <activemq/core/ActiveMQConnectionFactory.h>
#include <log4cxx/logger.h>
#include <map>
#include <cstdio>
#include <cstring>
#include <string>
#include <vector>
#include <thread>

#include "Acii.pb.h"
#include "Iwp.pb.h"

using activemq::core::ActiveMQConnectionFactory;
using log4cxx::Logger;
using org::mitre::iwp::buffers::AciiServiceResponse;
using org::mitre::iwp::buffers::HorizontalAlignment;
using org::mitre::iwp::buffers::ImageServiceQuery;
using org::mitre::iwp::buffers::VerticalAlignment;
using std::exception;
using std::find;
using std::string;
using std::transform;
using std::unique_ptr;
using std::vector;

const extern log4cxx::LoggerPtr logger =
    Logger::getLogger("acii.client");

#define CURL_STATICLIB

void Client::cleanup()
{
  try
  {
    if (connection != NULL)
    {
      LOG4CXX_INFO(logger, "Connection found, closing the existing conneciton!");
      connection->close();
    }
  }
  catch (CMSException &e)
  {
    LOG4CXX_ERROR(logger, "Error while closing connection: " << e.what());
    e.printStackTrace();
  }

  delete request_dest;
  request_dest = NULL;
  delete response_dest;
  response_dest = NULL;
  delete consumer;
  consumer = NULL;
  delete producer;
  producer = NULL;
  delete session;
  session = NULL;
  delete connection;
  connection = NULL;
  delete manager;
  manager = NULL;

  LOG4CXX_INFO(logger, "Connection resources destroyed");
}

Client::~Client() { this->cleanup(); }

Client::Client(const string &broker_uri,
               const string &artemis_user,
               const string &artemis_password,
               const string &requests_dest_name,
               const string &response_dest_name,
               string &eye_class_config_path,
               const string &output_path,
               vector<string> allowed_img_extensions,
               bool keep_class_in_mem)
{

  this->connection = NULL;
  this->session = NULL;
  this->request_dest = NULL;
  this->response_dest = NULL;
  this->consumer = NULL;
  this->producer = NULL;
  this->broker_uri = broker_uri;
  this->artemis_user = artemis_user;
  this->artemis_password = artemis_password;
  this->requests_dest_name = requests_dest_name;
  this->response_dest_name = response_dest_name;
  this->output_path = output_path;
  this->allowed_img_extensions = allowed_img_extensions;

  try
  {
    manager = new EOC2Manager(keep_class_in_mem);
    manager->loadConfiguration(eye_class_config_path);
    manager->showConfiguration();
  }
  catch (exception &e)
  {
    LOG4CXX_ERROR(logger, "Error while loading config: " << e.what() << ". Exiting.");
    exit(1);
  }
  catch (...)
  {
    LOG4CXX_ERROR(logger, "Error while loading config. Exiting.");
    exit(1);
  }
}

// Use this for iris-workstation integration
Client::Client(const string &broker_uri,
               const string &artemis_user,
               const string &artemis_password,
               const string &requests_dest_name,
               const string &response_dest_name,
               string &eye_class_config_path,
               vector<string> allowed_img_extensions,
               bool keep_class_in_mem)
{

  GOOGLE_PROTOBUF_VERIFY_VERSION;

  this->broker_uri = broker_uri;
  this->artemis_user = artemis_user;
  this->artemis_password = artemis_password;
  this->requests_dest_name = requests_dest_name;
  this->response_dest_name = response_dest_name;
  this->allowed_img_extensions = allowed_img_extensions;

  this->connection = NULL;
  this->session = NULL;
  this->request_dest = NULL;
  this->consumer = NULL;
  this->producer = NULL;

  try
  {
    manager = new EOC2Manager(keep_class_in_mem);
    manager->loadConfiguration(eye_class_config_path);
    manager->showConfiguration();
  }
  catch (exception &e)
  {
    LOG4CXX_ERROR(logger, "Error while loading config: " << e.what() << ". Exiting.");
    exit(1);
  }
  catch (...)
  {
    LOG4CXX_ERROR(logger, "Error while loading config. Exiting.");
    exit(1);
  }
}

void Client::runRequestConsumer()
{
  int attempts = 0;
  const int max_attempts = 5;
  bool connected = false;
  ActiveMQConnectionFactory *connection_factory = nullptr;

  while (attempts < max_attempts && !connected) {
    try
      {
        LOG4CXX_INFO(logger, "Initializing client attempt #" << attempts + 1 << " out of: " << max_attempts);

        // obtain new ConnectionFactory
        connection_factory = new ActiveMQConnectionFactory(broker_uri);
        connection = connection_factory->createConnection(artemis_user, artemis_password);

        connected = true;
        LOG4CXX_INFO(logger, "Successfully connected to ActiveMQ broker");
      }
      catch (CMSException e)
      {
        attempts++;
        LOG4CXX_WARN(logger, "Connection attempt #" << attempts << " failed. Error: " << e.what());
                
        // Clean up the factory if it was allocated but connection failed
        if (connection_factory) {
            delete connection_factory;
            connection_factory = nullptr;
        }

        if (attempts < max_attempts) {
            LOG4CXX_INFO(logger, "Retrying in 5 seconds...");
            std::this_thread::sleep_for(std::chrono::seconds(5));
        } else {
            LOG4CXX_ERROR(logger, "Reached maximum connection attempts. Exiting.");
            LOG4CXX_ERROR(logger, "Make sure the ActiveMQ server is accessible at " << broker_uri);
            exit(1);
        }
      }
  }
  
  try
  {
    // start connection now that consumers created
    connection->start();
    connection->setExceptionListener(this);

    // session = connection->createSession(Session::AUTO_ACKNOWLEDGE);
    session = connection->createSession(Session::DUPS_OK_ACKNOWLEDGE);
    request_dest = session->createQueue(requests_dest_name);
    response_dest = session->createQueue(response_dest_name);
    // Setup a producer to put messages on IWP response queue
    producer = session->createProducer(response_dest);
    producer->setDeliveryMode(DeliveryMode::NON_PERSISTENT);

    LOG4CXX_DEBUG(logger, "Using request queue: " << requests_dest_name);
    LOG4CXX_DEBUG(logger, "Using response queue: " << response_dest_name);

    // Setup a consumer to consume messages off IWP requst queue
    consumer = session->createConsumer(request_dest);
    // Asynchronous MessageListener consumption
    consumer->setMessageListener(this);

    delete connection_factory; // connection created, delete connection_factory
    LOG4CXX_INFO(logger, "Finished client initialization");
  }
  catch (CMSException e)
  {
    LOG4CXX_ERROR(logger, "Error occured during client initialization: "
                          "CMSException - "
                              << e.getCause()->what() << ". Exiting.");
    LOG4CXX_ERROR(logger, "Make sure the ActiveMQ server is accessible at " << broker_uri);
    e.printStackTrace();
    exit(1);
  }
}

void Client::onMessage(const Message *message)
{
  try
  {
    LOG4CXX_INFO(logger, "Message received");
    vector<string> message_properties = message->getPropertyNames();

    const BytesMessage *msg = dynamic_cast<const BytesMessage *>(message);
    if (!msg)
    {
      LOG4CXX_ERROR(logger, "Incorrect message type received from "
                            "ActiveMQ, aborting");
      return;
    }
    LOG4CXX_INFO(logger, "Message body length: " << msg->getBodyLength());

    // request
    ImageServiceQuery acii_query;

    AciiServiceResponse acii_serviceresponse;

    if (acii_query.ParseFromArray(msg->getBodyBytes(), msg->getBodyLength()))
    {
      LOG4CXX_INFO(logger, "Processing message...");
      if (!acii_query.path().empty())
      {
        LOG4CXX_DEBUG(logger, "Received image URL " << acii_query.path());
      }
      LOG4CXX_INFO(logger, "ActiveMQ Message ID " << acii_query.imageid());

      acii_serviceresponse.set_imageid(acii_query.imageid());

      string image_path = Client::getImage(acii_query.imageid(), acii_query.path(),
                                           acii_query.imagebytes());
      int image_type = acii_query.imagetype();
      int image_width = acii_query.width();
      int image_height = acii_query.height();
      bool imgAllowed = true;

      if (!image_path.empty())
      {
        int up_down = -1;
        int left_right = -1;

        manager->run(image_path, up_down, left_right, image_type, image_width,
                     image_height);
        if (up_down == -1 || left_right == -1)
        {
          acii_serviceresponse.set_vertical(VerticalAlignment::UnknownVertical);
          acii_serviceresponse.set_horizontal(HorizontalAlignment::UnknownHorizontal);
        }
        else
        {
          switch (up_down)
          {
          case 0:
            acii_serviceresponse.set_vertical(VerticalAlignment::Up);
            break;
          case 1:
            acii_serviceresponse.set_vertical(VerticalAlignment::Down);
            break;
          default:
            acii_serviceresponse.set_vertical(VerticalAlignment::UnknownVertical);
          }

          switch (left_right)
          {
          case 0:
            acii_serviceresponse.set_horizontal(HorizontalAlignment::Left);
            break;
          case 1:
            acii_serviceresponse.set_horizontal(HorizontalAlignment::Right);
            break;
          default:
            acii_serviceresponse.set_horizontal(
                HorizontalAlignment::UnknownHorizontal);
          }
        }

        string data;
      }
      else
      {
        LOG4CXX_ERROR(logger, "Image not accepted. Responding with: ERROR");
        acii_serviceresponse.set_vertical(VerticalAlignment::UnknownVertical);
        acii_serviceresponse.set_horizontal(HorizontalAlignment::UnknownHorizontal);
      }

      if (!image_path.empty())
      {
        remove(image_path.c_str()); // remove temp file
      }
    }
    else
    {
      LOG4CXX_ERROR(logger, "Error occured parsing acii_query");
    }

    LOG4CXX_INFO(logger, "Processing response...");

    // ByteSize() const' is deprecated: Please use ByteSizeLong()
    size_t reply_size = acii_serviceresponse.ByteSizeLong();

    unsigned char reply_bytes[reply_size];
    acii_serviceresponse.SerializeToArray(reply_bytes, reply_size);

    unique_ptr<BytesMessage> response(
        session->createBytesMessage(reply_bytes, reply_size));
    producer->send(response.get());

    LOG4CXX_INFO(logger, "Response sent "
                         "for ID: "
                             << acii_query.imageid());

    LOG4CXX_INFO(logger, "Session committed ...well not anymore");
  }
  catch (CMSException &e)
  {
    LOG4CXX_ERROR(logger, "Error occured during processing: " << e.what());
    e.printStackTrace();
  }
  catch (exception &e)
  {
    LOG4CXX_ERROR(logger, "Error occured during processing: " << e.what());
  }
  catch (...)
  {
    LOG4CXX_ERROR(logger, "An unknown error occured during processing");
  }
}

void Client::onException(const CMSException &ex)
{
  LOG4CXX_ERROR(logger, "Exception occured: " << ex.what() << ". Exiting.");
  exit(1);
}

void Client::transportInterrupted()
{
  LOG4CXX_WARN(logger, "Transport interupted");
}

void Client::transportResumed()
{
  LOG4CXX_WARN(logger, "Transport resumed");
}

bool Client::imgAllowed(string &img_ext)
{
  bool ret = find(allowed_img_extensions.begin(),
                  allowed_img_extensions.end(), img_ext) != allowed_img_extensions.end();
  if (!ret)
  {
    LOG4CXX_ERROR(logger, "Image type '" << img_ext << "' not allowed");
  }
  return ret;
}

string Client::getImage(string id, string url_path,
                        const string bytes)
{
  CURL *curl;
  FILE *fp;
  CURLcode res;

  string str_path = this->output_path + id;

  if (!url_path.empty())
  {
    LOG4CXX_INFO(logger, "URL - " << url_path);
  }
  LOG4CXX_INFO(logger, "Output file name " << str_path);

  string local_path = "";

  try
  {
    curl = curl_easy_init();
    if (curl)
    {
      fp = fopen(str_path.c_str(), "wb");
      if (fp)
      {
        if (url_path.length())
        {
          curl_easy_setopt(curl, CURLOPT_URL, url_path.c_str());
          curl_easy_setopt(curl, CURLOPT_HEADER, false);
          curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, Client::write_data);
          curl_easy_setopt(curl, CURLOPT_WRITEDATA, fp);
          curl_easy_setopt(curl, CURLOPT_VERBOSE, 1L);
          res = curl_easy_perform(curl);
          if (res != CURLE_OK)
          {
            LOG4CXX_ERROR(logger, "curl_easy_perform() failed: %s\n"
                                      << curl_easy_strerror(res) << "\n");
          }
          curl_easy_cleanup(curl);
        }
        else
        {
          fwrite(bytes.c_str(), sizeof(char), bytes.length(), fp);
        }
        fclose(fp);
      }
      else
      {
        LOG4CXX_ERROR(logger, "Unable to open output file " << str_path);
        return "";
      }
      local_path = str_path;
    }
    else
    {
      LOG4CXX_ERROR(logger, "Error initialize curl object");
    }
  }
  catch (exception &e)
  {
    LOG4CXX_ERROR(logger, "Error downloading image to local " << e.what());
  }

  return local_path;
}

size_t Client::write_data(void *contents, size_t size, size_t nmemb,
                          FILE *stream)
{
  size_t written;
  written = fwrite(contents, size, nmemb, stream);

  return written;
}
