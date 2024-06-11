import http from 'k6/http';

export function generateSingleNotice(noticeServiceUri, subKey, inputData, folderId) {

    let headers = {
        'Ocp-Apim-Subscription-Key': subKey,
        'X-User-Id': inputData.data.creditorInstitution.taxCode,
	    'Content-Type': 'application/json'
    };

    return folderId !== null ?
        http.post(noticeServiceUri+`/notices/generate?folderId=${folderId}`,
        folderId, JSON.stringify(inputData), {headers}) :
        http.post(noticeServiceUri+"/notices/generate",
         JSON.stringify(inputData), {headers, responseType: "text"});

}

export function generateMassiveNotice(noticeServiceUri, subKey, inputData, userId) {

    let headers = {
        'Ocp-Apim-Subscription-Key': subKey,
        'X-User-Id': userId,
        'Content-Type': 'application/json'
    };

    return http.post(`${noticeServiceUri}/notices/generate-massive`, JSON.stringify(inputData), {headers, responseType: "text"});

}

export function getNoticeRequest(noticeServiceUri, subKey, folderId, userId) {

    let headers = {
        'Ocp-Apim-Subscription-Key': subKey,
        'X-User-Id': userId
    };

    return http.get(`${noticeServiceUri}/notices/folder/${folderId}/status`,
        {headers, responseType: "text"});

}

export function deleteNoticeRequest(noticeServiceUri, subKey, folderId, userId) {

    let headers = {
        'Ocp-Apim-Subscription-Key': subKey,
        'X-User-Id': userId,
        'Content-Type': 'application/json'
    };

    return http.del(`${noticeServiceUri}/notices/folder/${folderId}`,
        {headers});

}
