terraform {
  backend "s3" {
    bucket         = "terraform-narendra-123-465362303741-ap-south-1-an"
    key            = "env/dev/modules/terraform.tfstate"
    region         = "ap-south-1"
    dynamodb_table = "terraform-state-narendra-locks"
    encrypt        = true
  }
}
