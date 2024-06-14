#Feature: Single Generation CDS
#
#  Background:
#    Given the creditor institution in the storage:
#      | variableName       | value                                  |
#      | taxCode            | "80034390585"                          |
#      | fullName           | "Comune di Test"                       |
#      | organization       | "Settore di Test"                      |
#      | info               | "Info di Test"                         |
#      | webChannel         | true                                   |
#      | appChannel         | false                                  |
#      | physicalChannel    | "Canale Fisico"                        |
#      | cbill              | "CBI1234"                              |
#      | posteAccountNumber | "000000123456"                         |
#      | posteAuth          | "AUT. 08/5 S3/81 53079 08129.07.20211" |
#      | logo               | "./resources/logo1.png"                |
#
#  Scenario: FT_17_CDS_AllFields: Violazione CDS - Tutti campi valorizzati
#    When I send a GET request to "/notices/templates"
#    Then the response status should be 200
#    And the response list should contain a template "TemplateCdsInfraction"
#    Given I have the following variables:
#      | variableName               | value                      |
#      | template_id                | "TemplateCdsInfraction"    |
#      | Avviso.Oggetto             | "Avviso Pagamento di TEST" |
#      | Avviso.ImportoRidotto      | 150000                     |
#      | Avviso.CodiceRidotto       | "47000000880099905"        |
#      | Avviso.ImportoScontato     | 170000                     |
#      | Avviso.CodiceScontato      | "47000000880099906"        |
#      | Ente.CF                    | "80034390585"              |
#      | Destinatario.CF            | "FFFCST83A15L113V"         |
#      | Destinatario.NomeCompleto  | "Mario Rossi"              |
#      | Destinatario.Indirizzo     | "Via Nazionale"            |
#      | Destinatario.CodicePostale | "00100"                    |
#      | Destinatario.Citta         | "Roma"                     |
#      | Destinatario.Building      | "1"                        |
#      | Destinatario.Provincia     | "RM"                       |
#    When I send a POST request to "/notices/generate" with body:
#    """
#      {
#        "templateId": <template_id>,
#        "data": {
#          "notice": {
#            "subject": <Avviso.Oggetto>,
#            "reducedAmount": <Avviso.ImportoRidotto>,
#            "discountedAmount": <Avviso.ImportoScontato>,
#            "installments": [
#            {
#                  "code":  <Avviso.CodiceRidotto>,
#                  "amount": <Avviso.ImportoRidotto>
#            },
#            {
#                  "code": <Avviso.CodiceScontato>,
#                  "amount": <Avviso.ImportoScontato>
#            }
#          },
#          "creditorInstitution": {
#            "taxCode": <Ente.CF>
#          },
#          "debtor": {
#            "taxCode": <Destinatario.CF>,
#            "fullName": <Destinatario.NomeCompleto>,
#            "address": <Destinatario.Indirizzo>,
#            "postalCode": <Destinatario.CodicePostale>,
#            "city": <Destinatario.Citta>,
#            "buildingNumber": <Destinatario.Building>,
#            "province": <Destinatario.Provincia>
#          }
#        }
#      }
#      """
#    Then the response status should be 201
#    And the response should be in PDF format
#    And the PDF document should be equal to the reference PDF "scenario_ft_17.pdf"
