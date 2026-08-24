/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { State, StateType } from '../types/state';
import { Annotation } from '../types/annotation';

@Component({
  selector: 'app-annotation-history',
  templateUrl: './annotation-history.component.html',
  styleUrls: ['./annotation-history.component.css']
})
export class AnnotationHistoryComponent {

  @Input() history: { curr?: State; past?: State[] } = {};
  @Input() itemsMap: any = {};
  @Output('revert') revertEvent = new EventEmitter<number>();

  constructor() {
  }

  getStateName(type: StateType) {
    switch (type) {
      case StateType.ADD:
        return 'Add';
      case StateType.REMOVE:
        return 'Remove';
      case StateType.BULK_REMOVE:
        return 'Bulk Remove';
      case StateType.RESET:
        return 'Reset';
      case StateType.CLEAR:
        return 'Clear';
      case StateType.GROUP_ONLY:
        return 'Group';
      default:
        return 'Unknown';
    }
  }

  isGroup(type: StateType) {
    return type == StateType.GROUP_ONLY;
  }

  isAddRemove(type: StateType) {
    return type == StateType.ADD || type == StateType.REMOVE;
  }

  getStateText(state: State) {
    switch (state.type) {
      case StateType.ADD:
      case StateType.REMOVE:
        return this.getAnnotationText(state.annotation);
      case StateType.GROUP_ONLY:
        return 'Group annotation(s) together';
      case StateType.BULK_REMOVE:
        return 'Remove all drawn annotation(s)';
      case StateType.RESET:
        return 'Reset to last saved point';
      case StateType.CLEAR:
        return 'Clear all annotation(s)';
      default:
        return '';
    }
  }

  getAnnotationText(annotation: Annotation): string {
    return annotation.getValueString();
  }


  getStateWithAnnotationId(id: string) {
    for (const state of this.history.past) {
      if (state.annotation.id === id) {
        return state;
      }
    }
    if (this.history.curr.annotation.id === id) {
      return this.history.curr;
    }
    return null;
  }

  revertTo(index: number) {
    this.revertEvent.emit(index);
  }

  dummyPrint(thing) {
    console.log(thing);
  }

  getItemsKeys(items: any) {
    if (items == undefined) {
      return undefined;
    }

    return Object.keys(items);
  }
}
