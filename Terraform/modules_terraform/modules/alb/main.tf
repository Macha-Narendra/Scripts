resource "aws_lb" "this" {
  name               = "my-${var.name}-alb"
  load_balancer_type = "application"
  internal           = false
  subnets            = var.public_subnet_ids
  #subnets            = [for subnet in aws_subnet.public : subnet.id]
  security_groups = [var.alb_sg_id]
  #enable_deletion_protection = true
}

resource "aws_lb_target_group" "this" {
  name     = "my-${var.name}-tg"
  port     = 80
  protocol = "HTTP"
  vpc_id   = var.vpc_id

  health_check {
    path = "/"
  }
}

resource "aws_lb_listener" "this" {
  load_balancer_arn = aws_lb.this.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.this.arn
  }
}
