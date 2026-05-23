resource "aws_db_subnet_group" "this" {
  name       = "demo-db-subnet-group"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name = "demo-db-subnet-group"
  }
}

resource "aws_db_instance" "this" {
  allocated_storage      = 10
  db_name                = "mydb"
  engine                 = "mysql"
  engine_version         = "8.0"
  instance_class         = "db.t3.micro"
  username               = "foo"
  password               = "LGsoft5141"
  parameter_group_name   = "default.mysql8.0"
  skip_final_snapshot    = true
  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = [var.security_group_id]
  publicly_accessible    = false
}
