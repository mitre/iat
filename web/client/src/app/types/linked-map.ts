/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { JsonProperty } from '../utils/decorators/json-property';

export class Node<T> {
  @JsonProperty({clazz: Node})
  prev: Node<T>;

  @JsonProperty({clazz: Node})
  next: Node<T>;
  key: string;
  payload: T;

  constructor(key?: string, payload?: T) {
    this.prev = null;
    this.next = null;
    this.key = key || null;
    this.payload = payload || null;
  }
}

export class LinkedMap<T> {
  map: any;

  @JsonProperty({clazz: Node})
  private head: Node<T>; // Sentinel

  @JsonProperty({clazz: Node})
  private tail: Node<T>; // Sentinel
  public size: number; // 'length' is a reserved word for Object type

  // Accepts list of form [{ key, value(typeof T) }]
  constructor(items?: Array<any>) {
    this.map = {};
    this.head = new Node<T>();
    this.tail = new Node<T>();
    this.size = 0;

    this.head.next = this.tail;
    this.tail.prev = this.head;

    if (items && items.length) {
      for (const item of items) {
        this.insert(item.key, item.value);
      }
    }
  }

  get(thing: string | number): any {
    let item;
    if (typeof thing === 'string') {
      item = this.map[thing];
    } else if (typeof thing === 'number') {
      item = this.getNode(thing);
    }
    return item ? item.payload : null;
  }

  getFirst() {
    return this.head.next == this.tail ? null : this.head.next.payload;
  }

  getLast() {
    return this.tail.prev == this.head ? null : this.tail.prev.payload;
  }

  getNext(key: string) {
    const item = this.map[key];
    if (!item || item.next == this.tail) {
      return null;
    }
    return item.next.payload;
  }

  getPrev(key: string) {
    const item = this.map[key];
    if (!item || item.prev == this.head) {
      return null;
    }
    return item.prev.payload;
  }

  isEmpty(): boolean {
    return this.size === 0;
  }

  getIndex(key): number {
    let item = this.head;
    let i = 0;

    while (i <= this.size) {
      if (item.key === key) {
        return i - 1;
      }
      item = item.next;
      i++;
    }

    return -1;
  }

  insert(key: string, payload: T, index?: number) {
    let item;
    if (index != undefined &&
      index >= 0 &&
      index < this.size) {
      item = this.insertAtIndex(key, payload, index);
    } else {
      item = this.push(key, payload);
    }
    this.map[key] = item;
    this.size += 1;
  }

  insertBefore(beforeKey: string, key: string, payload: T) {
    const before = this.map[beforeKey];
    let item;
    if (!before) {
      item = this.push(key, payload);
    } else {
      item = new Node<T>(key, payload);
      item.prev = before.prev;
      item.next = before;
      before.prev.next = item;
      before.prev = item;
    }
    this.map[key] = item;
    this.size += 1;
  }

  insertAfter(afterKey: string, key: string, payload: T) {
    const after = this.map[afterKey];
    let item;
    if (!after) {
      item = this.push(key, payload);
    } else {
      item = new Node<T>(key, payload);
      item.prev = after;
      item.next = after.next;
      after.next.prev = item;
      after.next = item;
    }
    this.map[key] = item;
    this.size += 1;
  }

  compareIndices(key1: string, key2: string): number {
    // return -1 if key1 comes before key2
    // return 0 if either key1 or key2 is not in map
    // return 1 if key1 comes after key2

    if (!(this.map[key1] && this.map[key2])) {
      return 0;
    }

    let item = this.head;
    let i = 0;

    while (i <= this.size) {
      if (item.key === key1) {
        return -1;
      } else if (item.key === key2) {
        return 1;
      }

      item = item.next;
      i++;
    }
    return 0;
  }

  remove(thing: string | number): any {
    let item;
    if (typeof thing === 'string') {
      item = this.map[thing];
    } else if (typeof thing === 'number') {
      // First make sure that the index is valid
      if (thing < 0 || thing > this.size) {
        return;
      }
      item = this.getNode(+thing);
    }

    if (item) {
      this.removeItem(item);
      this.size -= 1;
      return item.payload;
    }
    return null;
  }

  clearAll() {
    this.map = {};
    this.size = 0;

    this.head.next = this.tail;
    this.tail.prev = this.head;
  }

  toList(): Array<T> {
    const list = [];
    let item = this.head;

    while (item.next != this.tail) {
      item = item.next;
      list.push(item.payload);
    }

    return list;
  }


  /*
   * For ES5, for..of only works for arrays and strings.
   * To bypass this, we set `"downlevelIteration": true` and
   * polyfill Symbol.iterator.
   *
   * Refer to:
   *   https://www.typescriptlang.org/docs/handbook/release-notes/typescript-2-3.html
   *   https://github.com/Microsoft/TypeScript/issues/4031
   *   ttps://stackoverflow.com/questions/48590262/typescript-symbol-iterator
   */
  [Symbol.iterator]() {
    let item = this.head;
    const self = this;

    return {
      next() {
        item = item.next;
        if (item == self.tail) {
          return {value: undefined, done: true};
        } else {
          return {value: item.payload, done: false};
        }
      }
    };
  }

  private insertAtIndex(key: string, payload: T, index: number): Node<T> {
    const old = this.getNode(index);
    let item;

    if (old) {
      item = new Node<T>(key, payload);

      item.prev = old.prev;
      item.next = old;
      old.prev.next = item;
      old.prev = item;
    } else {
      item = this.push(key, payload);
    }

    return item;
  }

  private push(key: string, payload: T): Node<T> {
    const item = new Node<T>(key, payload);

    item.prev = this.tail.prev;
    item.next = this.tail;
    this.tail.prev.next = item;
    this.tail.prev = item;

    return item;
  }

  private removeItem(item: Node<T>) {
    if (item.prev) {
      item.prev.next = item.next;
    }
    if (item.next) {
      item.next.prev = item.prev;
    }

    delete this.map[item.key];
  }

  private getNode(index: number) {
    if (index < 0 || index >= this.size) {
      return null;
    }

    let curr = this.head;
    let i = 0;

    while (i <= index) {
      curr = curr.next;
      i++;
    }
    return curr;
  }
}
