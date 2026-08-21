/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import 'reflect-metadata';

import { getJsonProperty, getClazz } from './decorators/json-property';

export class MapUtils {
  static isPrimitive(obj) {
    switch (typeof obj) {
      case 'string':
      case 'number':
      case 'boolean':
        return true;
    }
    return !!(obj instanceof String || obj === String ||
      obj instanceof Number || obj === Number ||
      obj instanceof Boolean || obj === Boolean);
  }

  static isArray(obj) {
    if (obj === Array) {
      return true;
    } else if (typeof Array.isArray === 'function') {
      return Array.isArray(obj);
    }
    return !!(obj instanceof Array);
  }

  static deserialize<T>(clazz: new() => T, jsonObject) {
    if (clazz === undefined || jsonObject === undefined) {
      return undefined;
    }
    const obj = new clazz();

    Object.keys(obj).forEach(key => {
      const propertyMetadataFn: (IJsonMetadata) => any = (propertyMetadata) => {
        const propertyName = propertyMetadata.name || key;
        const innerJson = jsonObject ? jsonObject[propertyName] : undefined;

        const clz = getClazz(obj, key);

        if (MapUtils.isArray(clz)) {
          const metadata = getJsonProperty(obj, key);
          if (metadata.clazz || MapUtils.isPrimitive(clz)) {
            if (innerJson && MapUtils.isArray(innerJson)) {
              return innerJson.map(item => MapUtils.deserialize(metadata.clazz, item));
            } else {
              return undefined;
            }
          } else {
            return innerJson;
          }
        } else if (!MapUtils.isPrimitive(clz)) {
          return MapUtils.deserialize(clz, innerJson);
        }
        return jsonObject ? jsonObject[propertyName] : undefined;
      };

      const pMD = getJsonProperty(obj, key);

      if (pMD) {
        obj[key] = propertyMetadataFn(pMD);
      } else if (jsonObject && jsonObject[key] !== undefined) {
        obj[key] = jsonObject[key];
      }
    });

    return obj;
  }
}
