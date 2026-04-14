# AWS EC2 Deployment Guide

## Prerequisites

1. AWS Account with EC2 Free Tier
2. GitHub Repository
3. Domain name (optional, for HTTPS)

---

## Step 1: Launch EC2 Instance

1. Go to AWS EC2 Console
2. Click **Launch Instance**
3. Configure:
   - **Name**: `career-ai-server`
   - **OS**: Ubuntu 22.04 LTS (Free Tier)
   - **Instance Type**: `t2.micro` (Free Tier)
   - **Key Pair**: Create new or use existing
   - **Security Groups**:
     - SSH (22): Your IP
     - HTTP (80): 0.0.0.0/0
     - HTTPS (443): 0.0.0.0/0
     - Custom TCP (8080): 0.0.0.0/0

4. Launch instance
5. Note the **Public IPv4 Address**

---

## Step 2: Setup EC2 Instance

SSH into your EC2 instance:

```bash
ssh -i "your-key.pem" ubuntu@your-ec2-ip
```

Run the setup script:

```bash
chmod +x setup-ec2.sh
./setup-ec2.sh
```

---

## Step 3: Configure GitHub Secrets

Go to your GitHub repository → **Settings** → **Secrets and variables** → **Actions**

Add these secrets:

| Secret Name | Description | Example |
|-------------|-------------|---------|
| `EC2_HOST` | EC2 Public IP | `54.123.45.67` |
| `EC2_KEY` | Private key (.pem content) | `-----BEGIN RSA...` |
| `EC2_USER` | SSH username | `ubuntu` |
| `DB_HOST` | RDS endpoint | `mydb.xxx.amazonaws.com` |
| `DB_NAME` | Database name | `career_assistant` |
| `DB_USER` | Database username | `admin` |
| `DB_PASSWORD` | Database password | `secret123` |
| `JWT_SECRET` | JWT signing key | `your-secret-key-32chars` |
| `OPENAI_API_KEY` | OpenAI API key | `sk-xxx` |
| `VITE_API_BASE_URL` | Backend URL | `https://your-domain.com/api` |

---

## Step 4: Update Vite Config (Frontend)

In `frontend/vite.config.js`, update the proxy:

```javascript
server: {
  proxy: {
    '/api': {
      target: process.env.VITE_API_BASE_URL || 'http://localhost:8080',
      changeOrigin: true,
    }
  }
}
```

---

## Step 5: Push to GitHub

```bash
git add .
git commit -m "Add Docker & CI/CD configuration"
git push origin main
```

---

## Step 6: Monitor Deployment

1. Go to **GitHub** → **Actions** tab
2. Click on the running workflow
3. Watch the logs

If deployment fails, check:
- EC2 security group rules
- SSH key permissions
- GitHub secrets values

---

## Manual Deployment (without CI/CD)

On EC2 instance:

```bash
cd ~/career-ai
docker-compose down
docker-compose pull
docker-compose up -d --build
docker-compose logs -f
```

---

## SSL/HTTPS Setup (Recommended)

Install Certbot:

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com
```

---

## Troubleshooting

### Container won't start
```bash
docker-compose logs backend
docker-compose logs frontend
```

### Database connection failed
- Check security group allows port 3306 from EC2
- Verify DB credentials in .env

### Out of memory
- Add swap space:
```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

### View running containers
```bash
docker-compose ps
```

### Restart services
```bash
docker-compose restart backend frontend
```

---

## Estimated Monthly Cost (Free Tier)

- EC2 t2.micro: FREE (750 hrs/month)
- RDS MySQL: ~$12/month (or use Free Tier for 12 months)
- Data transfer: ~$1-5/month

Total: **~$0-15/month**
