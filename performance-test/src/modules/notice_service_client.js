import http from 'k6/http';

export function generateSingleNotice(noticeServiceUri, subKey, inputData) {

    let headers = {
        'Ocp-Apim-Subscription-Key': subKey
    };

    return http.post(noticeServiceUri+"/notices/generate", inputData, {headers, responseType: "text"});

}
