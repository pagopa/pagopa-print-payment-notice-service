import {check, sleep} from 'k6';
import {generateMassiveNotice, getNoticeRequest} from './modules/notice_service_client.js';
import {SharedArray} from 'k6/data';
import {retrieveNoticeItemData} from './modules/common.js';

const varsArray = new SharedArray('vars', function () {
    return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
export let options = JSON.parse(open(__ENV.TEST_TYPE));

let attachmentUrl = "";

const vars = varsArray[0];
const noticeServiceUri = `${vars.noticeServiceUri}`;
const subKey = `${__ENV.SUBSCRIPTION_KEY}`;
const templateId = `${__ENV.TEMPLATE_ID}`;
const ciTaxCode = `${vars.ciTaxCode}`;
const processTime = `${__ENV.PROCESS_TIME >= 0 ? __ENV.PROCESS_TIME : 3}`;
const numberOfElements = `${__ENV.NUMBER_OF_MASSIVE_ELEMENTS >= 0 ? __ENV.NUMBER_OF_MASSIVE_ELEMENTS : 10}`;


function postcondition(folderId) {

    let response = getNoticeRequest(noticeServiceUri, subKey, folderId, ciTaxCode);

    console.log("Get Notice Request call, Status " + response.status);

    check(response, {
        'Get Notice Request status is 200': (response) => response.status === 200,
        'Get Notice Request content_type is the expected one':
            (response) => response.headers["Content-Type"] === "application/json",
        'Get Notice Request not null and with status PROCESSED':
            (response) => response.body !== null && JSON.parse(response.body).status === "PROCESSED"
    });

}

export default function () {

    const notices = [];

    for (let i = 0; i < numberOfElements; i++) {
        notices.push(retrieveNoticeItemData(ciTaxCode, templateId));
    }

    let response = generateMassiveNotice(noticeServiceUri, subKey, {"notices": notices}, ciTaxCode);

    console.log("Generate Notice call. ", response.status, response.body);

    check(response, {
        'Generate PDF status is 200': (response) => response.status === 200,
        'Generate PDF content_type is the expected one':
            (response) => response.headers["Content-Type"] === "application/json",
        'Generate Massive Request body not null': (response) => response.body !== null
    });

    sleep(processTime * numberOfElements);
    postcondition(JSON.parse(response.body).folderId);

}
