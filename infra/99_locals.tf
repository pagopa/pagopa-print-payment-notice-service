locals {
  product = "${var.prefix}-${var.env_short}"

  apim = {
    name       = "${local.product}-apim"
    rg         = "${local.product}-api-rg"
    ext_product_id = "pagopa_notices_service_external"
    int_product_id = "pagopa_notices_service_internal"
  }
}

