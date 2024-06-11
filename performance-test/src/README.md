# K6 tests for _PrintPaymentNoticeService_ project

[k6](https://k6.io/) is a load testing tool. 👀 See [here](https://k6.io/docs/get-started/installation/) to install it.

- [01. Payment notice service](#01-print-payment-notice-service-service)

This is a set of [k6](https://k6.io) tests related to the _ReceiptPdfService_ initiative.

To invoke k6 test passing parameter use -e (or --env) flag:

```
-e MY_VARIABLE=MY_VALUE
```

## 01. Notice service

Test the receipt service: 

```
k6 run --env VARS=local.environment.json --env TEST_TYPE=./test-types/load.json --env TEMPLATE_ID=<template-id> <script_name>.js
```

where the mean of the environment variables is:

```json
  "environment": [
    {
      "env": "local",
      "blobStorageInstContainerID": "institutionsdatablob",
      "blobStorageLogoContainerID": "institutionslogoblob",
      "noticeServiceUri": "http://localhost:8080",
      "ciTaxCode": "000000000000"
    }
  ]
```

`blobStorageInstContainerID`: container name for institution data storage

`blobStorageLogoContainerID`: container name for institution logo storage

`noticeServiceUri`: notice service uri

`ciTaxCode`: ci tax-code to be used on data generation
