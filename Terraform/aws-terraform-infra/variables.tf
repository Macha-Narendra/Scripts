variable "aws_region" {
  description = "AWS Default Region"
  type        = string
  default     = "ap-south-1"
}

variable "instance_type" {
  description = "Free tire instance type"
  type        = string
  default     = "t3.micro"
}

variable "ami_image_id" {
  description = "Amazon Linux 2 AMI ID for ap-south-1 region"
  type        = string
  default     = "ami-05d2d839d4f73aafb"
}

variable "vpc_cidr_block" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr_block" {
  description = "CIDR block for the public subnet"
  type        = string
  default     = "10.0.1.0/26"
}
