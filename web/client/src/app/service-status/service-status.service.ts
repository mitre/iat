/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MatTableDataSource } from '@angular/material/table';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  IwpServiceProperties,
  IwpServiceStatus,
  ServiceStatusNameType,
  ServiceStatusStatusType
} from '../types/service-status';
import { VersionInfo } from '../types/versionInfo';

@Injectable(
  {providedIn: 'root'}
)
export class ServiceStatusService {

  servicesServiceUrl = '/api/activeMQ/services/status';
  servicesPropertiesUrl = '/api/service/status';
  serviceVersionUrl = '/api/service/versions';

  // Property for flagging when even the URL that checks check services are available is down
  serviceStatusDown = false;
  systemDownMessage: string;

  serviceDataSource: MatTableDataSource<IwpServiceStatus>;
  versionInfo: VersionInfo;
  _iwpServices: IwpServiceStatus[] = [];
  iwpFinalServices: IwpServiceStatus[] = [];
  previousServiceStatus: IwpServiceStatus[] = [];
  overallServiceStatus: ServiceStatusStatusType = ServiceStatusStatusType.Loading;

  constructor(private http: HttpClient,
              private matSnackBar: MatSnackBar) {
  }

  updateServiceVersions(): void {
    this.http.get<VersionInfo>(this.serviceVersionUrl).subscribe(vi => {
      this.versionInfo = vi;
    });
  }

  getBackendServices(): Observable<IwpServiceStatus[]> {
    return this.http.get<IwpServiceStatus[]>(this.servicesServiceUrl);
  }

  getServicesProperties(): Observable<IwpServiceProperties[]> {
    return this.http.get<IwpServiceProperties[]>(this.servicesPropertiesUrl);
  }

  public isServiceDisabled(serviceName: string): boolean {
    const newStatus = this.iwpFinalServices.find(fStatus => fStatus.name === serviceName);
    if (!!newStatus && newStatus.status == ServiceStatusStatusType.Disabled) {
      return true;
    }
    return false;
  }

  checkServicesStatus(): void {
    this._iwpServices = [];
    this.iwpFinalServices = [];

    this.getBackendServices().subscribe(data => {
      if (data === null) {
        this.markAllServicesDown('ActiveMQ is down, contact your system administrator.');
      } else {
        for (const serviceData of data) {
          this._setIwpServicesArray(serviceData);
        }
        this.getServicesProperties().subscribe(propertyData => {
          this._iwpServices.forEach(iwpItem => {
            const matchedItem = propertyData.find(propData => propData.serviceName === iwpItem.name);
            if (!!matchedItem && matchedItem.serviceExcluded == false) {
              this.setIwpFinalServicesStatus(iwpItem, matchedItem);
            }
          });
          // Sort the services into alphabetical order
          this.iwpFinalServices.sort((a, b) => a.name.localeCompare(b.name));

          this.comparePreviousStatus();
          this.setOverallServiceStatus();

          // Push in a service definition for the front and backend manually so we can report out their version numbers
          this.iwpFinalServices.push({
            name: ServiceStatusNameType.webbackend,
            displayName: 'Web Backend',
            statusHint: this.getMessageForStatus(ServiceStatusStatusType.Online),
            status: ServiceStatusStatusType.Online,
            version: this.versionInfo.iwpVersion,
            consumerCount: null,
            enqueueCount: null,
            dequeueCount: null,
            listenerType: null,
            serviceDefinition: 'Serves the backend API to the IRIS web application.'
          });
          this.iwpFinalServices.push({
            name: ServiceStatusNameType.webfrontend,
            displayName: 'Web Frontend',
            statusHint: this.getMessageForStatus(ServiceStatusStatusType.Online),
            status: ServiceStatusStatusType.Online,
            version: this.versionInfo.uiVersion,
            consumerCount: null,
            enqueueCount: null,
            dequeueCount: null,
            listenerType: null,
            serviceDefinition: 'Serves the IRIS web application user interface.'
          });

          this.serviceDataSource = new MatTableDataSource<IwpServiceStatus>(this.iwpFinalServices);
        });
      }
    }, error => {
      this.markAllServicesDown('All services are down; contact your system administrator.');
    });
  }

  private markAllServicesDown(message: string) {
    this.serviceStatusDown = true;
    this.overallServiceStatus = ServiceStatusStatusType.Error;
    this.systemDownMessage = message;
    this.matSnackBar.open(message, 'Reload Page', {duration: 0}).onAction().subscribe(() => window.location.reload());
  }

  _setIwpServicesArray(service: IwpServiceStatus) {
    const fullName = service.name.split('.');
    let listenerName = fullName[1];
    let listenerType = fullName[2];
    let serviceDef = null;
    const displayName = listenerName;

    if (listenerName == ServiceStatusNameType.tshepii) {
      serviceDef = 'Detects crypts and unrolls the location for the iris';
    } else if (listenerName == ServiceStatusNameType.acii) {
      serviceDef = 'Suggests the orientation of an image: Left or Right';
    } else if (listenerName == ServiceStatusNameType.annotation) {
      serviceDef = 'Provides automated labeling for image segments';
    } else if (listenerName == ServiceStatusNameType.biqt) {
      serviceDef = 'Provides the image with a quality score and suggests the presence of contacts: Clear or Cosmetic';
    } else if (listenerName == ServiceStatusNameType.pdm) {
      serviceDef = 'Changes the ratio of the pupil to iris by dilating pupil.';
    }

    const serviceStatus: IwpServiceStatus = {
      name: <ServiceStatusNameType>listenerName,
      displayName,
      status: null,
      consumerCount: service.consumerCount,
      enqueueCount: service.enqueueCount,
      dequeueCount: service.dequeueCount,
      listenerType,
      statusHint: null,
      serviceDefinition: serviceDef,
      version: this.versionInfo.serviceVersions[listenerName]
    };
    this._iwpServices.push(serviceStatus);
  }

  setIwpFinalServicesStatus(iwpItem: IwpServiceStatus, propertyItem: IwpServiceProperties): void {
    let status = null;
    let errHint = null;
    let responseQueue = null;
    let requestQueue = null;

    if (propertyItem.serviceEnabled == false) {
      status = ServiceStatusStatusType.Disabled;
      errHint = this.getMessageForStatus(ServiceStatusStatusType.Disabled);
    } else {
      // Checking for listeners of each service
      this._iwpServices.forEach(item => {
        if (item.name == iwpItem.name && item.listenerType == 'response') {
          responseQueue = item;
        }
        if (item.name == iwpItem.name && item.listenerType == 'request') {
          requestQueue = item;
        }
      });

      // If request and response queues are present AND both have listeners - Not a problem
      // Service Property is true, and service is running
      if (!!requestQueue && !!responseQueue) {
        if (requestQueue.consumerCount != 0 && responseQueue.consumerCount != 0) {
          status = ServiceStatusStatusType.Online;
          errHint = this.getMessageForStatus(ServiceStatusStatusType.Online);
        } else if ((requestQueue.consumerCount != 0 && responseQueue.consumerCount == 0) ||
          (requestQueue.consumerCount == 0 && responseQueue.consumerCount == 0)) {
          // If request and response queues are present AND request listener = 1 AND response listener = 0 - Not a problem
          // Service Property is false, service is running
          // If both queues are present and both listeners = 0 Not a problem
          status = ServiceStatusStatusType.Disabled;
          errHint = this.getMessageForStatus(ServiceStatusStatusType.Disabled);
        } else if (requestQueue.consumerCount == 0 && responseQueue.consumerCount != 0) {
          // If request and response queues are present AND request listener = 0 AND response listener = 1 - Problem
          // Service Property is true, service is NOT running
          status = ServiceStatusStatusType.Error;
          errHint = this.getMessageForStatus(ServiceStatusStatusType.Error);
        }
      } else if (requestQueue == null && !!responseQueue && responseQueue.consumerCount != 0) {
        // If response queues is present AND response listener = 1 AND request queue is missing - Problem
        // Service Property is true, service is NOT running
        status = ServiceStatusStatusType.Error;
        errHint = this.getMessageForStatus(ServiceStatusStatusType.Error);
      } else if (requestQueue == null && responseQueue == null) {
        status = ServiceStatusStatusType.Disabled;
        errHint = this.getMessageForStatus(ServiceStatusStatusType.Disabled);
      }
    }

    const updatedService = {
      name: iwpItem.name,
      displayName: iwpItem.displayName,
      status,
      consumerCount: iwpItem.consumerCount,
      enqueueCount: iwpItem.enqueueCount,
      dequeueCount: iwpItem.dequeueCount,
      listenerType: null,
      statusHint: errHint,
      serviceDefinition: iwpItem.serviceDefinition,
      version: this.versionInfo.serviceVersions[iwpItem.name]
    };

    const serviceIndex = this.iwpFinalServices.findIndex(service => service.name == updatedService.name);
    if (serviceIndex === -1) {
      this.iwpFinalServices.push(updatedService);
    }
  }

  private getMessageForStatus(statusType: ServiceStatusStatusType): string {
    switch (statusType) {
      case ServiceStatusStatusType.Online: {
        return 'Online: Service is available';
      }
      case (ServiceStatusStatusType.Disabled): {
        return 'Disabled: Service is disabled in IWP Properties file';
      }
      case (ServiceStatusStatusType.Loading): {
        return 'Loading: Service is initializing';
      }
      case (ServiceStatusStatusType.Error): {
        return 'Error: Service has encountered an issue';
      }
      default: {
        return 'Unknown Status';
      }

    }
  }

  comparePreviousStatus(): void {
    const statusChanges: IwpServiceStatus[] = [];

    if (this.previousServiceStatus.length > 0) {
      this.previousServiceStatus.forEach(prevStatus => {
        const newStatus = this.iwpFinalServices.find(fStatus => fStatus.name === prevStatus.name);
        if (!!newStatus && newStatus.status != prevStatus.status) {
          statusChanges.push(newStatus);
        }
      });

      if (statusChanges.length > 0) {
        this.showSnackBarNotification(statusChanges);
      }
    }

    this.previousServiceStatus = JSON.parse(JSON.stringify(this.iwpFinalServices));

  }

  showSnackBarNotification(newStatuses: IwpServiceStatus[]): void {
    const messages: string[] = [];
    this.overallServiceStatus = ServiceStatusStatusType.Loading;
    for (const status of newStatuses) {
      const messageToAdd = status.name + '\'s status has changed to ' + status.status + '.\n\n';
      messages.push(messageToAdd);
    }
    const messageToPost = messages.join('');

    this.matSnackBar.open(messageToPost, 'Dismiss');
  }

  setOverallServiceStatus() {
    let isError = 0;
    let isDisabled = 0;
    let isOnline = 0;
    for (const service of this.iwpFinalServices) {
      if (service.status == ServiceStatusStatusType.Error) {
        isError++;
      } else if (service.status == ServiceStatusStatusType.Disabled) {
        isDisabled++;
      } else if (service.status == ServiceStatusStatusType.Online) {
        isOnline++;
      }
    }

    if (isError >= 1) {
      this.overallServiceStatus = ServiceStatusStatusType.Error;
    } else if (isDisabled == 5) {
      this.overallServiceStatus = ServiceStatusStatusType.Disabled;
    } else if (isError == 0 && isDisabled < 5 && isOnline >= 1) {
      this.overallServiceStatus = ServiceStatusStatusType.Online;
    } else {
      this.overallServiceStatus = ServiceStatusStatusType.Loading;
    }
  }

  getServices(): IwpServiceStatus[] {
    return this.iwpFinalServices;
  }

  getNonWebServices(): IwpServiceStatus[] {
    let nonwebServices: IwpServiceStatus[] = this.iwpFinalServices.slice();

    //take out the web services from display
    nonwebServices = nonwebServices.filter((obj) => (obj.name !== ServiceStatusNameType.webbackend));

    return nonwebServices.filter((obj) => (obj.name != ServiceStatusNameType.webfrontend));

  }

}
