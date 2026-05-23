resource "aws_security_group" "this" {
  name        = "my-${var.name}-sg"
  description = "My security group"
  vpc_id      = var.vpc_id

  tags = {
    Name = "my-${var.name}-sg"
  }
}

resource "aws_vpc_security_group_ingress_rule" "this" {
  for_each = { for idx, rule in var.ingress_rules : idx => rule }

  security_group_id            = aws_security_group.this.id
  from_port                    = each.value.from_port
  to_port                      = each.value.to_port
  ip_protocol                  = each.value.ip_protocol
  cidr_ipv4                    = each.value.cidr_ipv4 != null ? each.value.cidr_ipv4 : null
  referenced_security_group_id = each.value.referenced_security_group_id != null ? each.value.referenced_security_group_id : null
}

resource "aws_vpc_security_group_egress_rule" "this" {
  for_each = { for idx, rule in var.egress_rules : idx => rule }

  security_group_id = aws_security_group.this.id
  ip_protocol       = each.value.ip_protocol
  cidr_ipv4         = each.value.cidr_ipv4
}
