const {Given, When, Then, After, setDefaultTimeout} = require('@cucumber/cucumber')
const assert = require("assert");
const {call, post, formData, sleep} = require("./common");
const FormData = require("form-data");
const fs = require("fs");
const util = require('util');
const stream = require('stream');
const pipeline = util.promisify(stream.pipeline);
const pdf2html = require('pdf2html');


setDefaultTimeout(40 * 1000);

const app_host = process.env.APP_HOST;

let responseToCheck = null;
let folderId = null;
let ciTaxCode = null;
let signedUrl = null;
let variables = [];


// After each Scenario
After(async function () {
    // remove folder
    if (this.folderId != null) {
        await call('DELETE', app_host + '/folder' + folderId, {
            'X-User-Id': ciTaxCode ?? 'ADMIN'
        });
    }
    this.folderId = null;
    this.responseToCheck = null;
    this.ciTaxCode = null;
    this.signedUrl = null;
});


Given(/^the creditor institution in the storage:$/, async function (dataTable) {
    let jsonBody = {};
    let logoPath;
    dataTable.rows().forEach(([key, value]) => {
        if (key !== 'logo') {
            jsonBody[key] = JSON.parse(value);
            if (key === 'taxCode') {
                ciTaxCode = JSON.parse(value);
            }
        } else {
            logoPath = JSON.parse(value);
        }
    });
    console.log(jsonBody);
    console.log(JSON.stringify(jsonBody));

    let data = new FormData();

    data.append('institutions-data', JSON.stringify(jsonBody));
    data.append('file', fs.createReadStream(logoPath));


    const response = await formData(app_host + '/institutions/data', data);
    console.log(response);
    assert.strictEqual(response !== null && response !== undefined, true);
    assert.strictEqual(response.hasOwnProperty('status'), true);
    assert.strictEqual(response.status, 200);
});

Given(/^I have the following variables:$/, function (dataTable) {
    dataTable.rows().forEach(([key, value]) => {
        variables[key] = value;
    });

});

When(/^I send a (GET|DELETE) request to "([^"]*)"$/, async function (method, url) {
    responseToCheck = await call(method, app_host + url, ciTaxCode, {'X-User-Id': "ADMIN"});
});

When(/^I send a (POST|PUT) request to "([^"]*)" with body:$/, async function (method, url, jsonBody) {
    // Replace variables in JSON body
    for (const [key, value] of Object.entries(variables)) {
        const regex = new RegExp(`<${key}>`, 'g');
        jsonBody = jsonBody.replace(regex, value);
    }
    console.log(jsonBody);
    let idempotencyKey = (Math.random() + 1).toString(36).substring(7);
    console.log(idempotencyKey);
    responseToCheck = await call(method, app_host + url, jsonBody, {
        'X-User-Id': ciTaxCode ?? 'ADMIN',
        'Idempotency-Key': idempotencyKey
    }, true);
});

When(/^I send a POST request to "([^"]*)" with idempotency_key "([^"]*)" and with body:$/, async function (method, url, idempotencyKey, jsonBody) {
    // Replace variables in JSON body
    for (const [key, value] of Object.entries(variables)) {
        const regex = new RegExp(`<${key}>`, 'g');
        jsonBody = jsonBody.replace(regex, value);
    }
    console.log(jsonBody);
    responseToCheck = await call(method, app_host + url, jsonBody, {
        'X-User-Id': ciTaxCode ?? 'ADMIN',
        'Idempotency-Key': idempotencyKey
    }, true);
});

When(/^I send a (POST|PUT) request to "([^"]*)" without stream, with body:$/, async function (method, url, jsonBody) {
    // Replace variables in JSON body
    for (const [key, value] of Object.entries(variables)) {
        const regex = new RegExp(`<${key}>`, 'g');
        jsonBody = jsonBody.replace(regex, value);
    }
    console.log(jsonBody);
    let idempotencyKey = (Math.random() + 1).toString(36).substring(7);
    console.log(idempotencyKey);
    responseToCheck = await call(method, app_host + url, jsonBody, {
        'X-User-Id': ciTaxCode ?? 'ADMIN',
        'Idempotency-Key': idempotencyKey
    }, false);
});

Then(/^the response should be in PDF format$/, async function () {
    assert.strictEqual(responseToCheck !== null && responseToCheck !== undefined, true);
    assert.strictEqual(responseToCheck.hasOwnProperty('headers'), true);
    assert.strictEqual(responseToCheck.headers.hasOwnProperty('content-type'), true);
    assert.equal(responseToCheck.headers['content-type'], 'application/pdf');

    await pipeline(responseToCheck.data, fs.createWriteStream('./pdfToCheck.pdf'));

});

Then(/^the PDF document should be equal to the reference PDF "([^"]*)"$/, async function (pdfName) {

    // fs.copyFileSync('./pdfToCheck.pdf', `./resources/${pdfName}`);
    let html1 = await pdf2html.html('./pdfToCheck.pdf');
    let html2 = await pdf2html.html(`./resources/${pdfName}`);

    // we need to remove some metadata (like createDate) to check only the body of the file
    const timestampPattern = /\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z/g;
    const metaPattern = /<meta name="resourceName" content=".*\.pdf"\/>/g;
    const metaLengthPattern = /<meta name="Content-Length" content=".*"\/>/g;

    html1 = html1.replace(timestampPattern, '');
    html1 = html1.replace(metaPattern, '');
    html1 = html1.replace(metaLengthPattern, '');

    html2 = html2.replace(timestampPattern, '');
    html2 = html2.replace(metaPattern, '');
    html2 = html2.replace(metaLengthPattern, '');

    assert.equal(html1, html2);
});

Then(/^check response body is$/, function (payload) {
    assert.strictEqual(responseToCheck !== null && responseToCheck !== undefined, true);
    assert.strictEqual(responseToCheck.hasOwnProperty('data'), true);
    assert.deepStrictEqual(responseToCheck.data, JSON.parse(payload));
});

Then(/^the response status should be (\d+)$/, function (statusCode) {
    // console.log(responseToCheck);
    assert.strictEqual(responseToCheck !== null && responseToCheck !== undefined, true);
    assert.strictEqual(responseToCheck.hasOwnProperty('status'), true);
    assert.strictEqual(responseToCheck.status, statusCode);
});

Then(/^the response should contain "([^"]*)"$/, function (templateId) {
    assert.strictEqual(responseToCheck !== null && responseToCheck !== undefined, true);
    assert.strictEqual(responseToCheck.hasOwnProperty('data'), true);
    assert(JSON.stringify(responseToCheck.data).includes(templateId));
});

Then(/^the response list should contain a template "([^"]*)"$/, function (templateId) {
    assert.strictEqual(responseToCheck !== null && responseToCheck !== undefined, true);
    assert.strictEqual(responseToCheck.hasOwnProperty('data'), true);
    assert(JSON.stringify(responseToCheck.data).includes(`"${templateId}"`));
});

Then(/^the response should be in JSON format$/, async function () {
    assert.strictEqual(responseToCheck !== null && responseToCheck !== undefined, true);
    assert.strictEqual(responseToCheck.hasOwnProperty('headers'), true);
    assert.strictEqual(responseToCheck.headers.hasOwnProperty('content-type'), true);
    assert.equal(responseToCheck.headers['content-type'], 'application/json');
});

Then('the response should contain the folderId', function () {
    assert.strictEqual(responseToCheck !== null && responseToCheck !== undefined, true);
    assert.strictEqual(responseToCheck.hasOwnProperty('data'), true);
    let data = responseToCheck.data;
    assert.strictEqual(data.hasOwnProperty("folder_id"), true);
    folderId = data["folder_id"];
});

Then('the request is in status {string} after {int} ms', async function (status, time) {
    // boundary time spent by azure function to process event
    await sleep(time);
    responseToCheck = await call('GET', app_host + '/notices/folder/' + folderId + '/status', null, {
        'X-User-Id': ciTaxCode ?? 'ADMIN'
    });
    console.log(responseToCheck)
    assert.strictEqual(responseToCheck !== null && responseToCheck !== undefined, true);
    assert.strictEqual(responseToCheck.hasOwnProperty('status'), true);
    assert.strictEqual(responseToCheck.status, 200);
    assert.strictEqual(responseToCheck.hasOwnProperty('data'), true);
    assert.strictEqual(responseToCheck.data.status, status);
});

Then('download url is recoverable with the folderId', async function () {
    // boundary time spent by azure function to process event
    responseToCheck = await call('GET', app_host + '/notices/folder/' + folderId + '/url', null, {
        'X-User-Id': ciTaxCode ?? 'ADMIN'
    });
    assert.strictEqual(responseToCheck !== null && responseToCheck !== undefined, true);
    assert.strictEqual(responseToCheck.hasOwnProperty('status'), true);
    assert.strictEqual(responseToCheck.status, 200);
    assert.strictEqual(responseToCheck.hasOwnProperty('data'), true);
    assert.strictEqual(responseToCheck.data.hasOwnProperty('signedUrl'), true);
    signedUrl = responseToCheck.data.signedUrl;
});

Then('can download content using signedUrl', async function () {
    // boundary time spent by azure function to process event
    responseToCheck = await call('GET', signedUrl, null, {
        'X-User-Id': ciTaxCode ?? 'ADMIN'
    });
    assert.strictEqual(responseToCheck !== null && responseToCheck !== undefined, true);
    assert.strictEqual(responseToCheck.hasOwnProperty('headers'), true);
    assert.strictEqual(responseToCheck.headers.hasOwnProperty('content-type'), true);
    assert.equal(responseToCheck.headers['content-type'], 'application/pdf');
});

Then('the request is not in status {string} after {int} ms', async function (status, time) {
    // boundary time spent by azure function to process event
    await sleep(time);
    responseToCheck = await call('GET', app_host + '/notices/folder/' + folderId + '/status', null, {
        'X-User-Id': ciTaxCode ?? 'ADMIN'
    });
    console.log(responseToCheck)
    assert.strictEqual(responseToCheck !== null && responseToCheck !== undefined, true);
    assert.strictEqual(responseToCheck.hasOwnProperty('status'), true);
    assert.strictEqual(responseToCheck.status, 200);
    assert.strictEqual(responseToCheck.hasOwnProperty('data'), true);
    assert.notEqual(responseToCheck.data.status, status);
});


Then('error is recoverable with response errorId', async function () {
    assert.strictEqual(responseToCheck.data.hasOwnProperty('noticesInError'), true);
    // boundary time spent by azure function to process event
    responseToCheck = await call('GET', app_host + '/notices/folder/' + folderId + '/error/'+ errorId, null, {
        'X-User-Id': ciTaxCode ?? 'ADMIN'
    });
    console.log(responseToCheck)
    assert.strictEqual(responseToCheck !== null && responseToCheck !== undefined, true);
    assert.strictEqual(responseToCheck.hasOwnProperty('status'), true);
    assert.strictEqual(responseToCheck.status, 200);
    assert.strictEqual(responseToCheck.hasOwnProperty('data'), true);
});
