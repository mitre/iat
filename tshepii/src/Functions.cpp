// #######################################################################
// NOTICE
// 
// This software (or technical data) was produced for the U. S. Government
// and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
// (May 2014) – Alternative IV (Dec 2007)
// 
// (c) 2024 The MITRE Corporation. All Rights Reserved.
// #######################################################################

/** @file Implementation of Fucntions class. */

#include <time.h>
#include "Functions.hpp"

void Functions::split(string *s, string delimiters, vector<string> *tokens)
{
	stringstream ss;
	ss << *s;

	while (true)
	{
		bool processNext = false;

		for (int i = 0; i < delimiters.length(); i++)
		{
			string item;
			if (getline(ss, item, delimiters.at(i)))
			{
				tokens->push_back(item);
				processNext = true;
				break;
			}
		}

		if (!processNext)
			break;
	}
}

string Functions::getCurrentDateTime()
{
	time_t now = time(0);
	struct tm tstruct;
	char buf[80];
	tstruct = *localtime(&now);
	strftime(buf, sizeof(buf), "%Y-%m-%d   %X", &tstruct);
	return buf;
}

bool Functions::compareMatches(DMatch match1, DMatch match2)
{
	return fabs(match1.distance) < fabs(match2.distance);
}

bool Functions::compareKeypoints(KeyPoint kp1, KeyPoint kp2)
{
	return kp1.response > kp2.response;
}

void Functions::computeMeanStd(vector<double> *data, double *mean, double *std)
{
	*mean = 0.0;
	for (vector<double>::iterator it = data->begin(); it != data->end(); ++it)
		*mean = *mean + *it;
	*mean = *mean / data->size();

	*std = 0.0;
	for (vector<double>::iterator it = data->begin(); it != data->end(); ++it)
		*std = *std + pow(*it - *mean, 2);
	*std = sqrt(*std / data->size());
}
