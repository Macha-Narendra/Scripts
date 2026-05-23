output "vpc_id" {
  value = aws_vpc.this.id
}

output "public_subnet_ids" {
  value = aws_subnet.this_public_subnet[*].id
}

output "private_subnet_ids" {
  value = aws_subnet.this_private_subnet[*].id
}

output "nat_gateway_id" {
  value = aws_nat_gateway.this.id
}

output "private_route_id" {
  value = aws_route.this_private.id
}

output "private_route_table_association_id" {
  value = aws_route_table_association.this_private[*].id
}
