const axios = require("axios");
const FormData = require("form-data");
const fs = require("fs");

axios.defaults.headers.common['Ocp-Apim-Subscription-Key'] = process.env.SUBKEY // for all requests
if (process.env.CANARY) {
    axios.defaults.headers.common['X-Canary'] = 'canary' // for all requests
}

function get(url) {

    return axios.get(url)
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });
}

function post(url, body) {
    let config = {
        headers: {
            'Content-Type': 'application/json',
        },
        responseType: 'stream'
    };
    return axios.post(url, body, config)
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });
}

function put(url, body) {
    return axios.put(url, body)
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });
}

function del(url) {
    return axios.delete(url)
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });
}

function call(method, url, body) {
    if (method === 'GET') {
        return get(url)
    }
    if (method === 'POST') {
        return post(url, body)
    }
    if (method === 'PUT') {
        return put(url, body)
    }
    if (method === 'DELETE') {
        return del(url)
    }

}


function formData(url, data) {
    let config = {
        method: 'post',
        maxBodyLength: Infinity,
        url: url,
        headers: {
            'Content-Type': 'multipart/form-data',
            'Accept': 'application/json',
            ...data.getHeaders()
        },
        data: data,
    };

    return axios.request(config)
        .then((response) => {
            return response
        })
        .catch((error) => {
            return error.response;
        });
}


module.exports = {get, post, put, del, call, formData}
