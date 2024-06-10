import { check } from 'k6';
import { generateSingleNotice } from './modules/notice_service_client.js';
import { SharedArray } from 'k6/data';
import { retrieveNoticeItemData } from './modules/common.js';

const varsArray = new SharedArray('vars', function () {
    return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
export const ENV_VARS = varsArray[0];
export let options = JSON.parse(open(__ENV.TEST_TYPE));

let attachmentUrl = "";

const  = varsArray[0];
const noticeServiceUri = `${vars.noticeServiceUri}`;
const subKey = `${__ENV.SUBSCRIPTION_KEY}`;
const templateId = `${__ENV.TEMPLATE_ID}`;
const ciTaxCode = `${vars.ciTaxCode}`;

export default function () {

      let inputData = retrieveInputData(ciTaxCode, templateId);
      let response = generateSingleNotice(pdfEngineUri, subKey, inputData);

      console.log("Generate PDF call, Status " + response.status);

      check(response, {
        'Generate PDF status is 200': (response) => response.status === 200,
        'Generate PDF content_type is the expected one':
         (response) => response.headers["Content-Type"] === "application/pdf",
        'Generate PDF body not null': (response) => response.body !== null
      });

}
