function randomString(length, charset) {
    let res = '';
    while (length--) res += charset[(Math.random() * charset.length) | 0];
    return res;
}

export function retrieveNoticeItemData(ciTaxCode, templateId) {
    return JSON.stringify({
        "templateId": templateId,
        "data": {
          "notice": {
            "subject": "Avviso Pagamento di TEST",
            "paymentAmount": 300000,
            "dueDate": "30/12/2024",
            "code": randomString(18, '0123456789'),
            "installments": [
              {
                "code": randomString(18, '0123456789'),
                "amount": 160000,
                "dueDate": "31/12/2024"
              },
              {
                "code": randomString(18, '0123456789'),
                "amount": 140000,
                "dueDate": "31/12/2025"
              }
            ]
          },
          "creditorInstitution": {
            "taxCode": ciTaxCode
          },
          "debtor": {
            "taxCode": "FFFCST83A15L113V",
            "fullName": "Mario Rossi",
            "address": "Via Nazionale",
            "postalCode": "00100",
            "city": "Roma",
            "buildingNumber": "1",
            "province": "RM"
          }
        }
  });
}
