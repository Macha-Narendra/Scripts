terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 6.0.0"
    }
  }
  required_version = ">= 1.0.0"
}

resource "aws_vpc" "main_vpc" {
  cidr_block           = var.vpc_cidr_block
  instance_tenancy     = "default"
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = {
    Name = "main-vpc"
  }
}

resource "aws_subnet" "public_subnet" {
  vpc_id                  = aws_vpc.main_vpc.id
  cidr_block              = var.public_subnet_cidr_block
  map_public_ip_on_launch = true
  tags = {
    Name = "public-subnet-1"
  }
}

resource "aws_internet_gateway" "main_igw" {
  vpc_id = aws_vpc.main_vpc.id
  tags = {
    Name = "main-igw"
  }
}

resource "aws_route_table" "main_rt" {
  vpc_id = aws_vpc.main_vpc.id
  tags = {
    Name = "main-rt"
  }
}

resource "aws_route" "default_route" {
  route_table_id         = aws_route_table.main_rt.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.main_igw.id
}

resource "aws_route_table_association" "public_subnet_association" {
  subnet_id      = aws_subnet.public_subnet.id
  route_table_id = aws_route_table.main_rt.id
}

data "aws_iam_instance_profile" "ssm_instance_profile" {
  name = "AWS_EC2_SSM_Connect"
}

resource "aws_security_group" "ec2_test_sg" {
  name        = "ec2-test-sg"
  description = "Allow SSH and HTTP traffic"
  vpc_id      = aws_vpc.main_vpc.id

  tags = {
    Name = "ec2-test-sg-1"
  }
}

resource "aws_vpc_security_group_ingress_rule" "allow_ssh" {
  security_group_id = aws_security_group.ec2_test_sg.id
  cidr_ipv4       = "0.0.0.0/0"
  from_port         = 22
  to_port           = 22
  ip_protocol       = "tcp"
}

resource "aws_vpc_security_group_ingress_rule" "allow_http" {
  security_group_id = aws_security_group.ec2_test_sg.id
  cidr_ipv4       = "0.0.0.0/0"
  from_port         = 80
  to_port           = 80
  ip_protocol       = "tcp"
}

resource "aws_vpc_security_group_egress_rule" "allow_all_outbound" {
  security_group_id = aws_security_group.ec2_test_sg.id
  cidr_ipv4       = "0.0.0.0/0"
  ip_protocol       = "-1"
}

resource "aws_instance" "my_first_ec2" {
  ami                    = var.ami_image_id
  instance_type          = var.instance_type
  subnet_id              = aws_subnet.public_subnet.id
  vpc_security_group_ids = [aws_security_group.ec2_test_sg.id]
  key_name               = "my-first-ec2-key-pair"
  iam_instance_profile   = "AWS_EC2_SSM_Connect"
  user_data              = <<-EOF
                #!/bin/bash
                apt update -y
                apt install nginx -y
                systemctl enable snap.amazon-ssm-agent.amazon-ssm-agent.service
                systemctl start snap.amazon-ssm-agent.amazon-ssm-agent.service
                systemctl start nginx
                systemctl enable nginx
                echo "Hello, World!" > /var/www/html/index.html
                EOF
  tags = {
    Name = "my-first-ec2"
  }
}
