/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { OnInit, OnDestroy, Component } from '@angular/core';
import { Layer, Item } from 'paper';
import { ToolManagerService } from '../tool-manager.service';
import { Subject } from 'rxjs';
import { AnnotationType } from '../../types/annotation';
import { CoreFunctions } from '../core/core-functions';

export enum IrisToolType {
  ANNOTATION = 'ANNOTATION',
  IMAGE = 'IMAGE'
}

export class RequiredMembers {
  public static icon: string;
  public static id: string;
  public static type: IrisToolType;
}

@Component({
  template: ''
})

export class IrisDrawingTool implements OnInit, OnDestroy {

  name: string;                           // override to name the Tool
  public static id: string;               // override to match the "name" member (this.name)
  public static icon: string;             // override to a Font Awesome Icon (i.e: fa-user)
  public static toolType: IrisToolType;   // override to select which toolbar to populate
  protected _layerKey: string;            // override this to select a specific layer (i.e.: 'iris'). Defaults to this.name
  protected data: object = {};            // override this to set default values for tool-specific variables/values
  protected config: object = {};          //configuration information set in config file for this tool
  public toolChange$: Subject<any> = new Subject<any>();
  coreFunctions: CoreFunctions = new CoreFunctions();
  modified = false;
  userParentGroupId: string;

  public annotationType = AnnotationType.UNKNOWN;
  protected _finalItem: InstanceType<typeof Item> = null;

  // add properties to this object for defaults across all tools
  protected defaults: object = {
    drawColor: '#000',
    fillColor: '#fff',
    fillAlpha: 0.2
  };


  ////////////////////////////////////// Do not override any MEMBERS below this point ////////////////////////////////////////////////

  protected _layer: InstanceType<typeof Layer>;                // The paper.Layer instance this tool references, pre-activated
  public _isActive = false;            // Show be no need to override this


  ////////////////////////// Override any placeholder methods belong to define tool's behavior  //////////////////////////////////////
  constructor(protected toolManager: ToolManagerService) {
  }

  // Placeholder method
  // this._layer will be populated by the time this fires
  onToolInit() {
  }

  // Placeholder method
  onMouseUp(evt?) {
  }

  // Placeholder method
  onMouseDown(evt?) {
  }

  // Placeholder method
  onMouseDrag(evt?) {
  }

  // Placeholder method
  onMouseMove(evt?) {
  }

  // Placeholder method
  onMouseLeave(evt?) {
  }

  reset() {
    // Re-create this Component??
  }

  complete() {
    this.toolManager.updateUnrollAnnotations();

    // Dumps updates into the Layer.data field for storage
    this._layer.data = Object.assign(this._layer.data, this.data);
  }

  protected _destroy() {
    this._isActive = false;
  };

  resetData() {
    // Do nothing, as it's overridden by children
  }

  reAddData(annotation) {
    if (!annotation || !annotation.value) {
      return;
    }
  }


////////////////////////////////////// Do not override any METHODS below this point ////////////////////////////////////////////////
  //this is called when the tool on the tool bar is clicked
  ngOnInit() {
    // update active tool within ToolManager
    this.toolManager.activeTool = this.name;

    this._layer = this.toolManager.initTool(this.name, this._layerKey);

    // Override any defaults with current values via Object.assign
    this.data = Object.assign(this.data, this._layer.data);

    // Deep Clone the config data for this tool
    this.config = JSON.parse(JSON.stringify(this.toolManager.contentSvc.getToolConfig(this.name)));

    // run tool-specific logic during init, this.config will exist at this point
    this.onToolInit();

    this._isActive = true;
  }

  ngOnDestroy() {
    // Dumps updates into the Layer.data field for storage
    this._layer.data = Object.assign(this._layer.data, this.data);

    this._destroy();
  }
}
