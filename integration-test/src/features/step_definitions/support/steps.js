const {Given, When, Then, setDefaultTimeout} = require('@cucumber/cucumber')
const assert = require("assert");
const {call, post} = require("./common");

setDefaultTimeout(40 * 1000);

const app_host = process.env.APP_HOST;

let body;
let responseToCheck;

Given(/^initial json$/, function (payload) {
    body = JSON.parse(payload);
});

When(/^the client send (GET|POST|PUT|DELETE) to (.*)$/, async function (method, url) {
    responseToCheck = await call(method, app_host + url, body);
});

Then(/^check statusCode is (\d+)$/, function (status) {
    assert.strictEqual(responseToCheck.status, status);

});

Then(/^check response body is$/, function (payload) {
    assert.deepStrictEqual(responseToCheck.data, JSON.parse(payload));
});
Then(/^check response is a PDF with size (\d+)$/, function (size) {
    assert.equal(responseToCheck.data.length, size);
});
