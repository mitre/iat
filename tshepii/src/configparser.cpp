// #######################################################################
// NOTICE
// 
// This software (or technical data) was produced for the U. S. Government
// and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
// (May 2014) – Alternative IV (Dec 2007)
// 
// (c) 2024 The MITRE Corporation. All Rights Reserved.
// #######################################################################

#include <locale>
#include <iostream>
#include <fstream>
#include <vector>
#include <sstream>
#include <map>
#include <string>
#include "configparser.h"

using std::getline;
using std::ifstream;
using std::isspace;
using std::locale;
using std::map;
using std::string;
using std::stringstream;
using std::vector;

ConfigParser::ConfigParser() {}

// Remove whitespace from front and back of given string
void ConfigParser::strip(string &s)
{
  locale loc;
  int front = 0;
  int end = s.length() - 1;
  while (isspace(s[front], loc))
  {
    front++;
  }
  while (isspace(s[end], loc))
  {
    end--;
  }
  s = s.substr(front, end - front + 1);
}

// Returns true on sucess, false if config cannot be opened
bool ConfigParser::parseParams(string &config_file, map<string,
                                                        string> &params)
{
  ifstream file;
  string line;
  string prop;
  string value;
  int eqIndex;
  file.open(config_file.c_str());
  if (!file.is_open())
  {
    return false;
  }
  else
  {
    while (getline(file, line))
    {
      if (line.empty())
      {
        continue;
      }
      // Remove all to the right of '#' (treated as comment)
      line = line.substr(0, line.find("#"));
      // Use '=' as delimiter separating prop and value
      eqIndex = line.find("=");
      prop = line.substr(0, eqIndex);
      value = line.substr(eqIndex + 1, line.length());
      strip(prop);
      strip(value);
      params[prop] = value;
    }
  }
  return true;
}

void ConfigParser::parseDelimitedString(vector<string> &delimited,
                                        string &s, char delim)
{
  stringstream ss(s);
  while (ss.good())
  {
    string substr;
    getline(ss, substr, delim);
    delimited.push_back(substr);
  }
}
