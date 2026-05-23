variable "vpc_cidr_block" {

}

variable "public_subnet_cidr_block" {
  type = list(string)
}

variable "private_subnet_cidr_block" {
  type = list(string)

}

variable "name" {
  description = "vpc name"
}

variable "azs" {
  type = list(string)
}
