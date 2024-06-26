import { blobInstContainerClient, blobLogoContainerClient, CI_TAX_CODE } from "./scripts_common.js";

//UPLOAD Data TO BLOB STORAGE
const uploadDocumentToAzure = async () => {

  const blockBlobLogoClient = blobLogoContainerClient.getBlockBlobClient(CI_TAX_CODE+"/logo.png");
  const logoResponse = await blockBlobLogoClient.uploadFile("./resources/logo.png");
  if (logoResponse._response.status !== 201) {
    throw new Error(
      `Error uploading logo ${blockBlobLogoClient.name} to container ${blockBlobLogoClient.containerName}`
    );
  }

  const blockBlobClient = blobInstContainerClient.getBlockBlobClient(CI_TAX_CODE+"/data.json");
  const instResponse = await blockBlobClient.uploadFile("./resources/data.json");
  if (instResponse._response.status !== 201) {
    throw new Error(
      `Error uploading document ${blockBlobClient.name} to container ${blockBlobClient.containerName}`
    );
  }

  return instResponse;
};

uploadDocumentToAzure().then(resp => {
  console.info("RESPONSE SAVE DATA", resp._response.status);
}) ;
