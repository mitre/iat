/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { 
  Component, Directive, Input, ViewChild, ViewContainerRef,
  ComponentFactoryResolver, OnChanges, AfterContentInit
 } from '@angular/core';
import { ToolManagerService } from '../tool-manager.service';
import { IrisToolRegistry } from './tool.registry';

@Directive({
  selector: '[appToolHost]'
})
export class ToolHostDirective {
  constructor(public viewContainerRef: ViewContainerRef) {
  }
}

//this is the middle bar underneath the tool bar
@Component({
  selector: 'app-iris-tool',
  template: `
    <div class="iris-tool">
      <form name="{{ 'tool-instance-' + instanceId }}">
        <ng-template appToolHost></ng-template>
      </form>
    </div>
  `,
})
export class IrisToolComponent implements AfterContentInit, OnChanges {
  _tool: string;
  @Input() index: number;
  @Input() instanceId: string;
  @ViewChild(ToolHostDirective, {static: true}) toolHost: ToolHostDirective;

  constructor(private toolRegistry: IrisToolRegistry,
              public toolManager: ToolManagerService,
              private componentFactoryResolver: ComponentFactoryResolver) {

  }

  @Input() set tool(val: string) {
    this._tool = val;
  }

  get tool(): string {
    return this._tool;
  }

  ngAfterContentInit() {
    this.loadToolComponent();
  }

  // Changes the loaded Component each time a different tool is activated
  // This logic only works AFTER ViewInit has fired (due to the dependency on @ViewChild())
  ngOnChanges(changes) {
    if (!!changes['tool'] && !changes['tool'].firstChange) {
      this.loadToolComponent();
    }
  }

  lookUpToolComponent() {
    return this.toolRegistry.getComponent(this.tool).component;
  }

  loadToolComponent() {
    // Get Derived Tool Component Class from Tool Registry
    const ToolComponent = this.lookUpToolComponent();

    // Resolve the Component via Angular black magic
    const componentFactory = this.componentFactoryResolver.resolveComponentFactory(ToolComponent);

    // Clear the current View Container ("host")
    const viewContainerRef = this.toolHost.viewContainerRef;
    viewContainerRef.clear();

    // Create/place the component inside the View Container
    const componentRef = viewContainerRef.createComponent(componentFactory);

    // Using the componentRef variable, append any additional data to the new component as needed
    this.toolManager.setCompRef(componentRef);
  }
}
