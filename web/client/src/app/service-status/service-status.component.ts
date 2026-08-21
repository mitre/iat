/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { ServiceStatusService } from './service-status.service';

@Component({
  selector: 'app-service-status',
  templateUrl: './service-status.component.html',
  styleUrls: ['./service-status.component.css']
})

export class ServiceStatusComponent implements OnInit {
  currentUserId = 0;
  serviceDataSource: MatTableDataSource<string>;

  private servicePollingInterval = 10000; // (5000 = 5s)
  @Input() visibleColumns: string[];

  @Output() finishedLoading: EventEmitter<boolean> = new EventEmitter<boolean>();

  constructor(public serviceStatusService: ServiceStatusService) {
  }

  ngOnInit() {
    this.serviceStatusService.checkServicesStatus(); // Initial run that kicks off right away without waiting 10 seconds
    setInterval(() => this.serviceStatusService.checkServicesStatus(), this.servicePollingInterval);
  }
}
