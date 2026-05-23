variable "ami_id" {
  default = "ami-05d2d839d4f73aafb"
}

variable "instance_type" {}

variable "subnet_id" {}

variable "security_group_id" {
  type = list(string)
}

variable "name" {
  description = "ec2 instance name"
}
