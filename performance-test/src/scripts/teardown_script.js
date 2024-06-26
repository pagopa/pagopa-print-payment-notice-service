import {
    blobInstContainerClient, blobLogoContainerClient, noticesContainerClient, noticesMongoClient,
     CI_TAX_CODE, NOTICES_DB, NOTICES_COLLECTION
} from "./scripts_common.js";

//DELETE CI DATA FROM BLOB STORAGE
const deleteDocumentFromAzure = async () => {
    const instResponse = await blobInstContainerClient.deleteBlob(CI_TAX_CODE+'/data.json');
    if (instResponse._response.status !== 202 && instResponse._response.status !== 404) {
        throw new Error(`Error deleting data ${CI_TAX_CODE}`);
    }

    const logoResponse = await blobLogoContainerClient.deleteBlob(CI_TAX_CODE+'/logo.png');
    if (logoResponse._response.status !== 202 && logoResponse._response.status !== 404) {
        throw new Error(`Error deleting logo ${CI_TAX_CODE}`);
    }

    return logoResponse;
};

deleteDocumentFromAzure().then((res) => {
    console.log("RESPONSE DELETE CI DATA STATUS", res._response.status);
});

const deleteTestFolderData = async () => {
    await noticesMongoClient.connect();
    const collection = noticesMongoClient.db(NOTICES_DB).collection(NOTICES_COLLECTION);
    const findFoldersByUserId = {
        userId: CI_TAX_CODE
    };
    const noticesToDelete = await collection.find(findFoldersByUserId).toArray();

    console.log(noticesToDelete);

    for await (const noticeToDelete of noticesToDelete) {
      let iter1 = noticesContainerClient.listBlobsByHierarchy("/", { prefix: noticeToDelete._id+"/" });
      for await (const item of iter1) {
        if (item.kind !== "prefix") {
          await noticesContainerClient.deleteBlob(item.name);
        }
      }
      await collection.deleteOne({ "_id": noticeToDelete._id });
    }

   noticesMongoClient.close();

}

deleteTestFolderData().then((res) => {
    console.log("RESPONSE DELETE FOLDER DATA COMPLETED");
});
