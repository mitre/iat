/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { Annotation, AnnotationType } from '../types/annotation';
import { LinkedMap } from '../types/linked-map';
import { ToolManagerService } from '../iris-display/tool-manager.service';

@Component({
  selector: 'app-annotation',
  templateUrl: './annotation.component.html',
  styleUrls: ['./annotation.component.css']
})
export class AnnotationComponent {

  @Input() annotations = new LinkedMap<Annotation>();
  @Input() itemsMap = {};
  @Input('hiddenAnnotations') hidden = new Set();
  @Input('color') annotationColor = '#ff0000';
  @Input() isShowAnnotationLabels = false;
  @Input() annotationTextSize = 12;

  @Output('remove') removeEvent = new EventEmitter<string>();
  @Output() annotationMouseOn = new EventEmitter<string>();
  @Output() annotationMouseOff = new EventEmitter<string>();
  @Output('hideDialog') hideEvent = new EventEmitter<any>();
  @Output() updateColor = new EventEmitter<any>();
  @Output() showAnnotationColor = new EventEmitter<boolean>();
  @Output() updateAnnotationTextSize = new EventEmitter<number>();
  @Output() showHideAnnotations = new EventEmitter<boolean>();
  @Output() deleteAnnotations = new EventEmitter<boolean>();
  @Output() groupAnnotations = new EventEmitter<any>();
  @Output() showHideAnnotation = new EventEmitter<string>();
  @Output() selectedAnnotationChanged = new EventEmitter<Annotation>();

  public selectedIds: Array<string> = [];
  annoSelected = false;
  defocusLayers = false;
  showAnnotations = true;
  isDropdownToggled = false;
  selectedAnnotation: Annotation;
  private selected = {};
  constructor(protected toolManager: ToolManagerService
  ) {
  }

  getAnnotationText(annotation: Annotation) {
    return annotation.getValueString();
  }

  removeItem(annotation: Annotation) {
    this.toolManager.selectedAnnotation = undefined;
    if (annotation.parentId != null) {
      this.removeEvent.emit(annotation.id);
    } else {
      this.removeEvent.emit(annotation.id);
      this.toolManager.userParentGroupId = '';
      this.toolManager.userAnnoGroup = [];
    }
    if (annotation.text == 'Iris Annotation Service') {
      this.toolManager.pullAnnoButtonClicked = false;
    }
  }

  itemMouseOn(id: string): void {
    this.annotationMouseOn.emit(id);
  }

  itemMouseOff(id: string): void {
    this.annotationMouseOff.emit(id);
  }

  showHideAnnotationsEvent(event) {
    this.showAnnotations = !this.showAnnotations;
    this.showHideAnnotations.emit(this.showAnnotations);
  }

  removeAllAnnotations() {
    this.deleteAnnotations.emit(true);
  }


  isAnyParentSelected(anno): boolean {
    if (this.selectedIds.includes(anno.id)) {
      return true;
    }

    if (!anno.parentId) {
      return false;
    }

    return this.isAnyParentSelected(this.itemsMap[anno.parentId]['annotation']);
  }

  removeSelectedChildren(anno) {
    if (anno.hasChildren()) {
      for (const child of anno.children) {
        const foundIndex = this.selectedIds.indexOf(child.id);
        if (foundIndex >= 0) {
          this.selectedIds.splice(foundIndex, 1);
        }
        this.removeSelectedChildren(child);
      }
    }
  }

  selectItem(anno, index, event) {
    const id = anno.id;
    const parentId = anno.parentId || 'no_parent';

    const foundIndex = this.selectedIds.indexOf(id);
    let select = false;
    if (foundIndex >= 0) { // If the item was already selected

      // If it's not a multi select, remove everything
      // and add the currently selected item.
      // If it is, just remove the selected item.
      if (!(event.ctrlKey || event.metaKey)) {
        this.selectedIds = [];
        this.selectedIds.push(id);
        this.selected = {};
        select = true;
      } else {
        this.selectedIds.splice(foundIndex, 1);
        if (this.selected[parentId]) {
          // Remove child with id=`id`
          this.selected[parentId] =
            this.selected[parentId].filter(obj => obj['id'] !== id);
        }
      }
    } else {
      // If it's not a multi-select and the clicked anno
      // is not yet selected, empty the selection list
      // Also check for metaKey for macs
      if (!(event.ctrlKey || event.metaKey)) {
        this.selectedIds = [];
        this.selected = {};
      }

      // Make sure to remove all children of this particular
      // item, since it's invalid to select both group and its
      // children.
      this.removeSelectedChildren(anno);
      delete this.selected[anno.id];

      // If any of item's parents are already selected,
      // don't select this item.
      if (!this.isAnyParentSelected(anno)) {
        this.selectedIds.push(id);
        select = true;
      }
    }
    this.annoSelected = true;

    // If the specified item is to be selected,
    // insert it to the selected map
    if (select) {
      if (!this.selected[parentId]) {
        this.selected[parentId] = [];
      }

      this.selected[parentId].push({id, index});
    }

    if (!anno.isSystemGenerated) {
      if (anno.type == AnnotationType.GROUP) {
        this.selectedAnnotation = anno;
        this.selectedAnnotationChanged.emit(anno);
      } else {
        this.selectedAnnotation = this.itemsMap[id]['annotation'];
        this.selectedAnnotationChanged.emit(this.selectedAnnotation);
      }
    } else {
      this.selectedAnnotation = undefined;
      this.selectedAnnotationChanged.emit(undefined);
    }
  }

  group() {
    // Sort each parent's children list by index
    Object.keys(this.selected).forEach(parentId => {
      this.selected[parentId].sort((a, b) => a.index - b.index);
    });
    this.groupAnnotations.emit(this.selected);
    this.selectedIds.length = 0;
    this.selected = {};

  }

  expandAllGroups() {
    this.collapsingExpandingAction(true);
  }

  collapseAllGroups() {
    this.collapsingExpandingAction(false);
  }

  collapsingExpandingAction(isExpand: boolean) {
    const entries = Object.entries(this.itemsMap);

    for (const [id, objects] of entries) {
      const objs = Object.entries(objects);
      const annotation: any = objs[0];
      const item: any = objs[1];

      if (item[1] != undefined) {
        if (item[1]._class == 'Group') {
          annotation[1].isDropdownToggled = isExpand;
        }
      }
    }
  }

  showHideItem(id: string) {
    //this if/else prevents 'children' being unhiden if the parent node is hiden
    const anno = this.itemsMap[id]['annotation'];
    if (!(this.hidden.has(anno.parentId))) {
      this.showHideAnnotation.emit(id);
    }
  }

  isAnnotationHidden(id: string) {
    return this.hidden.has(id);
  }

  isHideAble(anno: Annotation) {
    return anno.type == AnnotationType.POLYGON ||
      anno.type == AnnotationType.POINT ||
      anno.type == AnnotationType.PENCIL ||
      anno.type == AnnotationType.CIRCLE;
  }

  toggleClicked(item) {
    item.isDropdownToggled = !item.isDropdownToggled;
  }

  clickedInside = false;

  @HostListener('document:click', ['$event'])
  onDocumentClick(e) {
    if (this.clickedInside) {
      // If clicked within the dialogue and outside the layer,
      // and the previous click wasn't outside the dialogue,
      // deselect all layers.
      // Basically, if clicked anywhere else but the layer,
      // deselect, except the first click inside after an outside click.
      if (!this.annoSelected && !this.defocusLayers) {
        this.selectedIds.length = 0;
        this.selected = {};
        this.selectedAnnotation = this.toolManager.userParentGroup;
        this.selectedAnnotationChanged.emit(this.toolManager.userParentGroup);
      }
      this.defocusLayers = false;
    } else if (!this.clickedInside) {
      // If clicked outside the dialogue, the dialogue is
      // hidden, and there were selected layers, deselect
      // everything.
      if (this.selectedIds.length) {
        this.selectedIds.length = 0;
        this.selected = {};
      }
      this.defocusLayers = true;
    }
    this.clickedInside = false;
    this.annoSelected = false;
  }

  // This is needed for elements rendered thru ngIf.
  // Without it, layers (esp. groups) that are conditioned
  // on ngIf fire an outside click event.
  @HostListener('click', ['$event'])
  onClick(e) {
    this.clickedInside = true;
  }
}
