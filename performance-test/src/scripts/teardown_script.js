import { blobInstContainerClient, blobLogoContainerClient, CI_TAX_CODE } from "./scripts_common.js";

//DELETE CI DATA FROM BLOB STORAGE
const deleteDocumentFromAzure = async () => {
    const response = await blobInstContainerClient.deleteBlob(CI_TAX_CODE+'/data.json');
    if (response._response.status !== 202) {
        throw new Error(`Error deleting data ${CI_TAX_CODE}`);
    }

    response = await blobLogoContainerClient.deleteBlob(CI_TAX_CODE+'/logo.png');
    if (response._response.status !== 202) {
        throw new Error(`Error deleting logo ${CI_TAX_CODE}`);
    }

    return response;
};

deleteDocumentFromAzure().then((res) => {
    console.log("RESPONSE DELETE CI DATA STATUS", res._response.status);
});
