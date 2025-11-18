const express = require("express");
const path = require("path");

const app = express();
const PORT = process.env.PORT || 3000;

// Serve static files from project-root (index.html etc.)
app.use(express.static(path.join(__dirname, "..", "project-root")));

// Basic health endpoint
app.get("/api/health", (req, res) => {
  res.json({ status: "ok", env: process.env.NODE_ENV || "development" });
});

// Fallback to index.html for SPA routing
app.get("*", (req, res) => {
  res.sendFile(path.join(__dirname, "..", "project-root", "index.html"));
});

app.listen(PORT, () => {
  console.log(`Sidebar server listening on http://localhost:${PORT}`);
});
