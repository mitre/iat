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
#include <cstdlib>
#include "configparser.h"

using std::getline;
using std::ifstream;
using std::isspace;
using std::locale;
using std::map;
using std::string;
using std::stringstream;
using std::vector;

namespace {
string expandEnvVars(const string &value)
{
  string expanded;
  string::size_type pos = 0;

  while (pos < value.length())
  {
    string::size_type start = value.find("${", pos);
    if (start == string::npos)
    {
      expanded.append(value.substr(pos));
      break;
    }

    expanded.append(value.substr(pos, start - pos));
    string::size_type end = value.find("}", start + 2);
    if (end == string::npos)
    {
      expanded.append(value.substr(start));
      break;
    }

    string env_var = value.substr(start + 2, end - start - 2);
    const char *env_value = std::getenv(env_var.c_str());
    if (env_value != nullptr)
    {
      expanded.append(env_value);
    }
    else
    {
      expanded.append(value.substr(start, end - start + 1));
    }

    pos = end + 1;
  }

  return expanded;
}
}

ConfigParser::ConfigParser() {}

// Remove whitespace from front and back of given string
void ConfigParser::strip(string &s)
{
  std::locale loc;

  // Do nothing if the string is empty:
  if (s.empty()) {
    return;
  }

  int front = 0;
  int end = s.length() - 1;
  while (front < s.length() && isspace(s[front], loc))
  {
    front++;
  }

  // Do nothing if the string is just empty white space:
  if (front == s.length()) {
    s.clear();
    return;
  }

  while (end > front && isspace(s[end], loc))
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
  string::size_type eqIndex;
  string::size_type commentIndex;
  file.open(config_file.c_str());
  if (!file.is_open())
  {
    return false;
  }
  while (getline(file, line))
  {
    if (line.empty())
    {
      continue;
    }
    // Remove all commented out lines:
    // removeto the right of '#' (treated as comment) to make an empty string
    commentIndex = line.find("#");
    if (commentIndex != string::npos) {
      line = line.substr(0, commentIndex);
      strip(line);
    }
    // ignore the line if its empty
    if (line.empty()) {
      continue;
    }

    // Use '=' as delimiter separating prop and value
    eqIndex = line.find("=");
    if (eqIndex == string::npos) { //If the line doesn't have a '=', ignore the line
      continue;
    }

    prop = line.substr(0, eqIndex);
    value = line.substr(eqIndex + 1, line.length());
    strip(prop);
    strip(value);
    value = expandEnvVars(value);

    if (prop.empty()) { // ignore the ine if the property is empty
      continue;
    }

    params[prop] = value;
  }
  return true;
}

void ConfigParser::parseDelimitedString(vector<string> &delimited,
                                        string &s, char delim)
{
  // Remove the line if it's empty:
  if (s.empty()) {
    return;
  }

  stringstream ss(s);
  string substr;
  while (getline(ss, substr, delim))
  {
    strip(substr);
    if (!substr.empty()) {
      delimited.push_back(substr);
    }
  }
}
