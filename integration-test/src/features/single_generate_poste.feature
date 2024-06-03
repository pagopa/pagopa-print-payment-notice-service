Feature: Single Generation Poste

  Scenario: FT_03_RataSingolaPoste_AllFields: Rata singola con bollettino postale - Tutti campi valorizzati
    Given the creditor institution in the storage:
      | variableName       | value                                  |
      | taxCode            | "80034390585"                          |
      | fullName           | "Comune di Test"                       |
      | organization       | "Settore di Test"                      |
      | info               | "Info di Test"                         |
      | webChannel         | true                                   |
      | physicalChannel    | "Canale Fisico"                        |
      | cbill              | "CBI1234"                              |
      | posteAccountNumber | "000000123456"                         |
      | posteAuth          | "AUT. 08/5 S3/81 53079 08129.07.20211" |
      | logo               | "./resources/logo1.png"                |
    When I send a GET request to "/notices/templates"
    Then the response status should be 200
    And the response should contain "TemplateSingleInstalmentPoste"
    Given I have the following variables:
      | variableName               | value                           |
      | template_id                | "TemplateSingleInstalmentPoste" |
      | Avviso.Oggetto             | "Avviso Pagamento di TEST"      |
      | Avviso.Importo             | 150000                          |
      | Avviso.Data                | "31/12/2024"                    |
      | Avviso.Codice              | "47000000880099905"             |
      | Ente.CF                    | "80034390585"                   |
      | Destinatario.CF            | "FFFCST83A15L113V"              |
      | Destinatario.NomeCompleto  | "Mario Rossi"                   |
      | Destinatario.Indirizzo     | "Via Nazionale"                 |
      | Destinatario.CodicePostale | "00100"                         |
      | Destinatario.Citta         | "Roma"                          |
      | Destinatario.Building      | "1"                             |
      | Destinatario.Provincia     | "RM"                            |
    When I send a POST request to "/notices/generate" with body:
    """
      {
        "templateId": <template_id>,
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
      """
    Then the response status should be 201
    And the response should be in PDF format
    And the PDF document should be equal to the reference PDF "scenario_ft_03.pdf"


  Scenario: FT_04_RataSingolaPoste_SomeFields: Rata singola con bollettino postale - Solo alcuni campi valorizzati
    Given the creditor institution in the storage:
      | variableName       | value                                  |
      | taxCode            | "80034390585"                          |
      | fullName           | "Comune di Test"                       |
      | organization       | null                                   |
      | info               | "Info di Test"                         |
      | webChannel         | true                                   |
      | physicalChannel    | "Canale Fisico"                        |
      | cbill              | "CBI1234"                              |
      | posteAccountNumber | "232323"                               |
      | posteAuth          | "AUT. 08/5 S3/81 53079 08129.07.20211" |
      | logo               | "./resources/logo1.png"                |
    When I send a GET request to "/notices/templates"
    Then the response status should be 200
    And the response should contain "TemplateSingleInstalmentPoste"
    Given I have the following variables:
      | variableName               | value                           |
      | template_id                | "TemplateSingleInstalmentPoste" |
      | Avviso.Oggetto             | "Avviso Pagamento di TEST"      |
      | Avviso.Importo             | 100                             |
      | Avviso.Data                | "31/12/2024"                    |
      | Avviso.Codice              | "47000000880099905"             |
      | Ente.CF                    | "80034390585"                   |
      | Destinatario.CF            | "FFFCST83A15L113V"              |
      | Destinatario.NomeCompleto  | "Mario Rossi"                   |
      | Destinatario.Indirizzo     | "Via Nazionale"                 |
      | Destinatario.CodicePostale | "00100"                         |
      | Destinatario.Citta         | "Roma"                          |
      | Destinatario.Building      | "1"                             |
      | Destinatario.Provincia     | "RM"                            |
    When I send a POST request to "/notices/generate" with body:
    """
      {
        "templateId": <template_id>,
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
      """
    Then the response status should be 201
    And the response should be in PDF format
    And the PDF document should be equal to the reference PDF "scenario_ft_04.pdf"
