/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

/**
 * Utility function to delay (or debounce) the firings of callback handlers during persistent identical events
 *
 * @param func - callback function to fire once the "debounce period" is over (twin event has no fired X amount of time)
 * @param wait - the "debounce period" in ms
 * @param immediate - if true, fires callback first, then debounces sequential events until "debounce period" expires
 * @returns
 * @private
 */
// taken from https://github.com/component/debounce/blob/master/index.js
export const debounce = (func: () => void, wait?: number, immediate?: boolean) => {
  let timestamp; let timeout; let result; let context; let args;

  if (wait == null) {
    wait = 100;
}

  const delay = function() {
    const last = Date.now() - timestamp;

    if (last < wait && last >= 0) {
      timeout = setTimeout(delay, wait - last);
    } else {
      timeout = null;
      if (!immediate) {
        result = func.apply(context, args);
        context = args = null;
      }
    }
  };

  const d = function() {
    context = this;
    args = arguments;
    timestamp = Date.now();
    const callNow = immediate && !timeout;
    if (!timeout) {
timeout = setTimeout(delay, wait);
}
    if (callNow) {
      result = func.apply(context, args);
      context = args = null;
    }

    return result;
  };

  return d;
};
