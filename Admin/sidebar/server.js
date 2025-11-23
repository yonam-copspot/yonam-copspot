const path = require("path");
const express = require("express");
const {
  fetchComplaints,
  fetchCompletedComplaints,
  createComplaint,
  deleteComplaint,
  completeComplaint,
  createComment,
} = require("./lib/dbconnect");
const { fetchMyComplaints } = require("./lib/mobileQueries");

const app = express();
const PORT = process.env.PORT || 3000;
const publicDir = __dirname;
const chatbotDir = path.join(__dirname, "..", "..", "AIchatbot");
const projectRootDir = path.join(__dirname, "..", "project-root");

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

app.use(express.json({ limit: "15mb" }));

app.use(express.static(publicDir));
app.use("/chatbot-assets", express.static(chatbotDir));
app.use("/project-preview", express.static(projectRootDir));

app.get("/api/complaints", async (_req, res) => {
  try {
    const rows = await fetchComplaints(50);
    res.json(rows);
  } catch (err) {
    console.error("Failed to load complaints", err);
    res.status(500).json({ message: "DB_ERROR" });
  }
});

app.get("/api/completed", async (_req, res) => {
  try {
    const rows = await fetchCompletedComplaints(50);
    res.json(rows);
  } catch (err) {
    console.error("Failed to load completed complaints", err);
    res.status(500).json({ message: "DB_ERROR" });
  }
});

app.get("/api/mobile/completions", async (req, res) => {
  const rawLimit = Number(req.query.limit) || 100;
  const limit = Math.min(Math.max(rawLimit, 1), 300);

  try {
    const rows = await fetchCompletedComplaints(limit);
    const payload = rows.map((row) => ({
      id: row.id,
      user_id: row.user_id,
      registered_at: row.created_at,
      completed_at: row.done_at,
      address: row.location_name || "",
      detail: row.description || "",
    }));
    res.json(payload);
  } catch (err) {
    console.error("Failed to load mobile completion snapshot", err);
    res.status(500).json({ message: "DB_ERROR" });
  }
});

app.get("/api/mobile/my-complaints", async (req, res) => {
  const userId = (req.query.user_id || "").trim();
  if (!userId) {
    return res.status(400).json({ message: "MISSING_USER_ID" });
  }

  try {
    const rows = await fetchMyComplaints(userId);
    res.json(rows);
  } catch (err) {
    console.error("Failed to load my complaints", err);
    res.status(500).json({ message: "DB_ERROR" });
  }
});

// 앱에서 전송눌렀을때의 api -> db insert
app.post("/api/create", async (req, res) => {
  const {
    author_name,
    user_id,
    location_name,
    description,
    latitude,
    longitude,
    photo_base64,
  } = req.body || {};

  if (
    !author_name ||
    typeof user_id !== "string" ||
    !user_id.trim() ||
    !location_name ||
    typeof latitude !== "number" ||
    typeof longitude !== "number"
  ) {
    return res.status(400).json({ message: "INVALID_PAYLOAD" });
  }

  try {
    const normalizedDescription =
      typeof description === "string" ? description.trim() : "";
    const insertId = await createComplaint({
      author_name,
      user_id: user_id.trim(),
      location_name,
      description: normalizedDescription || null,
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
    const affected = await completeComplaint(numericId);
    if (!affected) {
      return res.status(404).json({ message: "NOT_FOUND" });
    }
    res.json({ message: "COMPLETED" });
  } catch (err) {
    console.error("Failed to complete complaint", err);
    res.status(500).json({ message: "DB_ERROR" });
  }
});

app.post("/api/comments", async (req, res) => {
  const { complaint_id, commenter_name, comment_text } = req.body || {};
  const numericComplaintId = Number(complaint_id);
  if (!numericComplaintId || !commenter_name || !comment_text) {
    return res.status(400).json({ message: "INVALID_COMMENT" });
  }

  try {
    const insertId = await createComment({
      complaint_id: numericComplaintId,
      commenter_name,
      comment_text,
    });
    res.status(201).json({ message: "COMMENT_CREATED", id: insertId });
  } catch (err) {
    console.error("Failed to create comment", err);
    res.status(500).json({ message: "DB_ERROR" });
  }
});

app.get("/completed", (_req, res) => {
  res.sendFile(path.join(publicDir, "completed.html"));
});

app.get("/chatbot", (_req, res) => {
  res.sendFile(path.join(chatbotDir, "chatbot.html"));
});

app.use((_req, res) => {
  res.sendFile(path.join(publicDir, "index.html"));
});

app.listen(PORT, () => {
  console.log(`Sidebar server running at http://localhost:${PORT}`);
});
