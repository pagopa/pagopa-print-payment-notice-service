import { BlobServiceClient } from "@azure/storage-blob";
const { MongoClient, ObjectId } = require('mongodb');
import { createRequire } from 'node:module';
import http from 'k6/http';

const require = createRequire(import.meta.url);

//ENVIRONMENTAL VARIABLES
const blobStorageConnString = process.env.BLOB_INSTITUTIONS_CONN_STRING;
const blobNoticesConnString = process.env.BLOB_NOTICES_CONN_STRING;
const mongoDbConnString = process.env.NOTICES_MONGO_CONN_STRING;


const environmentString = process.env.ENVIRONMENT_STRING || "local";
let environmentVars = require(`../${environmentString}.environment.json`)?.environment?.[0] || {};

const blobStorageInstContainerID = environmentVars.blobStorageInstContainerID;
const blobStorageLogoContainerID = environmentVars.blobStorageLogoContainerID;
const blobStorageNoticeContainerID = environmentVars.blobStorageNoticeContainerID;

//CONSTANTS
export const CI_TAX_CODE = environmentVars.ciTaxCode;
export const NOTICES_DB = environmentVars.noticesDb;
export const NOTICES_COLLECTION = environmentVars.noticesCollection;

//CLIENTS
const blobServiceClient = BlobServiceClient.fromConnectionString(
    blobStorageConnString || ""
);
export const blobInstContainerClient = blobServiceClient.getContainerClient(
    blobStorageInstContainerID || ""
);
export const blobLogoContainerClient = blobServiceClient.getContainerClient(
    blobStorageLogoContainerID || ""
);
const blobNoticesClient = BlobServiceClient.fromConnectionString(
    blobNoticesConnString || ""
);
export const noticesContainerClient = blobNoticesClient.getContainerClient(
    blobStorageNoticeContainerID || ""
);

export const noticesMongoClient = new MongoClient(mongoDbConnString);
