/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

#ifndef ACII_CLIENT_INCLUDE_CONFIGPARSER_H_
#define ACII_CLIENT_INCLUDE_CONFIGPARSER_H_

#include <string>
#include <map>
#include <vector>

using std::map;
using std::string;
using std::vector;

class ConfigParser
{
public:
  ConfigParser();

  void strip(string &s);

  bool parseParams(string &config_file, map<string, string> &params);

  void parseDelimitedString(vector<string> &delimited, string &s, char delim);
};

#endif // ACII_CLIENT_INCLUDE_CONFIGPARSER_H_
