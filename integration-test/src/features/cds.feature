Feature: Single Generation CDS


  Scenario Outline: <scenario_id>
    Given the creditor institution in the storage:
      | variableName       | value                                  |
      | taxCode            | "99999000013"                          |
      | fullName           | "Comune di Test"                       |
      | organization       | "Settore di Test"                      |
      | info               | "<ec.info>"                            |
      | webChannel         | true                                   |
      | appChannel         | false                                  |
      | physicalChannel    | "Canale Fisico"                        |
      | cbill              | "CBI1234"                              |
      | posteAccountNumber | "000000123456"                         |
      | posteAuth          | "AUT. 08/5 S3/81 53079 08129.07.20211" |
      | logo               | "./resources/logo1.png"                |
    When I send a GET request to "/notices/templates"
    Then the response status should be 200
    And the response list should contain a template "<template_id>"
    Given I have the following variables:
      | variableName               | value                      |
      | template_id                | "TemplateCdsInfraction"    |
      | Avviso.Oggetto             | "Avviso Pagamento di TEST" |
      | Avviso.ImportoRidotto      | 150000                     |
      | Avviso.CodiceRidotto       | "470000008800999051"       |
      | Avviso.ImportoScontato     | 170000                     |
      | Avviso.CodiceScontato      | "470000008800999052"       |
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
            "reducedAmount": <Avviso.ImportoRidotto>,
            "discountedAmount": <Avviso.ImportoScontato>,
            "installments": [
              {
                  "code":  <Avviso.CodiceRidotto>,
                  "amount": <Avviso.ImportoRidotto>
              },
              {
                  "code": <Avviso.CodiceScontato>,
                  "amount": <Avviso.ImportoScontato>
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
    And the PDF document should be equal to the reference PDF "<pdf_name>"
    Examples:
      | scenario_id                | template_id                   | ec.info      | pdf_name           |
      | FT_17_CDS_AllFields        | TemplateCdsInfraction         | Info di Test | scenario_ft_17.pdf |
      | FT_18_CDS_Poste_AllFields  | TemplateCdsInfractionExtended | Info di Test | scenario_ft_18.pdf |
      | FT_19_CDS_SomeFields       | TemplateCdsInfraction         | null         | scenario_ft_19.pdf |
      | FT_20_CDS_Poste_SomeFields | TemplateCdsInfractionExtended | null         | scenario_ft_20.pdf |

  Scenario Outline: <scenario_id> Bad Request Discounted Amount
      Given the creditor institution in the storage:
        | variableName       | value                                  |
        | taxCode            | "80034390585"                          |
        | fullName           | "Comune di Test"                       |
        | organization       | "Settore di Test"                      |
        | info               | "<ec.info>"                            |
        | webChannel         | true                                   |
        | appChannel         | false                                  |
        | physicalChannel    | "Canale Fisico"                        |
        | cbill              | "CBI1234"                              |
        | posteAccountNumber | "000000123456"                         |
        | posteAuth          | "AUT. 08/5 S3/81 53079 08129.07.20211" |
        | logo               | "./resources/logo1.png"                |
      When I send a GET request to "/notices/templates"
      Then the response status should be 200
      And the response list should contain a template "<template_id>"
      Given I have the following variables:
        | variableName               | value                      |
        | template_id                | "TemplateCdsInfraction"    |
        | Avviso.Oggetto             | "Avviso Pagamento di TEST" |
        | Avviso.ImportoRidotto      | 150000                     |
        | Avviso.CodiceRidotto       | "47000000880099905"        |
        | Avviso.ImportoScontato     | 170000                     |
        | Avviso.CodiceScontato      | "47000000880099906"        |
        | Ente.CF                    | "80034390585"              |
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
              "reducedAmount": <Avviso.ImportoRidotto>,
              "installments": [
              {
                    "code":  <Avviso.CodiceRidotto>,
                    "amount": <Avviso.ImportoRidotto>
              },
              {
                    "code": <Avviso.CodiceScontato>,
                    "amount": <Avviso.ImportoScontato>
              }
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
      Then the response status should be 400
      Examples:
        | scenario_id                | template_id                   | ec.info      | pdf_name           |
        | FT_17_CDS_AllFields        | TemplateCdsInfraction         | Info di Test | scenario_ft_17.pdf |
        | FT_18_CDS_Poste_AllFields  | TemplateCdsInfractionExtended | Info di Test | scenario_ft_18.pdf |
        | FT_19_CDS_SomeFields       | TemplateCdsInfraction         | null         | scenario_ft_19.pdf |
        | FT_20_CDS_Poste_SomeFields | TemplateCdsInfractionExtended | null         | scenario_ft_20.pdf |

  Scenario Outline: <scenario_id> Bad Request Reduced Amount
      Given the creditor institution in the storage:
        | variableName       | value                                  |
        | taxCode            | "80034390585"                          |
        | fullName           | "Comune di Test"                       |
        | organization       | "Settore di Test"                      |
        | info               | "<ec.info>"                            |
        | webChannel         | true                                   |
        | appChannel         | false                                  |
        | physicalChannel    | "Canale Fisico"                        |
        | cbill              | "CBI1234"                              |
        | posteAccountNumber | "000000123456"                         |
        | posteAuth          | "AUT. 08/5 S3/81 53079 08129.07.20211" |
        | logo               | "./resources/logo1.png"                |
      When I send a GET request to "/notices/templates"
      Then the response status should be 200
      And the response list should contain a template "<template_id>"
      Given I have the following variables:
        | variableName               | value                      |
        | template_id                | "TemplateCdsInfraction"    |
        | Avviso.Oggetto             | "Avviso Pagamento di TEST" |
        | Avviso.ImportoRidotto      | 150000                     |
        | Avviso.CodiceRidotto       | "47000000880099905"        |
        | Avviso.ImportoScontato     | 170000                     |
        | Avviso.CodiceScontato      | "47000000880099906"        |
        | Ente.CF                    | "80034390585"              |
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
              "discountedAmount": <Avviso.ImportoScontato>,
              "installments": [
              {
                    "code":  <Avviso.CodiceRidotto>,
                    "amount": <Avviso.ImportoRidotto>
              },
              {
                    "code": <Avviso.CodiceScontato>,
                    "amount": <Avviso.ImportoScontato>
              }
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
      Then the response status should be 400
      Examples:
        | scenario_id                | template_id                   | ec.info      | pdf_name           |
        | FT_17_CDS_AllFields        | TemplateCdsInfraction         | Info di Test | scenario_ft_17.pdf |
        | FT_18_CDS_Poste_AllFields  | TemplateCdsInfractionExtended | Info di Test | scenario_ft_18.pdf |
        | FT_19_CDS_SomeFields       | TemplateCdsInfraction         | null         | scenario_ft_19.pdf |
        | FT_20_CDS_Poste_SomeFields | TemplateCdsInfractionExtended | null         | scenario_ft_20.pdf |
