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
#include "CryptDetector.hpp"
#include "Iris.hpp"
#include "Configuration.hpp"
#include "configparser.h"
#include "CryptMatcher.hpp"
#include <curl/curl.h>
#include <activemq/core/ActiveMQConnectionFactory.h>
#include <log4cxx/logger.h>
#include <map>
#include <chrono>
#include <cstdio>
#include <cstring>
#include <string>
#include <thread>
#include <vector>
#include <sys/stat.h>

using activemq::core::ActiveMQConnectionFactory;
using log4cxx::Logger;
using org::mitre::iwp::buffers::Crypt;
using org::mitre::iwp::buffers::ImageServiceQuery;
using org::mitre::iwp::buffers::TCircle;
using org::mitre::iwp::buffers::TPoint;
using org::mitre::iwp::buffers::TShepiiCompareResponse;
using org::mitre::iwp::buffers::TShepiiResponse;
using std::exception;
using std::find;
using std::string;
using std::transform;
using std::unique_ptr;
using std::vector;
using std::chrono::seconds;
using std::this_thread::sleep_for;

const extern log4cxx::LoggerPtr logger =
    Logger::getLogger("org.MITRE.cryptDectector.client");

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
  LOG4CXX_INFO(logger, "Connection resources destroyed");
}

Client::~Client() { this->cleanup(); }

Client::Client(const string &broker_uri,
               const string &artemis_user,
               const string &artemis_password,
               const string &requests_dest_name,
               const string &response_dest_name,
               const string &output_path,
               const vector<string> allowed_img_extensions)
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
}

// Use this for iris-workstation integration
Client::Client(const string &broker_uri,
               const string &artemis_user,
               const string &artemis_password,
               const string &requests_dest_name,
               const string &response_dest_name,
               const vector<string> allowed_img_extensions)
{

  GOOGLE_PROTOBUF_VERIFY_VERSION;

  this->broker_uri = broker_uri;
  this->artemis_user = artemis_user;
  this->artemis_password = artemis_password;
  this->requests_dest_name = requests_dest_name;
  this->allowed_img_extensions = allowed_img_extensions;

  this->connection = NULL;
  this->session = NULL;
  this->request_dest = NULL;
  this->response_dest = NULL;
  this->consumer = NULL;
  this->producer = NULL;
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
    LOG4CXX_ERROR(logger, "Error occured during client initialization: " << e.what() << ". Exiting.");
    e.printStackTrace();
    exit(1);
  }
}

void Client::handle_crypt_query(TShepiiResponse *crypt_response, ImageServiceQuery *query)
{
  LOG4CXX_INFO(logger, "Processing message...");
  if (!query->path().empty())
  {
    LOG4CXX_DEBUG(logger, "Received image URL " << query->path());
  }
  LOG4CXX_INFO(logger, "Message ID " << query->imageid());

  crypt_response->set_imageid(query->imageid());

  string image_path = Client::getImage(query->imageid(), query->path(),
                                       query->imagebytes());
  int image_type = query->imagetype();
  int image_width = query->width();
  int image_height = query->height();

  LOG4CXX_INFO(logger, "Analyzing " << image_path);
  Iris *iris;
  if (image_type == ImageServiceQuery::PIXMAP)
  {
    iris = new Iris(image_path, image_width, image_height);
  }
  else
  {
    iris = new Iris(image_path);
  }

  iris->osirisPreProcess();
  Mat inputIris = iris->getGray3CCrop().clone();
  Mat inputIrisMask = iris->getCropMask().clone();
  Mat outputIris = iris->getGray3CCrop().clone();
  Mat originalIris = iris->getColorCrop().clone();

  CryptDetector *detector = new CryptDetector(
      &inputIris, &inputIrisMask, &outputIris, &originalIris,
      iris->getCropRadius(), iris->getCropCenter().x, iris->getCropCenter().y,
      iris->getCropPupilRadius(), iris->getCropCenter().x, iris->getCropCenter().y,
      Configuration::CRYPTS_COLOR);

  detector->detectCrypts();

  if (!image_path.empty())
  {
    remove(image_path.c_str()); // remove temp file
  }

  this->initCircle(
      crypt_response->mutable_cropiris(),
      iris->getCropCenter().x,
      iris->getCropCenter().y,
      iris->getCropRadius());

  this->initCircle(
      crypt_response->mutable_croppupil(),
      iris->getCropPupilCenter().x,
      iris->getCropPupilCenter().y,
      iris->getCropPupilRadius());

  this->initCircle(
      crypt_response->mutable_originaliris(),
      iris->getOriginalIrisCenter().x,
      iris->getOriginalIrisCenter().y,
      iris->getOriginalIrisRadius());

  this->initCircle(
      crypt_response->mutable_originalpupil(),
      iris->getOriginalPupilCenter().x,
      iris->getOriginalPupilCenter().y,
      iris->getOriginalPupilRadius());

  for (auto const &it : *(detector->getSortedKeypoints()))
  {
    TPoint *k = crypt_response->add_keypoints();
    k->set_x(it.pt.x);
    k->set_y(it.pt.y);
    k->set_size(it.size);
  }
  for (auto const &crypt : detector->crypts)
  {
    Crypt *c = crypt_response->add_crypts();
    for (auto const &point : crypt)
    {
      TPoint *p = c->add_points();
      p->set_x(point.x);
      p->set_y(point.y);
    }
  }
  crypt_response->set_inputimagewidth(inputIris.cols);
  crypt_response->set_inputimageheight(inputIris.rows);
}

void Client::handle_compare_query(TShepiiCompareResponse *reply, TShepiiCompareRequest *query)
{
  TShepiiResponse *response = query->mutable_response1();
  TShepiiResponse *response2 = query->mutable_response2();
  CryptMatcher *matcher = new CryptMatcher(response, response2);
  matcher->detectDescribeAndMatch();
  reply->mutable_cryptresponse()->CopyFrom(*response);
  reply->mutable_cryptresponse2()->CopyFrom(*response2);
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

    unsigned char *request_bytes;
    int request_length;
    ImageServiceQuery crypt_query;
    TShepiiCompareRequest compare_query;

    TShepiiResponse crypt_reply;
    TShepiiCompareResponse compare_reply;

    LOG4CXX_INFO(logger, "Message ID " << crypt_query.imageid());

    request_bytes = msg->getBodyBytes();
    request_length = msg->getBodyLength();

    bool isCryptReply = true;

    if (compare_query.ParseFromArray(request_bytes, request_length))
    {
      LOG4CXX_INFO(logger, "Received compare request");
      isCryptReply = false;
      this->handle_compare_query(&compare_reply, &compare_query);
    }
    else if (crypt_query.ParseFromArray(request_bytes, request_length))
    {
      isCryptReply = true;
      LOG4CXX_INFO(logger, "Received crypt request");
      this->handle_crypt_query(&crypt_reply, &crypt_query);
    }
    else
    {
      LOG4CXX_ERROR(logger, "Error occured parsing the request");
      return;
    }

    // Copy header info to response
    // ByteSize() const' is deprecated: Please use ByteSizeLong()
    size_t crypt_reply_size = crypt_reply.ByteSizeLong();
    size_t compare_reply_size = compare_reply.ByteSizeLong();

    size_t reply_size = crypt_reply_size;

    if (isCryptReply)
    {
      reply_size = crypt_reply_size;
    }
    else
    {
      reply_size = compare_reply_size;
    }

    unsigned char reply_bytes[reply_size];

    if (isCryptReply)
    {
      crypt_reply.SerializeToArray(reply_bytes, reply_size);
      LOG4CXX_INFO(logger, "Sending Crypt Response");
    }
    else
    {
      compare_reply.SerializeToArray(reply_bytes, reply_size);
      LOG4CXX_INFO(logger, "Sending Compare Response");
    }

    LOG4CXX_INFO(logger, "Reply size: " << reply_size);

    unique_ptr<BytesMessage> response(
        session->createBytesMessage(reply_bytes, reply_size));
    LOG4CXX_INFO(logger, "Response sent: " << reply_bytes);
    producer->send(response.get());

    LOG4CXX_INFO(logger, "Response sent for ID: " << crypt_query.imageid());
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

void Client::initCircle(TCircle *circle, double x, double y, double radius)
{
  TPoint *center = circle->mutable_center();
  center->set_x(x);
  center->set_y(y);
  circle->set_radius(radius);
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

void Client::loadImage(const string &rFilename, Mat **image)
{
  try
  {
    if (!*image)
    {
      LOG4CXX_ERROR(logger, "Cannot load image : " << rFilename);
    }
  }
  catch (exception &e)
  {
    LOG4CXX_ERROR(logger, e.what());
  }
}

void Client::loadPixmap(const string &rFilename, Mat **image, int imageHeight,
                        int imageWidth)
{

  int channels;
  Mat mat;
  FILE *fp = fopen(rFilename.c_str(), "rb");
  if (!fp)
  {
    LOG4CXX_ERROR(logger, "Cannot load image : " << rFilename);
    return;
  }
  // Determine the number of channels
  fseek(fp, 0, SEEK_END);
  channels = ftell(fp) / imageHeight / imageWidth;
  fseek(fp, 0, SEEK_SET);
  if (channels == 3)
  {
    mat = Mat::zeros(imageHeight, imageWidth, CV_8UC3);
    fread(mat.data, 3, imageWidth * imageHeight, fp);
  }
  else
  {
    mat = Mat::zeros(imageHeight, imageWidth, CV_8UC1);
    fread(mat.data, 1, imageWidth * imageHeight, fp);
  }
  *image = &mat;
  fclose(fp);
}

/**
 * Determines whether a file or directory exists and is readable.
 *
 * @param filename The path to the desired file or directory.
 * @return true if it exists and is readable, false otherwise.
 */
bool Client::fileExists(const string &filename)
{
  struct stat info;
  if (stat(filename.c_str(), &info))
    return false;
#ifdef _WIN32
  return (bool)(info.st_mode);
#else
  return (bool)(info.st_mode & (S_IXOTH | S_IROTH));
#endif
}

int main(int argc, char **argv)
{
  if (argc < 3)
  {
    std::cout << "Usage " << argv[0] << " <TSHEPII_CONFIG> <LOG4CXX_CONFIG>"
              << std::endl;
    return 0;
  }

  if (!Client::fileExists(argv[1]))
  {
    LOG4CXX_ERROR(logger, "Error loading tshepii component config. Missing "
                          "file. Exiting.");
    return -1;
  }
  if (!Client::fileExists(argv[2]))
  {
    LOG4CXX_ERROR(logger, "Error loading logger config. Missing file. "
                          "Exiting.");
    return -1;
  }

  ConfigParser cp;
  string config_file(argv[1]);
  map<string, string> params;
  bool success = cp.parseParams(config_file, params);
  if (!success)
  {
    LOG4CXX_ERROR(logger, "Error loading tshepii component config. Exiting.");
    return 1;
  }

  // Assume alowed img extensions are contained in comma-delimited string
  char delim = ',';
  vector<string> allowed_img_extensions;
  cp.parseDelimitedString(
      allowed_img_extensions, params["allowed-img-exts"], delim);

  Client c = Client(params["broker-URI"],
                    params["artemis-user"],
                    params["artemis-password"],
                    params["tshepii-request-queue"],
                    params["tshepii-response-queue"],
                    params["tshepii-output-path"],
                    allowed_img_extensions);

  if (Client::fileExists(argv[2]))
  {
    log4cxx::PropertyConfigurator::configure(argv[2]);
  }
  log4cxx::LoggerPtr logger = log4cxx::Logger::getRootLogger();
  log4cxx::helpers::Pool p;

  activemq::library::ActiveMQCPP::initializeLibrary();
  vector<string> extensions = {"PNG"};
  c.runRequestConsumer();

  while (true)
  {
    sleep_for(seconds(1));
  }

  activemq::library::ActiveMQCPP::shutdownLibrary();
  return 0;
}
