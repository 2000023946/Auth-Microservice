# -----------------------------------------------------------------------------
# GLOBAL & PROJECT SETTINGS
# -----------------------------------------------------------------------------

variable "project_name" {
  description = "A unique name for the project, used as a prefix for all resources."
  type        = string
  default     = "webapp"
}

variable "environment" {
  description = "The deployment environment (e.g., dev, staging, prod)."
  type        = string
  default     = "dev"
}

variable "aws_region" {
  description = "The AWS region where all resources will be deployed."
  type        = string
  default     = "us-east-1"
}

# -----------------------------------------------------------------------------
# NETWORKING VARIABLES (for the Network Module)
# -----------------------------------------------------------------------------

variable "vpc_cidr_block" {
  description = "The overall IP address range for the VPC."
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "A list of CIDR blocks for subnets that can access the internet."
  type        = list(string)
  default     = ["10.0.0.0/24", "10.0.1.0/24"]
}

variable "private_subnet_cidrs" {
  description = "A list of CIDR blocks for subnets isolated from the internet."
  type        = list(string)
  default     = ["10.0.2.0/24", "10.0.3.0/24"]
}

variable "my_ip" {
  description = "Your personal IP address for secure SSH access to servers."
  type        = string
  # No default to force a secure value to be set.
}

# -----------------------------------------------------------------------------
# DATABASE VARIABLES (for the Database Module)
# -----------------------------------------------------------------------------

variable "db_name" {
  description = "The name for the initial database to be created."
  type        = string
  default     = "webappdb"
}

variable "db_username" {
  description = "The master username for the database."
  type        = string
  default     = "dbadmin"
}

variable "db_password" {
  description = "The master password for the database. Must be at least 16 characters."
  type        = string
  sensitive   = true # Prevents the password from being shown in logs or plan output.
  # No default for security.
}

variable "db_instance_class" {
  description = "The instance class for the database writer and reader instances."
  type        = string
  default     = "db.t3.medium"
}

variable "db_reader_instance_count" {
  description = "The number of read-replica instances to create."
  type        = number
  default     = 1
}

# -----------------------------------------------------------------------------
# APPLICATION SERVER VARIABLES (for the Server Module)
# -----------------------------------------------------------------------------

variable "app_server_instance_type" {
  description = "The EC2 instance type for the application servers."
  type        = string
  default     = "t3.micro"
}

variable "ec2_key_pair_name" {
  description = "The name of an EC2 key pair that already exists in your AWS account for SSH access."
  type        = string
  # No default, as this is account-specific.
}

variable "docker_image" {
  description = "The Docker image to run on the app server."
  type        = string
  default     = "2000023946/auth-app:latest"
}