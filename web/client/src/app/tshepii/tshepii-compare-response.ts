/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { ServiceResponse } from '../types/service-response';
import { TshepiiResponse } from './tshepii-response';

export class TshepiiCompareResponse extends ServiceResponse {
  candidateImageId: number;
  probeResponse: TshepiiResponse;
  candidateResponse: TshepiiResponse;
}
