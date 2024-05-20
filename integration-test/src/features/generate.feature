Feature: Single Notice

  Scenario: Creation OK
    Given initial json
    """
      {
      "templateId": "TemplateSingleInstalment",
      "data": {
          "notice": {
              "subject": "Trasporto Scolastico A.S. 2021/2022",
              "paymentAmount": 152,
              "dueDate": "24/10/2024",
              "code": "123456789012345688",
              "posteAuth": "AUT. 08/5 S3/81 53079 08129.07.20211",
              "posteDocumentType": "876",
              "installments": [
                  {
                      "code": "123456789012345688",
                      "amount": 100,
                      "dueDate": "24/09/2024",
                      "posteAuth": "AUT. 08/5 S3/81 53079 08129.07.20211",
                      "posteDocumentType": "876"
                  }
              ]
          },
          "creditorInstitution": {
              "taxCode": "82001760676"
          },
          "debtor": {
              "taxCode": "rccmrz88A52C409A",
              "fullName": "MARZIA ROCCARASO",
              "address": "Corso Sempione",
              "postalCode": "20149",
              "city": "Milano",
              "buildingNumber": "55",
              "province": "MI"
          }
      }
    }
    """
    When the client send POST to /notices/generate
    Then check statusCode is 201
