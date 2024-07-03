Feature: Single Generation - 2 Installments

  Scenario: FT_22_RateMultiple2_AllFields: Rate multiple 4 - Tutti campi valorizzati
    Given the creditor institution in the storage:
      | variableName       | value                                  |
      | taxCode            | "99999000013"                          |
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
    And the response list should contain a template "TemplateInstalments"
    Given I have the following variables:
      | variableName               | value                      |
      | template_id                | "TemplateInstalments"      |
      | Avviso.Oggetto             | "Avviso Pagamento di TEST" |
      | Avviso.Importo             | 600000                     |
      | Avviso.Data                | "30/12/2024"               |
      | Avviso.Codice              | "470000008800999051"       |
      | Avviso.Rata1.Codice        | "470000008800999062"       |
      | Avviso.Rata1.Importo       | 160000                     |
      | Avviso.Rata1.Data          | "31/12/2024"               |
      | Avviso.Rata2.Codice        | "470000008800999073"       |
      | Avviso.Rata2.Importo       | 140000                     |
      | Avviso.Rata2.Data          | "31/12/2025"               |
      | Avviso.Rata3.Codice        | "470000008800999063"       |
      | Avviso.Rata3.Importo       | 160000                     |
      | Avviso.Rata3.Data          | "31/12/2024"               |
      | Avviso.Rata4.Codice        | "470000008800999074"       |
      | Avviso.Rata4.Importo       | 140000                     |
      | Avviso.Rata4.Data          | "31/12/2025"               |
      | Ente.CF                    | "99999000013"              |
      | Destinatario.CF            | "FFFCST83A15L113V"         |
      | Destinatario.NomeCompleto  | "Mario Rossi"              |
      | Destinatario.Indirizzo     | "Via Nazionale"            |
      | Destinatario.CodicePostale | "00100"                    |
      | Destinatario.Citta         | "Roma"                     |
      | Destinatario.Building      | "1"                        |
      | Destinatario.Provincia     | "RM"                       |
    When I send a POST request to "/notices/generate" with body:
    """
      {
        "templateId": <template_id>,
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
              },
              {
                "code": <Avviso.Rata3.Codice>,
                "amount": <Avviso.Rata3.Importo>,
                "dueDate": <Avviso.Rata3.Data>
              },
              {
                "code": <Avviso.Rata4.Codice>,
                "amount": <Avviso.Rata4.Importo>,
                "dueDate": <Avviso.Rata4.Data>
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
      """
    Then the response status should be 201
    And the response should be in PDF format
    And the PDF document should be equal to the reference PDF "scenario_ft_09.pdf"


  Scenario: FT_23_RateMultiple2_SomeFields: Rate multiple 5 - Solo alcuni campi valorizzati
    Given the creditor institution in the storage:
      | variableName       | value                                  |
      | taxCode            | "99999000013"                          |
      | fullName           | "Comune di Test"                       |
      | organization       | null                                   |
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
    And the response list should contain a template "TemplateInstalments"
    Given I have the following variables:
      | variableName               | value                      |
      | template_id                | "TemplateInstalments"      |
      | Avviso.Oggetto             | "Avviso Pagamento di TEST" |
      | Avviso.Importo             | 300000                     |
      | Avviso.Data                | "31/12/2024"               |
      | Avviso.Codice              | "470000008800999051"       |
      | Avviso.Rata1.Codice        | "470000008800999062"       |
      | Avviso.Rata1.Importo       | 160000                     |
      | Avviso.Rata1.Data          | "31/12/2024"               |
      | Avviso.Rata2.Codice        | "470000008800999073"       |
      | Avviso.Rata2.Importo       | 140000                     |
      | Avviso.Rata2.Data          | "31/12/2025"               |
      | Avviso.Rata3.Codice        | "470000008800999063"       |
      | Avviso.Rata3.Importo       | 160000                     |
      | Avviso.Rata3.Data          | "31/12/2024"               |
      | Avviso.Rata4.Codice        | "470000008800999074"       |
      | Avviso.Rata4.Importo       | 140000                     |
      | Avviso.Rata4.Data          | "31/12/2025"               |
      | Avviso.Rata5.Codice        | "570000008800999074"       |
      | Avviso.Rata5.Importo       | 50000                      |
      | Avviso.Rata5.Data          | "5/12/2025"                |
      | Ente.CF                    | "99999000013"              |
      | Destinatario.CF            | "FFFCST83A15L113V"         |
      | Destinatario.NomeCompleto  | "Mario Rossi"              |
      | Destinatario.Indirizzo     | "Via Nazionale"            |
      | Destinatario.CodicePostale | "00100"                    |
      | Destinatario.Citta         | "Roma"                     |
      | Destinatario.Building      | "1"                        |
      | Destinatario.Provincia     | "RM"                       |
    When I send a POST request to "/notices/generate" with body:
    """
      {
        "templateId": <template_id>,
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
              },
              {
                "code": <Avviso.Rata3.Codice>,
                "amount": <Avviso.Rata3.Importo>,
                "dueDate": <Avviso.Rata3.Data>
              },
              {
                "code": <Avviso.Rata4.Codice>,
                "amount": <Avviso.Rata4.Importo>,
                "dueDate": <Avviso.Rata4.Data>
              }, {
                "code": <Avviso.Rata5.Codice>,
                "amount": <Avviso.Rata5.Importo>,
                "dueDate": <Avviso.Rata5.Data>
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
      """
    Then the response status should be 201
    And the response should be in PDF format
    And the PDF document should be equal to the reference PDF "scenario_ft_10.pdf"
