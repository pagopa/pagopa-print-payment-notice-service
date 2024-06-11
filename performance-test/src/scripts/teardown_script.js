import {
    blobInstContainerClient, blobLogoContainerClient, noticesContainerClient,
     CI_TAX_CODE, NOTICES_DB, NOTICES_COLLECTION
} from "./scripts_common.js";

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

const deleteTestFolderData = async () => {
    await client.connect();
    const collection = client.db(NOTICES_DB).collection(NOTICES_COLLECTION);
    const findFoldersByUserId = {
        userId: CI_TAX_CODE
    };
    const noticesToDelete = await collection.find(findFoldersByUserId).toArray();

    noticesToDelete.forEach(function(data) {
      let iter1 = containerClient.listBlobsByHierarchy("/", { prefix: data.id+"/" });
      for await (const item of iter1) {
        if (item.kind !== "prefix") {
          await blobInstContainerClient.deleteBlob(item.name);
        }
      }
      await collection.deleteOne({ "_id": data.id });
    });

}
