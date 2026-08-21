/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

export interface IwpServiceStatus {
  name: ServiceStatusNameType;
  displayName: string;
  status: ServiceStatusStatusType;
  consumerCount: string;
  enqueueCount: string;
  dequeueCount: string;
  listenerType: string;
  statusHint: string;
  serviceDefinition: string;
  version: string;
}

export interface IwpServiceProperties {
  serviceName: string;
  serviceEnabled: boolean;
  serviceRequest: string;
  serviceResponse: string;
  serviceExcluded: boolean;
}

export enum ServiceStatusStatusType {
  Loading = 'Loading',
  Online = 'Online',
  Disabled = 'Disabled',
  Error = 'Error'
}

export enum ServiceStatusNameType {
  acii = 'acii',
  annotation = 'annotation',
  biqt = 'biqt',
  contactdetection = 'contactdetection',
  tshepii = 'tshepii',
  dualpdm = 'dualpdm',
  pdm = 'pdm',
  webbackend = 'webbackend',
  webfrontend = 'webfrontend'
}
