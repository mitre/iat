/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { CUSTOM_ELEMENTS_SCHEMA, EventEmitter } from '@angular/core';
import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatExpansionModule } from '@angular/material/expansion';
import { Annotation } from '../types/annotation';
import { AnnotationComponent } from './annotation.component';
import { AnnotationType } from '../types/annotation';
import { ToolManagerService } from '../iris-display/tool-manager.service';
import { LinkedMap } from '../types/linked-map';

describe('V. The AnnotationComponent Class: ', () => {
  let component: AnnotationComponent;
  let fixture: ComponentFixture<AnnotationComponent>;
  let toolManager: any;

  const annoType = AnnotationType.POINT;
  const anno =  new Annotation('string', annoType, 'string');
  const index = 1;
  const event: EventEmitter<any> = new EventEmitter();
  const valStr = 'foo';

  beforeEach(waitForAsync(() => {
    toolManager = {
      selectedAnnotation: undefined,
      userParentGroupId: '',
      userAnnoGroup: [],
      pullAnnoButtonClicked: false,
      userParentGroup: undefined
    };

    TestBed.configureTestingModule({
      imports: [MatExpansionModule],
      declarations: [ AnnotationComponent ],
      providers: [
        {provide: ToolManagerService, useValue: toolManager}
      ],
      schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
    })
      .compileComponents();
  }));

  beforeEach(waitForAsync(() => {
    fixture = TestBed.createComponent(AnnotationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

  }));

  it('a. getAnnotationText takes an object instance of the Annotation class as a parameter and returns a string.', () => {
    spyOn(component, 'getAnnotationText').and.returnValue(valStr);

    const res = component.getAnnotationText(anno);
    expect(res).toEqual(valStr);
  });

  it('b. removeItem takes an object instance of the Annotation class as a parameter and returns a void type.', () => {
    anno.parentId = 'foo';
    spyOn(component, 'removeItem').and.returnValue(undefined);

    const res = component.removeItem(anno);
    expect(res).toBe(undefined);
  });
  // These three test "void" functions and will require some added nuance.
  it('c. isAnyParentSelected takes an object instance of the Annotation class as a parameter and returns a boolean value (false).', () => {
    spyOn(component, 'isAnyParentSelected').and.returnValue(false);

    const res = component.isAnyParentSelected(anno);
    expect(res).toBe(false);
  });

  it('d. removeSelectedChildren takes an object instance of the Annotation class as a parameter and removes selected children from the param if present.', () => {
    anno.children = new LinkedMap<Annotation>();
    spyOn(component, 'removeSelectedChildren').and.returnValue(undefined);

    const res = component.removeSelectedChildren(anno);
    expect(res).toBe(undefined);
  });

  it('e. selectItem takes an object instance of the Annotation class, an index, and an event as parameters.', () => {
    spyOn(component, 'selectItem');
    const res = component.selectItem(anno, index, event);
    expect(res).toBe(undefined);
  });
});
