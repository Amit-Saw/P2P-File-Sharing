# P2P Frontend (React + Vite)

Quick start (from `Frontend/`):

1. Install dependencies:

```bash
npm install
```

2. Run development server:

```bash
npm run dev
```

3. Build for production:

```bash
npm run build
```

Notes:
- Development server runs on `http://localhost:3000` by default.
- API calls are proxied to `http://localhost:8080` under `/api`.
- Upload endpoint: `POST /upload` (multipart form-data) — the backend returns `{ port }`.
- Download endpoint: `GET /download?port=PORT` — fetches the file from the peer server started by the backend.

Next steps:
- Add authentication, file listing, and history UI.
- Optionally move proxy to `http://localhost/` if running nginx reverse proxy locally.
