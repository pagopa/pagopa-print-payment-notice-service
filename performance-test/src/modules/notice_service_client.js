import http from 'k6/http';

export function generateSingleNotice(noticeServiceUri, subKey, inputData, folderId) {

    let headers = {
        'Ocp-Apim-Subscription-Key': subKey,
        'X-User-Id': inputData.data.creditorInstitution.taxCode,
        'Content-Type': 'application/json'
    };

    let path = noticeServiceUri + `/notices/generate`;
    if (folderId) {
        path = path + `?folderId=${folderId}`;
    }
    console.log(`POST ${path}`, inputData);
    console.log(`k=`, subKey.charAt(0));

    return folderId !== null ?
        http.post(path, folderId, JSON.stringify(inputData), {
            headers,
            timeout: '180s'
        }) :
        http.post(noticeServiceUri + "/notices/generate",
            JSON.stringify(inputData),
            {
                headers,
                responseType: "text",
                timeout: '180s'
            });

}

export function generateMassiveNotice(noticeServiceUri, subKey, inputData, userId) {
    let idempotencyKey = (Math.random() + 1).toString(36).substring(7);

    let headers = {
        'Ocp-Apim-Subscription-Key': subKey,
        'X-User-Id': userId,
        'Content-Type': 'application/json',
        'Idempotency-Key': idempotencyKey
    };

    return http.post(`${noticeServiceUri}/notices/generate-massive`, JSON.stringify(inputData), {
        headers,
        responseType: "text"
    });

}

export function getNoticeRequest(noticeServiceUri, subKey, folderId, userId) {

    let headers = {
        'Ocp-Apim-Subscription-Key': subKey,
        'X-User-Id': userId
    };

    return http.get(`${noticeServiceUri}/notices/folder/${folderId}/status`,
        {
            headers,
            responseType: "text"
        });

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
