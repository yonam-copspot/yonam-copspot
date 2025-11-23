# Sidebar Node Setup

## Requirements

- Node.js 18+

## Install

```
npm install
```

## Environment

Copy `.env.example` to `.env` and fill in the DB connection values:

```
cp .env.example .env
# then edit .env to match your local MySQL settings
```

## Run

- Development (auto-restart)

```
npm run dev
```

- Production

```
npm start
```

The server hosts the files in this folder, exposes `/api/complaints`, and falls back to `index.html` for every other request.
