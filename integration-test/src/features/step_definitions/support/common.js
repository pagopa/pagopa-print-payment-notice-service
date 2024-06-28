const axios = require("axios");
const FormData = require("form-data");
const fs = require("fs");

axios.defaults.headers.common['Ocp-Apim-Subscription-Key'] = process.env.SUBKEY // for all requests
if (process.env.CANARY) {
    axios.defaults.headers.common['X-Canary'] = 'canary' // for all requests
}

function get(url, userId) {

    let config = {
        headers: {
            'X-User-Id': userId,
        },
   }

    return axios.get(url, config)
        .then(res => {
            console.info("STATUS");
            console.info(res);
            return res;
        })
        .catch(error => {
            console.info("ERROR");
            console.info(error.response);
            return error.response;
        });
}

function post(url, body, userId, stream) {
    let config = {
        headers: {
            'Content-Type': 'application/json',
            'X-User-Id': userId,
        },
    };

    if (stream) {
        config.responseType = 'stream';
    }

    return axios.post(url, body, config)
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });
}


function put(url, body, userId) {

    let config = {
        headers: {
            'X-User-Id': userId,
        },
   }

    return axios.put(url, body, config)
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });
}

function del(url, userId) {

    let config = {
        headers: {
            'X-User-Id': userId,
        },
   }

    return axios.delete(url, config)
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });
}

function call(method, url, body, userId = "ADMIN", stream = false) {
    if (method === 'GET') {
        return get(url, userId)
    }
    if (method === 'POST') {
        return post(url, body, userId, stream)
    }
    if (method === 'PUT') {
        return put(url, body, userId)
    }
    if (method === 'DELETE') {
        return del(url, userId)
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

function sleep(ms) {
	return new Promise(resolve => setTimeout(resolve, ms));
}



module.exports = {get, post, put, del, call, formData, sleep}
