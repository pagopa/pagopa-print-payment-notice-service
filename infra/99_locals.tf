locals {
  product = "${var.prefix}-${var.env_short}"

  apim = {
    name       = "${local.product}-apim"
    rg         = "${local.product}-api-rg"
    product_id = "pagoa_notices_service"
  }
}

