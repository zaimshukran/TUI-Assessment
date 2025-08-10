variable "key_name" {
  description = "The name of EC2 Key Pair to allow SSH access"
  type        = string
}

variable "my_ip" {
  description = "Your public IP address with CIDR suffix for SSH access"
  type        = string
}
