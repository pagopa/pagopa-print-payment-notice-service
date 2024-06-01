const {Given, When, Then, setDefaultTimeout} = require('@cucumber/cucumber')
const assert = require("assert");
const {call, post, formData} = require("./common");
const FormData = require("form-data");
const fs = require("fs");
const path = require('path');
const util = require('util');
const stream = require('stream');
const pipeline = util.promisify(stream.pipeline);


setDefaultTimeout(40 * 1000);

const app_host = process.env.APP_HOST;

let body;
let responseToCheck;
let pdfToCheck;
let variables = [];


Given(/^the creditor institution in the storage:$/, async function (dataTable) {
    let jsonBody = {};
    let logoPath;
    dataTable.rows().forEach(([key, value]) => {
        if (key !== 'logo') {
            jsonBody[key] = JSON.parse(value);
        } else {
            logoPath = JSON.parse(value);
        }
    });

    const fs = require('fs');
    let data = new FormData();


    data.append('institutions-data', JSON.stringify(jsonBody));
    data.append('file', fs.createReadStream(logoPath));


    const response = await formData(app_host + '/institutions/data', data);
    assert.strictEqual(response.status, 200);
});

Given(/^I have the following variables:$/, function (dataTable) {
    dataTable.rows().forEach(([key, value]) => {
        variables[key] = value;
    });

});

When(/^I send a (GET|DELETE) request to "([^"]*)"$/, async function (method, url) {
    responseToCheck = await call(method, app_host + url);
});

When(/^I send a (POST|PUT) request to "([^"]*)" with body:$/, async function (method, url, jsonBody) {
    // Replace variables in JSON body
    for (const [key, value] of Object.entries(variables)) {
        const regex = new RegExp(`<${key}>`, 'g');
        jsonBody = jsonBody.replace(regex, value);
    }
    responseToCheck = await call(method, app_host + url, jsonBody);
});

Then(/^the response should be in PDF format$/, async function () {
    assert.equal(responseToCheck.headers['content-type'], 'application/octet-stream');

    await pipeline(responseToCheck.data, fs.createWriteStream('./pdfToCheck.pdf'));

});

Then(/^the PDF document should be equal to the reference PDF "([^"]*)"$/, function (pdfName) {
    const actual = fs.createReadStream('./pdfToCheck.pdf');
    const expected = fs.createReadStream(`./resources/${pdfName}.pdf`);

    assert.strictEqual(actual.toString(), expected.toString());
});

Then(/^check response body is$/, function (payload) {
    assert.deepStrictEqual(responseToCheck.data, JSON.parse(payload));
});
Then(/^check response is a PDF with size (\d+)$/, function (size) {
    assert.equal(responseToCheck.data.length, size);
});

Then(/^the response status should be (\d+)$/, function (statusCode) {
    assert.strictEqual(responseToCheck.status, statusCode);
});

Then(/^the response list should contain a template "([^"]*)"$/, function (templateId) {
    assert(JSON.stringify(responseToCheck.data).includes(templateId));
});
