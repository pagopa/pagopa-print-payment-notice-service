import { sleep, check } from 'k6';
import { generateMassiveNotice, getNoticeRequest, deleteNoticeRequest } from './modules/notice_service_client.js';
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
const numberOfElements = `${vars.numberOfElements}`;

function postcondition(folderId) {

    let response = getNoticeRequest(noticeServiceUri, subKey, folderId);

    check(response, {
      'Generate PDF status is 200': (response) => response.status === 200,
      'Generate PDF content_type is the expected one':
       (response) => response.headers["Content-Type"] === "application/json",
      'Generate Massive Request request not null and with status PROCESSED':
        (response) => response.body !== null && response.body.status === "PROCESSED"
    });

    deleteNoticeRequest(noticeServiceUri, folderId);

}

export default function () {

      const notices = [];

      for (let i = 0; i < numberOfElements; i++) {
        notices.push(retrieveInputData(ciTaxCode, templateId));
      }

      let response = generateMassiveNotice(noticeServiceUri, subKey, notices);

      console.log("Generate PDF call, Status " + response.status);

      check(response, {
        'Generate PDF status is 200': (response) => response.status === 200,
        'Generate PDF content_type is the expected one':
         (response) => response.headers["Content-Type"] === "application/json",
        'Generate Massive Request body not null': (response) => response.body !== null
      });

	  sleep(processTime);
	  postcondition(response.body);

}
