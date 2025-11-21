const path = require("path");
const express = require("express");
const {
  fetchComplaints,
  createComplaint,
  deleteComplaint,
} = require("./lib/dbconnect");

const app = express();
const PORT = process.env.PORT || 3000;
const publicDir = __dirname;

app.use((req, res, next) => {
  res.header("Access-Control-Allow-Origin", req.headers.origin || "*");
  res.header(
    "Access-Control-Allow-Headers",
    "Origin, X-Requested-With, Content-Type, Accept"
  );
  res.header("Access-Control-Allow-Methods", "GET,POST,OPTIONS");

  if (req.method === "OPTIONS") {
    return res.sendStatus(204);
  }

  next();
});

app.use(express.json());

app.use(express.static(publicDir));

app.get("/api/complaints", async (_req, res) => {
  try {
    const rows = await fetchComplaints(50);
    res.json(rows);
  } catch (err) {
    console.error("Failed to load complaints", err);
    res.status(500).json({ message: "DB_ERROR" });
  }
});

// 앱에서 전송눌렀을때의 api -> db insert
app.post("/api/create", async (req, res) => {
  const { author_name, location_name, latitude, longitude, photo_base64 } =
    req.body || {};

  if (
    !author_name ||
    !location_name ||
    typeof latitude !== "number" ||
    typeof longitude !== "number"
  ) {
    return res.status(400).json({ message: "INVALID_PAYLOAD" });
  }

  try {
    const insertId = await createComplaint({
      author_name,
      location_name,
      latitude,
      longitude,
      photo_base64,
    });
    res.status(201).json({ message: "CREATED", id: insertId });
  } catch (err) {
    console.error("Failed to insert complaint", err);
    res.status(500).json({ message: "DB_ERROR" });
  }
});

// 삭제 api -> db delete
app.post("/api/delete", async (req, res) => {
  const { id } = req.body || {};
  const numericId = Number(id);
  if (!numericId) {
    return res.status(400).json({ message: "INVALID_ID" });
  }

  try {
    const affected = await deleteComplaint(numericId);
    if (!affected) {
      return res.status(404).json({ message: "NOT_FOUND" });
    }
    res.json({ message: "DELETED" });
  } catch (err) {
    console.error("Failed to delete complaint", err);
    res.status(500).json({ message: "DB_ERROR" });
  }
});

app.use((_req, res) => {
  res.sendFile(path.join(publicDir, "index.html"));
});

app.listen(PORT, () => {
  console.log(`Sidebar server running at http://localhost:${PORT}`);
});
