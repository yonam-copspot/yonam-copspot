const path = require("path");
require("dotenv").config({ path: path.join(__dirname, "..", ".env") });
const mysql = require("mysql");

const {
  DB_HOST = "127.0.0.1",
  DB_PORT = 3306,
  DB_USER = "root",
  DB_PASSWORD = "",
  DB_NAME = "complaint_db",
  DB_CONNECTION_LIMIT = 5,
} = process.env;

const pool = mysql.createPool({
  host: DB_HOST,
  port: Number(DB_PORT) || 3306,
  user: DB_USER,
  password: DB_PASSWORD,
  database: DB_NAME,
  connectionLimit: Number(DB_CONNECTION_LIMIT) || 5,
  charset: "utf8mb4",
});

const complaintQuery = `
  SELECT id,
         author_name,
         location_name,
         latitude,
         longitude,
         photo_base64,
         created_at
  FROM complaint
  ORDER BY created_at DESC
`;

function fetchComplaints(limit = 50) {
  return new Promise((resolve, reject) => {
    const sql = `${complaintQuery} LIMIT ?`;
    pool.query(sql, [limit], (err, rows) => {
      if (err) return reject(err);
      resolve(rows);
    });
  });
}

function createComplaint({
  author_name,
  location_name,
  latitude,
  longitude,
  photo_base64,
}) {
  return new Promise((resolve, reject) => {
    const sql = `
      INSERT INTO complaint (author_name, location_name, latitude, longitude, photo_base64)
      VALUES (?, ?, ?, ?, ?)
    `;
    const payload = [
      author_name,
      location_name,
      latitude,
      longitude,
      photo_base64 || "",
    ];

    pool.query(sql, payload, (err, result) => {
      if (err) return reject(err);
      resolve(result.insertId);
    });
  });
}

function deleteComplaint(id) {
  return new Promise((resolve, reject) => {
    pool.query("DELETE FROM complaint WHERE id = ?", [id], (err, result) => {
      if (err) return reject(err);
      resolve(result.affectedRows);
    });
  });
}

module.exports = { fetchComplaints, createComplaint, deleteComplaint, pool };

if (require.main === module) {
  fetchComplaints(5)
    .then((rows) => {
      console.table(rows);
      pool.end();
    })
    .catch((err) => {
      console.error("MySQL 테스트 조회 실패", err);
      pool.end();
    });
}
