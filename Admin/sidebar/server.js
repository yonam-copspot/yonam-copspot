const express = require("express");
const path = require("path");

const app = express();
const PORT = process.env.PORT || 3000;

// Serve static files from sidebar first so visiting root shows sidebar index
app.use(express.static(path.join(__dirname)));
// Then fall back to original project-root static files if not found in sidebar
app.use(express.static(path.join(__dirname, "..", "project-root")));

// Explicit root route to always serve the sidebar index
app.get("/", (req, res) => {
  res.sendFile(path.join(__dirname, "index.html"));
});

// Basic health endpoint
app.get("/api/health", (req, res) => {
  res.json({ status: "ok", env: process.env.NODE_ENV || "development" });
});

// Fallback to sidebar/index.html for any non-API route
app.get("*", (req, res) => {
  // if request is for api, let it 404 or be handled above
  if (req.path.startsWith("/api")) return res.status(404).end();
  res.sendFile(path.join(__dirname, "index.html"));
});

app.listen(PORT, () => {
  console.log(`Sidebar server listening on http://localhost:${PORT}`);
});
