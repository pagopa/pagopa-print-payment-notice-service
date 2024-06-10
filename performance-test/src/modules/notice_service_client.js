import http from 'k6/http';

export function generateSingleNotice(noticeServiceUri, subKey, inputData) {

    let headers = {
        'Ocp-Apim-Subscription-Key': subKey,
        'X-User-Id': inputData.creditorInstitution.taxCode
    };

    return http.post(noticeServiceUri+"/notices/generate", inputData, {headers, responseType: "text"});

}

export function generateMassiveNotice(noticeServiceUri, subKey, inputData) {

    let headers = {
        'Ocp-Apim-Subscription-Key': subKey,
        'X-User-Id': inputData[0].creditorInstitution.taxCode
    };

    return http.post(noticeServiceUri+"/notices/generate-massive", inputData, {headers, responseType: "text"});

}

export function getNoticeRequest(noticeServiceUri, subKey, folderId) {

    let headers = {
        'Ocp-Apim-Subscription-Key': subKey,
        'X-User-Id': inputData.creditorInstitution.taxCode
    };

    return http.get(`${noticeServiceUri}/notices/folder/${folderId}/status`,
     {headers, responseType: "application/json"});

}
