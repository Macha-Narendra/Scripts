resource "aws_launch_template" "lt" {
  name_prefix            = "my-${var.name}-template"
  image_id               = "ami-05d2d839d4f73aafb"
  instance_type          = var.instance_type
  vpc_security_group_ids = [var.security_group_id]
  key_name               = "my-first-ec2-key-pair"
  iam_instance_profile {
    name = "AWS_EC2_SSM_Connect"
  }

  user_data = base64encode(<<EOF
#!/bin/bash

set -xe
# Retry until internet works
for i in {1..10}; do
  ping -c 1 google.com && break
  echo "Waiting for network..."
  sleep 5
done

echo "Network ready"
apt update -y
apt install -y nginx
systemctl start nginx
systemctl enable nginx
echo "Hello, World!" > /var/www/html/index.html
systemctl start snap.amazon-ssm-agent.amazon-ssm-agent.service
systemctl enable snap.amazon-ssm-agent.amazon-ssm-agent.service
EOF
  )
}

resource "aws_autoscaling_group" "asg" {
  desired_capacity = var.desired_capacity
  max_size         = 3
  min_size         = 1

  #health_check_grace_period = 300
  #health_check_type         = "ELB"
  #desired_capacity          = 4
  #force_delete              = true
  #placement_group           = aws_placement_group.test.id
  #launch_configuration      = aws_launch_configuration.foobar.name
  #vpc_zone_identifier       = [aws_subnet.example1.id, aws_subnet.example2.id]

  vpc_zone_identifier = var.private_subnet_ids

  launch_template {
    id      = aws_launch_template.lt.id
    version = "$Latest"
  }

  target_group_arns = [var.target_group_arn]
}
