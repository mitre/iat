/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/

import { ServiceStatus } from "../types/service-response";

export interface DualPDMCompareResponse {
    probe: Uint8Array;
    candidate: Uint8Array;
    status: ServiceStatus;
}

// Consuming Endpoint: /api/service/dualpdm/response/{imageId} in DualPDMResponse.java
export class DualDeformedImage {
    imageBytes: number[];

    constructor(
        imageBytes: number[],
    ) {
        this.imageBytes = imageBytes;
    }
}
