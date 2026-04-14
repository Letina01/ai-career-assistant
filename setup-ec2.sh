#!/bin/bash

# ============================================
# EC2 Setup Script for AI Career Assistant
# ============================================
# Run this script ONCE when setting up a new EC2 instance
# Usage: chmod +x setup-ec2.sh && ./setup-ec2.sh

set -e

echo "============================================"
echo "AI Career Assistant - EC2 Setup"
echo "============================================"

# Update system
echo "[1/7] Updating system packages..."
sudo apt update && sudo apt upgrade -y

# Install Docker
echo "[2/7] Installing Docker..."
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Start and enable Docker
echo "[3/7] Starting Docker..."
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# Install Git
echo "[4/7] Installing Git..."
sudo apt install -y git

# Create app directory
echo "[5/7] Creating application directory..."
mkdir -p /home/$USER/career-ai
cd /home/$USER/career-ai

# Clone repository (uncomment and modify if using git)
# echo "[6/7] Cloning repository..."
# git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git .

# Create .env file
echo "[6/7] Creating .env file..."
cat > .env << 'EOF'
SPRING_PROFILES_ACTIVE=prod
DB_HOST=your-rds-endpoint.amazonaws.com
DB_PORT=3306
DB_NAME=career_assistant
DB_USER=your_db_username
DB_PASSWORD=your_db_password
JWT_SECRET=change-this-to-a-random-secret-key-at-least-32-chars
OPENAI_API_KEY=your-openai-api-key
VITE_API_BASE_URL=https://your-ec2-public-ip/api
EOF

# Setup firewall
echo "[7/7] Configuring firewall..."
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8080/tcp
sudo ufw --force enable

echo "============================================"
echo "Setup complete!"
echo "============================================"
echo ""
echo "Next steps:"
echo "1. Edit /home/$USER/career-ai/.env with your actual values"
echo "2. Copy Dockerfile.backend, Dockerfile.frontend, and docker-compose.yml to /home/$USER/career-ai/"
echo "3. Run: cd /home/$USER/career-ai && docker-compose up -d"
echo "4. Check logs: docker-compose logs -f"
echo ""
echo "Your app will be available at:"
echo "  - Frontend: http://YOUR_EC2_IP"
echo "  - Backend:  http://YOUR_EC2_IP:8080"
echo "============================================"
