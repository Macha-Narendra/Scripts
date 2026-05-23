variable "vpc_id" {}
variable "name" {
  description = "security-group name"
}

variable "ingress_rules" {
  description = "List Of Inbound rules"
  type = list(object({
    from_port                    = number
    to_port                      = number
    ip_protocol                  = string
    cidr_ipv4                    = optional(string)
    referenced_security_group_id = optional(string)
  }))
  default = [
    {
      from_port   = 80
      to_port     = 80
      ip_protocol = "tcp"
    }
  ]
}

variable "egress_rules" {
  description = "List Of Outbound rules"
  type = list(object({
    ip_protocol = string
    cidr_ipv4   = string
  }))
  default = [
    {
      ip_protocol = "-1"
      cidr_ipv4   = "0.0.0.0/0"
    }
  ]
}
