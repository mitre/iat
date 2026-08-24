/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import * as short from 'short-uuid';

import { LinkedMap } from './linked-map';
import { JsonProperty } from '../utils/decorators/json-property';
import { MapUtils } from '../utils/map-utils';
import { parse, stringify } from 'flatted/esm';
import { AnnotationValue } from './annotation-value';

export enum AnnotationType {
  POINT = 0,
  CIRCLE = 1,
  POLYGON = 2,
  PENCIL = 3,
  UNKNOWN = 4,
  CLAHE = 5,
  BRIGHTNESS = 6,
  CONTRAST = 7,
  CHANNEL = 8,
  COLOR_LEVEL = 9,
  CROP = 10,
  ROTATE = 11,
  SHARPEN = 12,
  RESIZE = 13,
  MIRROR = 14,
  GROUP = 15,
  EYELID = 16,
  PDM = 17,
  DUAL_PDM = 18,
  // For grouping
}

export const ANNO_ICON_MAPPING = {
  [AnnotationType.POINT]: 'fa-map-pin',
  [AnnotationType.CIRCLE]: 'fa-circle',
  [AnnotationType.POLYGON]: 'fa-bookmark',
  [AnnotationType.PENCIL]: 'fa-pencil-alt',
  [AnnotationType.UNKNOWN]: 'fa-question-circle',
  [AnnotationType.CLAHE]: 'fas fa-th',
  [AnnotationType.BRIGHTNESS]: 'fa-sun-o',
  [AnnotationType.CONTRAST]: 'fa-adjust',
  [AnnotationType.CHANNEL]: 'fa-bar-chart',
  [AnnotationType.COLOR_LEVEL]: 'fa-pie-chart',
  [AnnotationType.CROP]: 'fa-crop',
  [AnnotationType.ROTATE]: 'fa-redo',
  [AnnotationType.SHARPEN]: 'fa-sliders',
  [AnnotationType.RESIZE]: 'fa-object-ungroup',
  [AnnotationType.MIRROR]: 'fa-exchange',
  [AnnotationType.GROUP]: 'fa-folder-o',
  [AnnotationType.EYELID]: 'fa-smile-beam',
  [AnnotationType.PDM]: 'all_out'
};

export class Annotation {
  id: string;
  text: string;
  type: AnnotationType;
  creationDateEpoch: number;
  drawColor: string;
  fillColor: string;
  annoIcon: string;
  iconType: string = "fa";
  isSystemGenerated = false;

  public value: AnnotationValue;

  parentId = '';

  // This is a workaround for saving a List instead of LinkedMap
  // on the server-side. The problem is, since LinkedMap is a doubly
  // linked list, it causes a circular reference that needs a custom
  // deseraializer on the backend. To avoid that, we have a dummy
  // List to pass onto the server when saving the Annotations.
  @JsonProperty({clazz: Annotation})
  tempChildren: Annotation[];

  children: LinkedMap<Annotation>;

  constructor(text?: string, type?: AnnotationType, annoIcon?: string, iconType?: string) {
    this.id = short.generate();
    this.text = text ?? null;
    this.type = type ?? null;
    this.creationDateEpoch = Date.now();
    this.annoIcon = annoIcon ?? null;
    this.iconType = iconType ?? "fa";
    this.drawColor = '';
    this.fillColor = '';
    this.value = new AnnotationValue();
    this.tempChildren = [];
    this.children = new LinkedMap<Annotation>();
  }

  reassignId(): string  {
    const strId = short.generate();
    this.id = strId;
    return this.id;
  }

  clearChildren() {
    return this.children.clearAll();
  }

  appendChildren(children: Array<Annotation>) {
    for (const child of children) {
      this.children.insert(child.id, child);
    }
  }

  addChild(child: Annotation, index?: number) {
    this.children.insert(child.id, child, index);
    return child.parentId = this.id;
  }

  addChildBefore(beforeId: string, child: Annotation) {
    this.children.insertBefore(beforeId, child.id, child);
    return child.parentId = this.id;
  }

  removeChild(thing: string | number) {
    let removed;

    // Due to strict typing of TypeScript
    if (typeof thing === 'string') {
      removed = this.children.remove(thing);
    } else if (typeof thing === 'number') {
      removed = this.children.remove(thing);
    }

    if (removed) {
      removed.parentId = null;
    }
    return removed;
  }

  getChildIndex(id: string): number {
    return this.children.getIndex(id);
  }

  getChildAfter(id: string) {
    return this.children.getNext(id);
  }

  getChildBefore(id: string) {
    return this.children.getPrev(id);
  }

  getFirstChild(): Annotation | null {
    return this.children.getFirst();
  }

  hasChildren() {
    return !this.children.isEmpty();
  }

  compareChildrenIndices(id1: string, id2: string) {
    return this.children.compareIndices(id1, id2);
  }

  getValueString(): string {
    switch (+this.type) {
      case AnnotationType.POINT:
      case AnnotationType.CIRCLE:
        return 'Center: (' + Math.floor(this.value.points[0].x) + ', ' + Math.floor(this.value.points[0].y) + ')';
      case AnnotationType.POLYGON:
        break;
      case AnnotationType.EYELID:
        return 'Eyelid Location: ' + this.value.data['eyelidType'];
      case AnnotationType.PENCIL:
        return 'Color: ' + this.value.data['strokeColor'];
      case AnnotationType.CLAHE:
        return 'Clip Factor: ' + this.value.data['clipFactor'];
      case AnnotationType.BRIGHTNESS:
        return 'Value: ' + Math.round((+this.value.data['adjustment'] + 100) / 2.0);
      case AnnotationType.CONTRAST:
        return 'Value: ' + Math.round((+this.value.data['adjustment'] + 100) / 2.0);
      case AnnotationType.CHANNEL:
        return 'Channel: ' + this.value.data['channel'];
      case AnnotationType.COLOR_LEVEL:
        return 'Change RGB(' + this.value.data['red'] + ', ' + this.value.data['green'] + ', ' + this.value.data['blue'] + ')';
      case AnnotationType.CROP:
        return 'Square( Center: (' + Math.floor(this.value.data['fromPointX']) + ', '
          + Math.floor(this.value.data['fromPointY']) + ') Width: '
          + Math.floor(this.value.data['width']) + ' Height: '
          + Math.floor(this.value.data['height']) + ')';
      case AnnotationType.ROTATE:
        return 'Rotation: ' + (-1 * this.value.data['adjustment']);
      case AnnotationType.SHARPEN:
        return 'Value: ' + this.value.data['adjustment'];
      case AnnotationType.RESIZE:
        return 'Width: ' + Math.floor(this.value.data['width']) + ' Height: '
          + Math.floor(this.value.data['height']);
      case AnnotationType.MIRROR:
        return 'Orientation: ' + this.value.data['adjustment'];
      case AnnotationType.PDM:
        return 'Alpha Score: ' + this.value.data['alphaScore'];
      default:
        return '';
    }
    return '';
  }

  static clone(anno: Annotation) {
    const json = parse(stringify(anno));
    const cloned = MapUtils.deserialize(Annotation, json);

    const arr: any[] = [];
    if (anno.hasChildren()) {
      for (const child of anno.children!) {

        arr.push({
          key: child!.id,
          value: this.clone(child!)
        });
      }
    }
    cloned!.children = new LinkedMap<Annotation>(arr);
    return cloned;
  }
}
