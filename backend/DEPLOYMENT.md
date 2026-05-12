# Deployment with nginx

This backend can run behind nginx as a reverse proxy.

## Local testing with Docker

1. Build and start containers:
   ```bash
   docker compose up --build
   ```
2. Open http://localhost
3. The backend listens on port `8080`, while nginx listens on port `80`.

## Use on DigitalOcean App Platform

1. Deploy the `backend` folder using the provided `Dockerfile`.
2. Set the environment variable `PORT=8080` if needed.
3. Use nginx only if you deploy to a Droplet or custom container setup.

## Use on Railway

Railway can build from `Dockerfile`. It will expose the service on the port that Railway provides.
The app reads `PORT` from the environment, so it works automatically.

## nginx setup for a Droplet or VM

1. Build the backend jar locally or on the server:
   ```bash
   mvn package
   ```
2. Start the backend:
   ```bash
   java -jar target/*.jar
   ```
3. Install nginx on the server.
4. Copy `nginx.conf` to `/etc/nginx/conf.d/default.conf`.
5. Restart nginx:
   ```bash
   sudo systemctl restart nginx
   ```
6. Ensure ports 80 and 8080 are open.

## Notes

- The backend uses `PORT` from the environment and defaults to `8080`.
- nginx acts as a proxy to the backend on `/`.
- For production, use a domain and HTTPS if you host on a public server.
