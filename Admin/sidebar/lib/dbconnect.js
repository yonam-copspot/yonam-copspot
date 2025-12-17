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

// 완료 코멘트 테이블 보장
const ensureDoneCommentTableSql = `
  CREATE TABLE IF NOT EXISTS complaint_done_comment (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    done_id BIGINT UNSIGNED NOT NULL,
    commenter_name VARCHAR(100) NOT NULL,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_done_comment_done
      FOREIGN KEY (done_id) REFERENCES complaint_done(id)
      ON DELETE CASCADE
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
`;

pool.query(ensureDoneCommentTableSql, (err) => {
  if (err) {
    console.error("Failed to ensure complaint_done_comment table", err);
  }
});

// ★ tags_json까지 포함해서 조회
const complaintQuery = `
  SELECT id,
         author_name,
         user_id,
         location_name,
         description,
         latitude,
         longitude,
         photo_base64,
         tags_json,
         created_at
  FROM complaint
  ORDER BY created_at DESC
`;

const completedQuery = `
  SELECT id,
         original_complaint_id,
         author_name,
         user_id,
         location_name,
         description,
         latitude,
         longitude,
         photo_base64,
         tags_json,
         created_at,
         done_at
  FROM complaint_done
  ORDER BY done_at DESC
`;

const commentQuery = `
  SELECT id,
         complaint_id,
         commenter_name,
         comment_text,
         created_at
  FROM complaint_comment
  WHERE complaint_id IN (?)
  ORDER BY created_at ASC
`;

function fetchComplaints(limit = 50) {
  return new Promise((resolve, reject) => {
    const sql = `${complaintQuery} LIMIT ?`;
    pool.query(sql, [limit], (err, complaints) => {
      if (err) return reject(err);
      if (!complaints.length) return resolve([]);

      const ids = complaints.map((c) => c.id);
      loadComments(ids)
        .then((commentMap) => {
          complaints.forEach((complaint) => {
            complaint.comments = commentMap.get(complaint.id) || [];
            // complaint.tags_json 은 그대로 서버에서 내려보내고
            // index.html에서 JSON.parse 해서 item.tags로 만듦
          });
          resolve(complaints);
        })
        .catch(reject);
    });
  });
}

function loadComments(ids) {
  return new Promise((resolve, reject) => {
    if (!ids.length) return resolve(new Map());
    pool.query(commentQuery, [ids], (err, rows) => {
      if (err) return reject(err);
      const map = new Map();
      rows.forEach((comment) => {
        if (!map.has(comment.complaint_id)) {
          map.set(comment.complaint_id, []);
        }
        map.get(comment.complaint_id).push(comment);
      });
      resolve(map);
    });
  });
}

function loadCompletedComments(doneIds) {
  return new Promise((resolve, reject) => {
    if (!doneIds.length) return resolve(new Map());
    const sql = `
      SELECT done_id,
             commenter_name,
             comment_text,
             created_at
      FROM complaint_done_comment
      WHERE done_id IN (?)
      ORDER BY created_at ASC
    `;
    pool.query(sql, [doneIds], (err, rows) => {
      if (err) return reject(err);
      const map = new Map();
      rows.forEach((row) => {
        if (!map.has(row.done_id)) {
          map.set(row.done_id, []);
        }
        map.get(row.done_id).push(row);
      });
      resolve(map);
    });
  });
}

function fetchCompletedComplaints(limit = 50) {
  return new Promise((resolve, reject) => {
    const sql = `${completedQuery} LIMIT ?`;
    pool.query(sql, [limit], (err, rows) => {
      if (err) return reject(err);
      if (!rows.length) return resolve([]);

      const doneIds = rows.map((row) => row.id);
      loadCompletedComments(doneIds)
        .then((commentMap) => {
          rows.forEach((row) => {
            row.comments = commentMap.get(row.id) || [];
          });
          resolve(rows);
        })
        .catch(reject);
    });
  });
}

/**
 * 신규 민원 생성
 * server.js에서 { ..., tags }를 넘기면 여기서 tags_json으로 직렬화해서 저장
 */
function createComplaint({
  author_name,
  user_id,
  location_name,
  description,
  latitude,
  longitude,
  photo_base64,
  tags, // ← 추가
}) {
  return new Promise((resolve, reject) => {
    const tagsJson =
      Array.isArray(tags) && tags.length ? JSON.stringify(tags) : null;

    const sql = `
      INSERT INTO complaint (
        photo_base64,
        author_name,
        user_id,
        location_name,
        description,
        latitude,
        longitude,
        tags_json
      )
      VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    `;
    const payload = [
      photo_base64 || "",
      author_name,
      user_id,
      location_name,
      description || null,
      latitude,
      longitude,
      tagsJson,
    ];

    pool.query(sql, payload, (err, result) => {
      if (err) return reject(err);
      resolve(result.insertId);
    });
  });
}

function createComment({ complaint_id, commenter_name, comment_text }) {
  return new Promise((resolve, reject) => {
    const sql = `
      INSERT INTO complaint_comment (complaint_id, commenter_name, comment_text)
      VALUES (?, ?, ?)
    `;
    pool.query(
      sql,
      [complaint_id, commenter_name, comment_text],
      (err, result) => {
        if (err) return reject(err);
        resolve(result.insertId);
      }
    );
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

/**
 * 민원 완료 처리
 * complaint → complaint_done 으로 복사할 때 tags_json도 같이 복사
 */
function completeComplaint(id) {
  return new Promise((resolve, reject) => {
    pool.getConnection((err, connection) => {
      if (err) return reject(err);

      const rollbackAndReject = (error) => {
        connection.rollback(() => {
          connection.release();
          reject(error);
        });
      };

      connection.beginTransaction((beginErr) => {
        if (beginErr) {
          connection.release();
          return reject(beginErr);
        }

        const selectSql = `
          SELECT id,
                 photo_base64,
                 author_name,
                 user_id,
                 location_name,
                 description,
                 latitude,
                 longitude,
                 tags_json,
                 created_at
          FROM complaint
          WHERE id = ? FOR UPDATE
        `;

        connection.query(selectSql, [id], (selectErr, rows) => {
          if (selectErr) {
            return rollbackAndReject(selectErr);
          }

          if (!rows.length) {
            return connection.rollback(() => {
              connection.release();
              resolve(0);
            });
          }

          const record = rows[0];
          const selectCommentsSql = `
            SELECT commenter_name, comment_text, created_at
            FROM complaint_comment
            WHERE complaint_id = ?
            ORDER BY created_at ASC
          `;

          connection.query(selectCommentsSql, [id], (commentErr, comments) => {
            if (commentErr) {
              return rollbackAndReject(commentErr);
            }

            const insertSql = `
                INSERT INTO complaint_done (
                  original_complaint_id,
                  photo_base64,
                  author_name,
                  user_id,
                  location_name,
                  description,
                  latitude,
                  longitude,
                  tags_json,
                  created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
              `;
            const insertPayload = [
              record.id,
              record.photo_base64 || "",
              record.author_name,
              record.user_id,
              record.location_name,
              record.description || null,
              record.latitude,
              record.longitude,
              record.tags_json || null,
              record.created_at,
            ];

            connection.query(
              insertSql,
              insertPayload,
              (insertErr, insertRes) => {
                if (insertErr) {
                  return rollbackAndReject(insertErr);
                }

                const doneId = insertRes.insertId;

                const copyComments = (cb) => {
                  if (!comments.length) return cb();
                  const values = comments.map((comment) => [
                    doneId,
                    comment.commenter_name,
                    comment.comment_text,
                    comment.created_at || new Date(),
                  ]);
                  const doneCommentSql = `
                      INSERT INTO complaint_done_comment (done_id, commenter_name, comment_text, created_at)
                      VALUES ?
                    `;
                  connection.query(doneCommentSql, [values], cb);
                };

                copyComments((copyErr) => {
                  if (copyErr) {
                    return rollbackAndReject(copyErr);
                  }

                  const deleteComments = (cb) => {
                    connection.query(
                      "DELETE FROM complaint_comment WHERE complaint_id = ?",
                      [id],
                      cb
                    );
                  };

                  deleteComments((deleteCommentErr) => {
                    if (deleteCommentErr) {
                      return rollbackAndReject(deleteCommentErr);
                    }

                    connection.query(
                      "DELETE FROM complaint WHERE id = ?",
                      [id],
                      (deleteErr, result) => {
                        if (deleteErr) {
                          return rollbackAndReject(deleteErr);
                        }

                        connection.commit((commitErr) => {
                          if (commitErr) {
                            return rollbackAndReject(commitErr);
                          }
                          connection.release();
                          resolve(result.affectedRows);
                        });
                      }
                    );
                  });
                });
              }
            );
          });
        });
      });
    });
  });
}

module.exports = {
  fetchComplaints,
  fetchCompletedComplaints,
  createComplaint,
  deleteComplaint,
  completeComplaint,
  createComment,
  loadComments,
  pool,
};

if (require.main === module) {
  fetchComplaints(5)
    .then((rows) => {
      console.table(rows);
      pool.end();
    })
    .catch((err) => {
      console.error("DB 연결 테스트 실패", err);
      pool.end();
    });
}
