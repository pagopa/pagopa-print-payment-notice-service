import { blobInstContainerClient, blobLogoContainerClient, CI_TAX_CODE } from "./scripts_common.js";

//UPLOAD Data TO BLOB STORAGE
const uploadDocumentToAzure = async () => {

  const blockBlobLogoClient = blobLogoContainerClient.getBlockBlobClient(CI_TAX_CODE+"/logo.png");
  const response = await blockBlobClient.uploadFile("./resources/logo.png");
  if (response._response.status !== 201) {
    throw new Error(
      `Error uploading logo ${blockBlobClient.name} to container ${blockBlobClient.containerName}`
    );
  }

  const blockBlobClient = blobInstContainerClient.getBlockBlobClient(CI_TAX_CODE+"/data.json");
  const response = await blockBlobClient.uploadFile("./resources/data.json");
  if (response._response.status !== 201) {
    throw new Error(
      `Error uploading document ${blockBlobClient.name} to container ${blockBlobClient.containerName}`
    );
  }

  return response;
};

uploadDocumentToAzure().then(resp => {
  console.info("RESPONSE SAVE DATA", resp._response.status);
}) ;
