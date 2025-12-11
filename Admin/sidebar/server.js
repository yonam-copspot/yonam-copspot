const path = require("path");
const http = require("http");
const https = require("https");
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

// 이 프로젝트에서는 HTML 파일이 이 파일과 같은 폴더에 있다고 가정
// (index.html, completed.html, chatbot.html 등)
const publicDir = __dirname;

// 필요하면 유지 (실제로 폴더 있으면 사용, 없으면 신경 안 써도 됨)
const chatbotDir = path.join(__dirname, "..", "..", "AIchatbot");
const projectRootDir = path.join(__dirname, "..", "project-root");

// LM Studio 설정
const LM_STUDIO_BASE_URL = "http://127.0.0.1:1234";
const LM_STUDIO_API_KEY = process.env.LM_STUDIO_API_KEY || "lm-studio-local";

// ----------------- LM Studio 프록시 함수 -----------------
function forwardToLmStudio(payload = {}) {
  return new Promise((resolve, reject) => {
    let targetUrl;
    try {
      targetUrl = new URL("/v1/chat/completions", LM_STUDIO_BASE_URL);
    } catch (error) {
      return reject(error);
    }

    const serialized = JSON.stringify(payload);
    const isHttps = targetUrl.protocol === "https:";
    const client = isHttps ? https : http;

    const requestOptions = {
      hostname: targetUrl.hostname,
      port: targetUrl.port || (isHttps ? 443 : 80),
      path: `${targetUrl.pathname}${targetUrl.search}`,
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${LM_STUDIO_API_KEY}`,
        "Content-Length": Buffer.byteLength(serialized),
      },
      timeout: 20000,
    };

    const req = client.request(requestOptions, (resp) => {
      let data = "";
      resp.setEncoding("utf8");
      resp.on("data", (chunk) => {
        data += chunk;
      });
      resp.on("end", () => {
        const status = resp.statusCode || 0;
        if (status >= 200 && status < 300) {
          try {
            resolve(data ? JSON.parse(data) : {});
          } catch (parseError) {
            reject(new Error("LM Studio 응답을 JSON으로 파싱할 수 없습니다."));
          }
        } else {
          reject(
            new Error(
              `LM Studio 응답 오류 (${status || "N/A"}): ${data || "No body"}`
            )
          );
        }
      });
    });

    req.on("error", reject);
    req.on("timeout", () => {
      req.destroy(new Error("LM Studio 요청이 시간 초과되었습니다."));
    });

    req.write(serialized);
    req.end();
  });
}

// ----------------- 공통 미들웨어 / 정적 파일 -----------------

// CORS 설정
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

// JSON 바디 파싱
app.use(express.json({ limit: "15mb" }));

// 정적 파일 서빙
app.use(express.static(publicDir));
app.use("/chatbot-assets", express.static(chatbotDir));
app.use("/project-preview", express.static(projectRootDir));

// ----------------- 민원 관련 API -----------------

// 현재 미처리 민원 목록
app.get("/api/complaints", async (_req, res) => {
  try {
    const rows = await fetchComplaints(50);
    res.json(rows);
  } catch (err) {
    console.error("Failed to load complaints", err);
    res.status(500).json({ message: "DB_ERROR" });
  }
});

// 처리 완료 민원 목록
app.get("/api/completed", async (_req, res) => {
  try {
    const rows = await fetchCompletedComplaints(50);
    res.json(rows);
  } catch (err) {
    console.error("Failed to load completed complaints", err);
    res.status(500).json({ message: "DB_ERROR" });
  }
});

// 모바일용: 완료 민원 스냅샷
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

// 모바일용: 특정 사용자 민원 목록
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

// 앱에서 "전송" 눌렀을 때: 신규 민원 등록
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

// 삭제(또는 완료 처리) API
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

// 민원 코멘트 추가
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

// ----------------- LM Studio 프록시 엔드포인트 -----------------

app.post("/api/chatbot-proxy", async (req, res) => {
  try {
    const lmStudioResponse = await forwardToLmStudio(req.body);
    res.setHeader("Content-Type", "application/json");
    res.json(lmStudioResponse);
  } catch (error) {
    console.error("Failed to proxy LM Studio request", error);
    res.status(502).json({
      message: "LM_STUDIO_PROXY_FAILED",
      detail: error.message,
    });
  }
});

// ----------------- 정적 페이지 라우팅 -----------------

app.get("/completed", (_req, res) => {
  res.sendFile(path.join(publicDir, "completed.html"));
});

app.get("/chatbot", (_req, res) => {
  res.sendFile(path.join(publicDir, "chatbot.html"));
});

// 그 외 모든 경로 → index.html (SPA 라우팅 용도)
app.use((_req, res) => {
  res.sendFile(path.join(publicDir, "index.html"));
});

// ----------------- 서버 시작 -----------------

app.listen(PORT, () => {
  console.log(`Sidebar server running at http://localhost:${PORT}`);
});
