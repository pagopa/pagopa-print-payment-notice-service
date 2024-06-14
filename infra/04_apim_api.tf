locals {
  repo_name = "pagopa-print-payment-notice-service"

  apim_external = {
    display_name = "External Payment Notices Print Service APIs"
    description  = "External Payment Notices Print Service APIs"
    path         = "print-payment-notice-service/external"
  }

  apim_internal = {
    display_name = "Internal Payment Notices Print Service APIs"
    description  = "Internal Payment Notices Print Service APIs"
    path         = "print-payment-notice-service/internal"
  }

  host     = "api.${var.apim_dns_zone_prefix}.${var.external_domain}"
  hostname = var.hostname
}

resource "azurerm_api_management_group" "external_api_group" {
  name                = local.apim.ext_product_id
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = local.apim_external.display_name
  description         = local.apim_external.description
}

resource "azurerm_api_management_api_version_set" "external_api_version_set" {
  name                = format("%s-${local.repo_name}-external", var.env_short)
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = local.apim_external.display_name
  versioning_scheme   = "Segment"
}

module "api_external_v1" {
  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v6.7.0"

  name                  = format("%s-${local.repo_name}-external", var.env_short)
  api_management_name   = local.apim.name
  resource_group_name   = local.apim.rg
  product_ids           = [local.apim.ext_product_id]
  subscription_required = true

  version_set_id = azurerm_api_management_api_version_set.external_api_version_set.id
  api_version    = "v1"

  description  = local.apim_external.description
  display_name = local.apim_external.display_name
  path         = local.apim_external.path
  protocols    = ["https"]

  service_url = null

  content_format = "openapi"
  content_value  = templatefile("../openapi/openapi_external.json", {
    host = local.host
  })

  xml_content = templatefile("./policy/_base_policy.xml", {
    hostname = var.hostname
  })
}

resource "azurerm_api_management_group" "internal_api_group" {
  name                = local.apim.int_product_id
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = local.apim_internal.display_name
  description         = local.apim_internal.description
}

resource "azurerm_api_management_api_version_set" "internal_api_version_set" {
  name                = format("%s-${local.repo_name}-internal", var.env_short)
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = local.apim_internal.display_name
  versioning_scheme   = "Segment"
}

module "api_internal_v1" {
  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v6.7.0"

  name                  = format("%s-${local.repo_name}-internal", var.env_short)
  api_management_name   = local.apim.name
  resource_group_name   = local.apim.rg
  product_ids           = [local.apim.int_product_id]
  subscription_required = true

  version_set_id = azurerm_api_management_api_version_set.internal_api_version_set.id
  api_version    = "v1"

  description  = local.apim_internal.description
  display_name = local.apim_internal.display_name
  path         = local.apim_internal.path
  protocols    = ["https"]

  service_url = null

  content_format = "openapi"
  content_value  = templatefile("../openapi/openapi.json", {
    host = local.host
  })

  xml_content = templatefile("./policy/_base_policy.xml", {
    hostname = var.hostname
  })
}

