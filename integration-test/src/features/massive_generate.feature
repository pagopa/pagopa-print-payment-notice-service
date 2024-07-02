Feature: Massive Generation

  Scenario: MT_01_SingleTemplates_COMPLETED - Generazione Massiva con successo su singolo template, unico elemento
    Given the creditor institution in the storage:
      | variableName       | value                                  |
      | taxCode            | "80034390585"                          |
      | fullName           | "Comune di Test"                       |
      | organization       | "Settore di Test"                      |
      | info               | "Info di Test"                         |
      | webChannel         | true                                   |
      | appChannel         | false                                  |
      | physicalChannel    | "Canale Fisico"                        |
      | cbill              | "CBI1234"                              |
      | posteAccountNumber | "000000123456"                         |
      | posteAuth          | "AUT. 08/5 S3/81 53079 08129.07.20211" |
      | logo               | "./resources/logo1.png"                |
    When I send a GET request to "/notices/templates"
    Then the response status should be 200
    And the response should contain "TemplateSingleInstalment"
    Given I have the following variables:
      | variableName               | value                      |
      | Avviso.Oggetto             | "Avviso Pagamento di TEST" |
      | Avviso.Importo             | 450000                     |
      | Avviso.Data                | "31/12/2024"               |
      | Avviso.Codice              | "470000008800999050"       |
      | Ente.CF                    | "80034390585"              |
      | Destinatario.CF            | "FFFCST83A15L113V"         |
      | Destinatario.NomeCompleto  | "Mario Rossi"              |
      | Destinatario.Indirizzo     | "Via Nazionale"            |
      | Destinatario.CodicePostale | "00100"                    |
      | Destinatario.Citta         | "Roma"                     |
      | Destinatario.Building      | "1"                        |
      | Destinatario.Provincia     | "RM"                       |
    When I send a POST request to "/notices/generate-massive" without stream, with body:
    """
      {
        "notices": [
          {
            "templateId": "TemplateSingleInstalment",
            "data": {
              "notice": {
                "subject": <Avviso.Oggetto>,
                "paymentAmount": <Avviso.Importo>,
                "dueDate": <Avviso.Data>,
                "code": <Avviso.Codice>
              },
              "creditorInstitution": {
                "taxCode": <Ente.CF>
              },
              "debtor": {
                "taxCode": <Destinatario.CF>,
                "fullName": <Destinatario.NomeCompleto>,
                "address": <Destinatario.Indirizzo>,
                "postalCode": <Destinatario.CodicePostale>,
                "city": <Destinatario.Citta>,
                "buildingNumber": <Destinatario.Building>,
                "province": <Destinatario.Provincia>
              }
            }
          }
        ]
      }
      """
    Then the response status should be 200
    And the response should be in JSON format
    And the response should contain the folderId
    And the request is in status "PROCESSED" after 10000 ms
    And download url is recoverable with the folderId
    And can download content using signedUrl

  Scenario: MT_02_SingleTemplate_FiveElements_COMPLETED - Generazione Massiva con successo su singolo template, cinque elementi
    Given the creditor institution in the storage:
      | variableName       | value                                  |
      | taxCode            | "80034390585"                          |
      | fullName           | "Comune di Test"                       |
      | organization       | "Settore di Test"                      |
      | info               | "Info di Test"                         |
      | webChannel         | true                                   |
      | appChannel         | false                                  |
      | physicalChannel    | "Canale Fisico"                        |
      | cbill              | "CBI1234"                              |
      | posteAccountNumber | "000000123456"                         |
      | posteAuth          | "AUT. 08/5 S3/81 53079 08129.07.20211" |
      | logo               | "./resources/logo1.png"                |
    When I send a GET request to "/notices/templates"
    Then the response status should be 200
    And the response should contain "TemplateSingleInstalment"
    Given I have the following variables:
      | variableName               | value                      |
      | Avviso.Oggetto             | "Avviso Pagamento di TEST" |
      | Avviso.Importo             | 450000                     |
      | Avviso.Data                | "31/12/2024"               |
      | Avviso.Codice              | "470000008800999050"       |
      | Avviso2.Codice             | "470000008800999051"       |
      | Avviso3.Codice             | "470000008800999062"       |
      | Avviso4.Codice             | "470000008800999073"       |
      | Avviso5.Codice             | "470000008800999074"       |
      | Ente.CF                    | "80034390585"              |
      | Destinatario.CF            | "FFFCST83A15L113V"         |
      | Destinatario.NomeCompleto  | "Mario Rossi"              |
      | Destinatario.Indirizzo     | "Via Nazionale"            |
      | Destinatario.CodicePostale | "00100"                    |
      | Destinatario.Citta         | "Roma"                     |
      | Destinatario.Building      | "1"                        |
      | Destinatario.Provincia     | "RM"                       |
    When I send a POST request to "/notices/generate-massive" without stream, with body:
    """
      {
        "notices": [
          {
            "templateId": "TemplateSingleInstalment",
            "data": {
              "notice": {
                "subject": <Avviso.Oggetto>,
                "paymentAmount": <Avviso.Importo>,
                "dueDate": <Avviso.Data>,
                "code": <Avviso.Codice>
              },
              "creditorInstitution": {
                "taxCode": <Ente.CF>
              },
              "debtor": {
                "taxCode": <Destinatario.CF>,
                "fullName": <Destinatario.NomeCompleto>,
                "address": <Destinatario.Indirizzo>,
                "postalCode": <Destinatario.CodicePostale>,
                "city": <Destinatario.Citta>,
                "buildingNumber": <Destinatario.Building>,
                "province": <Destinatario.Provincia>
              }
            }
          },
          {
            "templateId": "TemplateSingleInstalment",
            "data": {
              "notice": {
                "subject": <Avviso.Oggetto>,
                "paymentAmount": <Avviso.Importo>,
                "dueDate": <Avviso.Data>,
                "code": <Avviso2.Codice>
              },
              "creditorInstitution": {
                "taxCode": <Ente.CF>
              },
              "debtor": {
                "taxCode": <Destinatario.CF>,
                "fullName": <Destinatario.NomeCompleto>,
                "address": <Destinatario.Indirizzo>,
                "postalCode": <Destinatario.CodicePostale>,
                "city": <Destinatario.Citta>,
                "buildingNumber": <Destinatario.Building>,
                "province": <Destinatario.Provincia>
              }
            }
          },
          {
            "templateId": "TemplateSingleInstalment",
            "data": {
              "notice": {
                "subject": <Avviso.Oggetto>,
                "paymentAmount": <Avviso.Importo>,
                "dueDate": <Avviso.Data>,
                "code": <Avviso3.Codice>
              },
              "creditorInstitution": {
                "taxCode": <Ente.CF>
              },
              "debtor": {
                "taxCode": <Destinatario.CF>,
                "fullName": <Destinatario.NomeCompleto>,
                "address": <Destinatario.Indirizzo>,
                "postalCode": <Destinatario.CodicePostale>,
                "city": <Destinatario.Citta>,
                "buildingNumber": <Destinatario.Building>,
                "province": <Destinatario.Provincia>
              }
            }
          },
          {
            "templateId": "TemplateSingleInstalment",
            "data": {
              "notice": {
                "subject": <Avviso.Oggetto>,
                "paymentAmount": <Avviso.Importo>,
                "dueDate": <Avviso.Data>,
                "code": <Avviso4.Codice>
              },
              "creditorInstitution": {
                "taxCode": <Ente.CF>
              },
              "debtor": {
                "taxCode": <Destinatario.CF>,
                "fullName": <Destinatario.NomeCompleto>,
                "address": <Destinatario.Indirizzo>,
                "postalCode": <Destinatario.CodicePostale>,
                "city": <Destinatario.Citta>,
                "buildingNumber": <Destinatario.Building>,
                "province": <Destinatario.Provincia>
              }
            }
          },
          {
            "templateId": "TemplateSingleInstalment",
            "data": {
              "notice": {
                "subject": <Avviso.Oggetto>,
                "paymentAmount": <Avviso.Importo>,
                "dueDate": <Avviso.Data>,
                "code": <Avviso5.Codice>
              },
              "creditorInstitution": {
                "taxCode": <Ente.CF>
              },
              "debtor": {
                "taxCode": <Destinatario.CF>,
                "fullName": <Destinatario.NomeCompleto>,
                "address": <Destinatario.Indirizzo>,
                "postalCode": <Destinatario.CodicePostale>,
                "city": <Destinatario.Citta>,
                "buildingNumber": <Destinatario.Building>,
                "province": <Destinatario.Provincia>
              }
            }
          }
        ]
      }
      """
    Then the response status should be 200
    And the response should be in JSON format
    And the response should contain the folderId
    And the request is in status "PROCESSED" after 12000 ms
    And download url is recoverable with the folderId
    And can download content using signedUrl

  Scenario: MT_03_MultipleTemplates_COMPLETED - Generazione Massiva con successo su multipli template
    Given the creditor institution in the storage:
      | variableName       | value                                  |
      | taxCode            | "80034390585"                          |
      | fullName           | "Comune di Test"                       |
      | organization       | "Settore di Test"                      |
      | info               | "Info di Test"                         |
      | webChannel         | true                                   |
      | appChannel         | false                                  |
      | physicalChannel    | "Canale Fisico"                        |
      | cbill              | "CBI1234"                              |
      | posteAccountNumber | "000000123456"                         |
      | posteAuth          | "AUT. 08/5 S3/81 53079 08129.07.20211" |
      | logo               | "./resources/logo1.png"                |
    When I send a GET request to "/notices/templates"
    Then the response status should be 200
    And the response should contain "TemplateSingleInstalment"
    Given I have the following variables:
      | variableName               | value                      |
      | Avviso.Oggetto             | "Avviso Pagamento di TEST" |
      | Avviso.Importo             | 450000                     |
      | Avviso.Data                | "31/12/2024"               |
      | Avviso.Codice              | "470000008800999050"       |
      | Avviso.Rata1.Codice        | "470000008800999051"       |
      | Avviso.Rata1.Importo       | 150000                     |
      | Avviso.Rata1.Data          | "31/12/2024"               |
      | Avviso.Rata2.Codice        | "470000008800999062"       |
      | Avviso.Rata2.Importo       | 150000                     |
      | Avviso.Rata2.Data          | "31/12/2025"               |
      | Avviso.Rata3.Codice        | "470000008800999073"       |
      | Avviso.Rata3.Importo       | 150000                     |
      | Avviso.Rata3.Data          | "31/12/2026"               |
      | Ente.CF                    | "80034390585"              |
      | Destinatario.CF            | "FFFCST83A15L113V"         |
      | Destinatario.NomeCompleto  | "Mario Rossi"              |
      | Destinatario.Indirizzo     | "Via Nazionale"            |
      | Destinatario.CodicePostale | "00100"                    |
      | Destinatario.Citta         | "Roma"                     |
      | Destinatario.Building      | "1"                        |
      | Destinatario.Provincia     | "RM"                       |
    When I send a POST request to "/notices/generate-massive" without stream, with body:
    """
      {
        "notices": [
          {
            "templateId": "TemplateSingleInstalment",
            "data": {
              "notice": {
                "subject": <Avviso.Oggetto>,
                "paymentAmount": <Avviso.Importo>,
                "dueDate": <Avviso.Data>,
                "code": <Avviso.Codice>
              },
              "creditorInstitution": {
                "taxCode": <Ente.CF>
              },
              "debtor": {
                "taxCode": <Destinatario.CF>,
                "fullName": <Destinatario.NomeCompleto>,
                "address": <Destinatario.Indirizzo>,
                "postalCode": <Destinatario.CodicePostale>,
                "city": <Destinatario.Citta>,
                "buildingNumber": <Destinatario.Building>,
                "province": <Destinatario.Provincia>
              }
            }
          },
          {
            "templateId": "TemplateInstalmentPoste",
            "data": {
              "notice": {
                "subject": <Avviso.Oggetto>,
                "paymentAmount": <Avviso.Importo>,
                "dueDate": <Avviso.Data>,
                "code": <Avviso.Codice>
              },
              "creditorInstitution": {
                "taxCode": <Ente.CF>
              },
              "debtor": {
                "taxCode": <Destinatario.CF>,
                "fullName": <Destinatario.NomeCompleto>,
                "address": <Destinatario.Indirizzo>,
                "postalCode": <Destinatario.CodicePostale>,
                "city": <Destinatario.Citta>,
                "buildingNumber": <Destinatario.Building>,
                "province": <Destinatario.Provincia>
              }
            }
          },
          {
            "templateId": "TemplateInstalments",
            "data": {
              "notice": {
                "subject": <Avviso.Oggetto>,
                "paymentAmount": <Avviso.Importo>,
                "dueDate": <Avviso.Data>,
                "code": <Avviso.Codice>,
                "installments": [
                  {
                    "code": <Avviso.Rata1.Codice>,
                    "amount": <Avviso.Rata1.Importo>,
                    "dueDate": <Avviso.Rata1.Data>
                  },
                  {
                    "code": <Avviso.Rata2.Codice>,
                    "amount": <Avviso.Rata2.Importo>,
                    "dueDate": <Avviso.Rata2.Data>
                  }
                ]
              },
              "creditorInstitution": {
                "taxCode": <Ente.CF>
              },
              "debtor": {
                "taxCode": <Destinatario.CF>,
                "fullName": <Destinatario.NomeCompleto>,
                "address": <Destinatario.Indirizzo>,
                "postalCode": <Destinatario.CodicePostale>,
                "city": <Destinatario.Citta>,
                "buildingNumber": <Destinatario.Building>,
                "province": <Destinatario.Provincia>
              }
            }
          },
          {
            "templateId": "TemplateInstalmentsPoste",
            "data": {
              "notice": {
                "subject": <Avviso.Oggetto>,
                "paymentAmount": <Avviso.Importo>,
                "dueDate": <Avviso.Data>,
                "code": <Avviso.Codice>,
                "installments": [
                  {
                    "code": <Avviso.Rata1.Codice>,
                    "amount": <Avviso.Rata1.Importo>,
                    "dueDate": <Avviso.Rata1.Data>
                  },
                  {
                    "code": <Avviso.Rata2.Codice>,
                    "amount": <Avviso.Rata2.Importo>,
                    "dueDate": <Avviso.Rata2.Data>
                  }
                ]
              },
              "creditorInstitution": {
                "taxCode": <Ente.CF>
              },
              "debtor": {
                "taxCode": <Destinatario.CF>,
                "fullName": <Destinatario.NomeCompleto>,
                "address": <Destinatario.Indirizzo>,
                "postalCode": <Destinatario.CodicePostale>,
                "city": <Destinatario.Citta>,
                "buildingNumber": <Destinatario.Building>,
                "province": <Destinatario.Provincia>
              }
            }
          }
        ]
      }
      """
    Then the response status should be 200
    And the response should be in JSON format
    And the response should contain the folderId
    And the request is in status "PROCESSED" after 12000 ms
    And download url is recoverable with the folderId
    And can download content using signedUrl

  Scenario: MT_04_MultipleTemplates_COMPLETED_WITH_FAILURES - Generazione Massiva con successo parziale su multipli template
    Given the creditor institution in the storage:
      | variableName       | value                                  |
      | taxCode            | "80034390585"                          |
      | fullName           | "Comune di Test"                       |
      | organization       | "Settore di Test"                      |
      | info               | "Info di Test"                         |
      | webChannel         | true                                   |
      | appChannel         | false                                  |
      | physicalChannel    | "Canale Fisico"                        |
      | cbill              | "CBI1234"                              |
      | posteAccountNumber | "000000123456"                         |
      | posteAuth          | "AUT. 08/5 S3/81 53079 08129.07.20211" |
      | logo               | "./resources/logo1.png"                |
    When I send a GET request to "/notices/templates"
    Then the response status should be 200
    And the response should contain "TemplateSingleInstalment"
    Given I have the following variables:
      | variableName               | value                      |
      | Avviso.Oggetto             | "Avviso Pagamento di TEST" |
      | Avviso.Importo             | 450000                     |
      | Avviso.Data                | "31/12/2024"               |
      | Avviso.Codice              | "470000008800999050"       |
      | Avviso.Rata1.Codice        | "470000008800999051"       |
      | Avviso.Rata1.Importo       | 150000                     |
      | Avviso.Rata1.Data          | "31/12/2024"               |
      | Avviso.Rata2.Codice        | "470000008800999062"       |
      | Avviso.Rata2.Importo       | 150000                     |
      | Avviso.Rata2.Data          | "31/12/2025"               |
      | Avviso.Rata3.Codice        | "470000008800999073"       |
      | Avviso.Rata3.Importo       | 150000                     |
      | Avviso.Rata3.Data          | "31/12/2026"               |
      | Ente.CF                    | "80034390585"              |
      | Destinatario.CF            | "FFFCST83A15L113V"         |
      | Destinatario.NomeCompleto  | "Mario Rossi"              |
      | Destinatario.Indirizzo     | "Via Nazionale"            |
      | Destinatario.CodicePostale | "00100"                    |
      | Destinatario.Citta         | "Roma"                     |
      | Destinatario.Building      | "1"                        |
      | Destinatario.Provincia     | "RM"                       |
    When I send a POST request to "/notices/generate-massive" without stream, with body:
    """
      {
        "notices": [
          {
            "templateId": "TemplateSingleInstalment",
            "data": {
              "notice": {
                "subject": <Avviso.Oggetto>,
                "paymentAmount": <Avviso.Importo>,
                "code": <Avviso.Codice>,
                "dueDate": <Avviso.Data>,
              },
              "creditorInstitution": {
                "taxCode": <Ente.CF>
              },
              "debtor": {
                "taxCode": <Destinatario.CF>,
                "fullName": <Destinatario.NomeCompleto>,
                "address": <Destinatario.Indirizzo>,
                "postalCode": <Destinatario.CodicePostale>,
                "city": <Destinatario.Citta>,
                "buildingNumber": <Destinatario.Building>,
                "province": <Destinatario.Provincia>
              }
            }
          },
          {
            "templateId": "TemplateInstalmentPoste",
            "data": {
              "notice": {
                "subject": <Avviso.Oggetto>,
                "paymentAmount": <Avviso.Importo>,
                "dueDate": <Avviso.Data>,
                "code": <Avviso.Codice>
              },
              "creditorInstitution": {
                "taxCode": <Ente.CF>
              },
              "debtor": {
                "taxCode": <Destinatario.CF>,
                "fullName": <Destinatario.NomeCompleto>,
                "address": <Destinatario.Indirizzo>,
                "postalCode": <Destinatario.CodicePostale>,
                "city": <Destinatario.Citta>,
                "buildingNumber": <Destinatario.Building>,
                "province": <Destinatario.Provincia>
              }
            }
          },
          {
            "templateId": "TemplateInstalments",
            "data": {
              "notice": {
                "subject": <Avviso.Oggetto>,
                "paymentAmount": <Avviso.Importo>,
                "dueDate": <Avviso.Data>,
                "code": <Avviso.Codice>,
                "installments": [
                  {
                    "code": <Avviso.Rata1.Codice>,
                    "amount": <Avviso.Rata1.Importo>
                  },
                  {
                    "code": <Avviso.Rata2.Codice>,
                    "amount": <Avviso.Rata2.Importo>
                  }
                ]
              },
              "creditorInstitution": {
                "taxCode": <Ente.CF>
              },
              "debtor": {
                "taxCode": <Destinatario.CF>,
                "fullName": <Destinatario.NomeCompleto>,
                "address": <Destinatario.Indirizzo>,
                "postalCode": <Destinatario.CodicePostale>,
                "city": <Destinatario.Citta>,
                "buildingNumber": <Destinatario.Building>,
                "province": <Destinatario.Provincia>
              }
            }
          },
          {
            "templateId": "TemplateInstalmentsPoste",
            "data": {
              "notice": {
                "subject": <Avviso.Oggetto>,
                "paymentAmount": <Avviso.Importo>,
                "dueDate": <Avviso.Data>,
                "code": <Avviso.Codice>,
                "installments": [
                  {
                    "code": <Avviso.Rata1.Codice>,
                    "amount": <Avviso.Rata1.Importo>,
                    "dueDate": <Avviso.Rata1.Data>
                  },
                  {
                    "code": <Avviso.Rata2.Codice>,
                    "amount": <Avviso.Rata2.Importo>,
                    "dueDate": <Avviso.Rata2.Data>
                  }
                ]
              },
              "creditorInstitution": {
                "taxCode": <Ente.CF>
              },
              "debtor": {
                "taxCode": <Destinatario.CF>,
                "fullName": <Destinatario.NomeCompleto>,
                "address": <Destinatario.Indirizzo>,
                "postalCode": <Destinatario.CodicePostale>,
                "city": <Destinatario.Citta>,
                "buildingNumber": <Destinatario.Building>,
                "province": <Destinatario.Provincia>
              }
            }
          }
        ]
      }
      """
    Then the response status should be 200
    And the response should be in JSON format
    And the response should contain the folderId
    And the request is in status "PROCESSED_WITH_FAILURES" after 15000 ms
    And download url is recoverable with the folderId
    And can download content using signedUrl
