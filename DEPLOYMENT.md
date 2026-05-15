# InkWell Docker And AWS Deployment

This setup runs backend services in Docker. MySQL and the frontend are intentionally not Dockerized.

## 1. Local Run

Start or keep your local MySQL running on port `3306`.

Copy the sample environment file:

```powershell
cd D:\inkwell
copy .env.example .env
```

Edit `.env` and fill real values:

```env
MYSQL_HOST=host.docker.internal
MYSQL_PORT=3306
APP_FRONTEND_URL=http://localhost:5173
VITE_API_BASE_URL=http://localhost:8080
RAZORPAY_KEY_ID=your_key
RAZORPAY_KEY_SECRET=your_secret
SMTP_USERNAME=your_gmail
SMTP_PASSWORD=your_gmail_app_password
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

Build and start backend services:

```powershell
docker compose up --build -d
docker compose ps
```

Run the frontend outside Docker:

```powershell
cd D:\inkwell\frontend
npm install
npm run dev
```

Open:

- Frontend: `http://localhost:5173`
- API Gateway: `http://localhost:8080`
- Eureka: `http://localhost:8761`
- RabbitMQ UI: `http://localhost:15672`

RabbitMQ login defaults to `inkwell / inkwell123`.

To see backend logs:

```powershell
docker compose logs -f api-gateway
docker compose logs -f auth-service
docker compose logs -f post-service
```

To stop Docker backend services:

```powershell
docker compose down
```

## 2. AWS EC2 Deployment

Use this when you want the backend containers on EC2 but MySQL outside Docker, preferably Amazon RDS.

1. Create an EC2 Ubuntu instance.
2. Create a MySQL database, preferably with Amazon RDS.
3. Open security-group inbound ports:
   - `22` for SSH
   - `8080` for API Gateway if you expose it directly
   - `8761` only temporarily for Eureka debugging
4. Install Docker:

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin git
sudo usermod -aG docker $USER
newgrp docker
```

5. Clone or upload this repo:

```bash
git clone <your-repo-url> inkwell
cd inkwell
cp .env.example .env
nano .env
```

6. In `.env`, set:

```env
MYSQL_HOST=YOUR_RDS_ENDPOINT
MYSQL_PORT=3306
APP_FRONTEND_URL=http://YOUR_FRONTEND_DOMAIN_OR_IP
RAZORPAY_KEY_ID=your_key
RAZORPAY_KEY_SECRET=your_secret
SMTP_USERNAME=your_email
SMTP_PASSWORD=your_app_password
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_secret
```

7. Start backend services:

```bash
docker compose up --build -d
docker compose ps
```

8. Deploy the frontend separately, for example with S3 + CloudFront, Amplify, Vercel, or a simple Nginx server. Build it with:

```bash
cd frontend
npm install
VITE_API_BASE_URL=http://YOUR_EC2_PUBLIC_IP:8080 npm run build
```

## 3. Google OAuth Setup

For local development, add this redirect URI in Google Cloud Console:

```text
http://localhost:8080/login/oauth2/code/google
```

For deployment, add:

```text
http://YOUR_API_GATEWAY_DOMAIN_OR_IP/login/oauth2/code/google
```

## 4. Production Notes

- Keep MySQL outside Docker, preferably Amazon RDS.
- Move uploaded media to S3 before serious production traffic.
- Put HTTPS in front of the frontend and API Gateway.
- Use AWS Secrets Manager or SSM Parameter Store for secrets.
- Keep Eureka closed to the public internet.
