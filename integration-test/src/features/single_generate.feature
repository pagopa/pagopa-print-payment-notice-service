Feature: Single Generation

  Background:
    Given the creditor institution in the storage:
      | variableName       | value                                  |
      | taxCode            | "12345678911"                          |
      | fullName           | "Comune di Test"                       |
      | organization       | "Settore di Test"                      |
      | info               | "Info di Test"                         |
      | webChannel         | true                                   |
      | physicalChannel    | "Canale Fisico"                        |
      | cbill              | "CBI1234"                              |
      | posteAccountNumber | "232323"                               |
      | posteAuth          | "AUT. 08/5 S3/81 53079 08129.07.20211" |
      | logo               | "./resources/logo1.png"                |

  Scenario: All fields with a value
    When I send a GET request to "/notices/templates"
    Then the response status should be 200
    And the response list should contain a template "TemplateSingleInstalment"
    Given I have the following variables:
      | variableName               | value                      |
      | template_id                | "TemplateSingleInstalment" |
      | Avviso.Oggetto             | "Avviso Pagamento di TEST" |
      | Avviso.Importo             | 100                        |
      | Avviso.Data                | "31/12/2024"               |
      | Avviso.Codice              | "47000000880099905"        |
      | Avviso.Rata1.Codice        | "inst1"                    |
      | Avviso.Rata1.Importo       | 150000                     |
      | Avviso.Rata1.Data          | "31/12/2024"               |
      | Ente.CF                    | "12345678911"              |
      | Destinatario.CF            | "FFFCST83A15L113V"         |
      | Destinatario.NomeCompleto  | "Mario Rossi"              |
      | Destinatario.Indirizzo     | "Via Roma"                 |
      | Destinatario.CodicePostale | "00100"                    |
      | Destinatario.Citta         | "Somewhere"                |
      | Destinatario.Building      | "15"                       |
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
    And the PDF document should be equal to the reference PDF "scenario1.pdf"
