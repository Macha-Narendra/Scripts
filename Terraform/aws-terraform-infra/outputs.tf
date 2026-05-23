output "instance_public_ip" {
  value = aws_instance.my_first_ec2.public_ip
}

output "instance_tags_all" {
  value = aws_instance.my_first_ec2.tags
}
