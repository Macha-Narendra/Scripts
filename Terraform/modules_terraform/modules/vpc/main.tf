resource "aws_vpc" "this" {
  cidr_block           = var.vpc_cidr_block
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = {
    Name = "my-${var.name}-vpc"
  }
}

resource "aws_subnet" "this_public_subnet" {
  count                   = length(var.public_subnet_cidr_block)
  vpc_id                  = aws_vpc.this.id
  cidr_block              = var.public_subnet_cidr_block[count.index]
  availability_zone       = var.azs[count.index]
  map_public_ip_on_launch = true
  tags = {
    Name = "my-${var.name}-public-subnet-${count.index}"
  }
}

resource "aws_subnet" "this_private_subnet" {
  count                   = length(var.private_subnet_cidr_block)
  vpc_id                  = aws_vpc.this.id
  cidr_block              = var.private_subnet_cidr_block[count.index]
  availability_zone       = var.azs[count.index]
  map_public_ip_on_launch = false
  tags = {
    Name = "my-${var.name}-private-subnet-${count.index}"
  }
}

resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id
  tags = {
    Name = "my-${var.name}-igw"
  }
}

resource "aws_route_table" "this" {
  vpc_id = aws_vpc.this.id
  tags = {
    Name = "my-${var.name}-public-rt"
  }
}

resource "aws_route" "this" {
  route_table_id         = aws_route_table.this.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.this.id
}

resource "aws_route_table_association" "this_public_subnet" {
  count          = length(aws_subnet.this_public_subnet)
  subnet_id      = aws_subnet.this_public_subnet[count.index].id
  route_table_id = aws_route_table.this.id
}

resource "aws_eip" "this" {
  domain = "vpc"

  tags = {
    Name = "my-${var.name}-eip"
  }
}

resource "aws_nat_gateway" "this" {
  allocation_id = aws_eip.this.id
  subnet_id     = aws_subnet.this_public_subnet[0].id

  tags = {
    Name = "my-${var.name}-nat-gateway"
  }

  depends_on = [aws_internet_gateway.this]
}

resource "aws_route_table" "this_private" {
  vpc_id = aws_vpc.this.id

  tags = {
    Name = "my-${var.name}-private-rt"
  }
}

resource "aws_route" "this_private" {
  route_table_id         = aws_route_table.this_private.id
  destination_cidr_block = "0.0.0.0/0"
  nat_gateway_id         = aws_nat_gateway.this.id
}

resource "aws_route_table_association" "this_private" {
  count          = length(aws_subnet.this_private_subnet)
  subnet_id      = aws_subnet.this_private_subnet[count.index].id
  route_table_id = aws_route_table.this_private.id
}
