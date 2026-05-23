terraform {
  required_version = ">= 1.0.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 6.0"
    }
  }
}

module "vpc" {
  source                    = "../../modules/vpc"
  name                      = "dev"
  vpc_cidr_block            = "10.0.0.0/16"
  public_subnet_cidr_block  = ["10.0.1.0/26", "10.0.2.0/26"]
  private_subnet_cidr_block = ["10.0.3.0/26", "10.0.4.0/26"]
  azs                       = ["ap-south-1a", "ap-south-1b"]
}

module "security_group" {
  source = "../../modules/security-group"
  name   = "dev"
  vpc_id = module.vpc.vpc_id

  ingress_rules = [
    {
      from_port   = 22
      to_port     = 22
      ip_protocol = "tcp"
      cidr_ipv4   = "0.0.0.0/0"
    },
    /*{
      from_port   = 80
      to_port     = 80
      ip_protocol = "tcp"
      cidr_ipv4   = "0.0.0.0/0"
    }*/
  ]
  egress_rules = [
    {
      ip_protocol = "-1"
      cidr_ipv4   = "0.0.0.0/0"
    }
  ]
}

module "security_group_alb" {
  source = "../../modules/security-group"
  name   = "dev_alb"
  vpc_id = module.vpc.vpc_id

  ingress_rules = [
    {
      from_port   = 80
      to_port     = 80
      ip_protocol = "tcp"
      cidr_ipv4   = "0.0.0.0/0"
    }
  ]
  egress_rules = [
    {
      ip_protocol = "-1"
      cidr_ipv4   = "0.0.0.0/0"
    }
  ]
}

module "security_group_web" {
  source = "../../modules/security-group"
  name   = "dev_web"
  vpc_id = module.vpc.vpc_id

  ingress_rules = [
    {
      from_port   = 22
      to_port     = 22
      ip_protocol = "tcp"
      cidr_ipv4   = "0.0.0.0/0"
    },
    {
      from_port                    = 80
      to_port                      = 80
      ip_protocol                  = "tcp"
      referenced_security_group_id = module.security_group_alb.security_group_id
    }
  ]
  egress_rules = [
    {
      ip_protocol = "-1"
      cidr_ipv4   = "0.0.0.0/0"
    }
  ]
}

module "security_group_rds" {
  source = "../../modules/security-group"
  name   = "dev_rds_database"
  vpc_id = module.vpc.vpc_id

  ingress_rules = [
    {
      from_port                    = 3306
      to_port                      = 3306
      ip_protocol                  = "tcp"
      referenced_security_group_id = module.security_group_web.security_group_id
    }
  ]
  /*egress_rules = [
    {
      ip_protocol = "-1"
      cidr_ipv4   = "0.0.0.0/0"
    }
  ]*/
}

module "alb" {
  source            = "../../modules/alb"
  name              = "dev"
  vpc_id            = module.vpc.vpc_id
  public_subnet_ids = module.vpc.public_subnet_ids
  alb_sg_id         = module.security_group_alb.security_group_id
}

module "asg" {
  source             = "../../modules/asg"
  name               = "dev"
  private_subnet_ids = module.vpc.private_subnet_ids
  target_group_arn   = module.alb.target_group_arn
  security_group_id  = module.security_group_web.security_group_id
  instance_type      = "t3.micro"
  desired_capacity   = 1

  depends_on = [module.vpc, module.alb]
}

module "rds" {
  source             = "../../modules/rds"
  private_subnet_ids = module.vpc.private_subnet_ids
  security_group_id  = module.security_group_rds.security_group_id

  depends_on = [module.vpc, module.security_group_rds]
}
/*
module "ec2" {
  source            = "../../modules/ec2"
  name              = "dev"
  instance_type     = "t3.micro"
  subnet_id         = module.vpc.private_subnet_ids[0]
  security_group_id = [module.security_group_web.security_group_id]

  depends_on = [module.vpc]
}*/
