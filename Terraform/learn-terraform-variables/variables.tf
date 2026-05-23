# Copyright (c) HashiCorp, Inc.
# SPDX-License-Identifier: MPL-2.0

# Variable declarations
variable "aws_region" {
  type        = string
  description = "AWS region to deploy resources in"
  default     = "ap-south-1"
}

variable "vpc_cidr_notation" {
  type        = string
  description = "CIDR notation for VPC"
  default     = "10.0.0.0/16"
}

variable "instance_count" {
  type        = number
  description = "Number of EC2 instances to deploy"
  default     = 2
}

variable "enable_vpn_gateway" {
  type        = bool
  description = "Whether to enable VPN Gateway in VPC"
  default     = false
}

variable "pulic_subnet_count" {
  type        = number
  description = "Number of public subnets to create in VPC"
  default     = 2
}

variable "private_subnet_count" {
  type        = number
  description = "Number of private subnets to create in VPC"
  default     = 2
}

variable "public_subnet_cidr_blocks" {
  type        = list(string)
  description = "CIDR blocks for public subnets"
  default = [
    "10.0.1.0/24",
    "10.0.2.0/24",
    "10.0.3.0/24",
    "10.0.4.0/24",
    "10.0.5.0/24",
    "10.0.6.0/24",
    "10.0.7.0/24",
    "10.0.8.0/24",
  ]
}

variable "private_subnet_cidr_blocks" {
  type        = list(string)
  description = "CIDR blocks for private subnets"
  default = [
    "10.0.101.0/24",
    "10.0.102.0/24",
    "10.0.103.0/24",
    "10.0.104.0/24",
    "10.0.105.0/24",
    "10.0.106.0/24",
    "10.0.107.0/24",
    "10.0.108.0/24",
  ]
}

variable "resource_tags" {
    description = "Resource Tags"
    type = map(string)
    default = {
        environment = "dev",
        project     = "project-alpha"
    }
}

variable "instance_type" {
  description = "Type of EC2 instance to use"
  type        = string
  #default     = "t3.micro"
}

