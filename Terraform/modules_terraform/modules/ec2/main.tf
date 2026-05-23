resource "aws_instance" "this" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  subnet_id              = var.subnet_id
  vpc_security_group_ids = var.security_group_id[*]
  key_name               = "my-first-ec2-key-pair"
  iam_instance_profile   = "AWS_EC2_SSM_Connect"
  user_data              = <<-EOF
                #!/bin/bash
                set -x

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
  tags = {
    Name = "my-${var.name}-ec2"
  }

  depends_on = []
}
