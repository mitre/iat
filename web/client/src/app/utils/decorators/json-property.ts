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

const JSON_METADATA_KEY = 'jsonProperty';

// Taken from http://cloudmark.github.io/Json-Mapping/
interface IJsonMetaData<T> {
  name?: string;
  clazz?: new() => T;
}

function JsonProperty<T>(metadata?: IJsonMetaData<T> | string): any {
  if (metadata instanceof String || typeof metadata === 'string') {
    return Reflect.metadata(JSON_METADATA_KEY, {
      name: metadata,
      clazz: undefined
    });
  }

  const metadataObj = <IJsonMetaData<T>>metadata;
  return Reflect.metadata(JSON_METADATA_KEY, {
    name: metadataObj ? metadataObj.name : undefined,
    clazz: metadataObj ? metadataObj.clazz : undefined
  });
}

function getClazz(target: any, propertyKey: string): any {
  return Reflect.getMetadata('design:type', target, propertyKey);
}

function getJsonProperty<T>(target: any, propertyKey: string): IJsonMetaData<T> {
  return Reflect.getMetadata(JSON_METADATA_KEY, target, propertyKey);
}

export {
  JSON_METADATA_KEY,
  IJsonMetaData,
  JsonProperty,
  getClazz,
  getJsonProperty
};
