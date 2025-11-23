const { pool } = require("./dbconnect");

const myComplaintsSql = `
  (
    SELECT
      c.id,
      c.author_name,
      c.user_id,
      c.location_name,
      c.description,
      c.created_at,
      NULL AS done_at,
      'processing' AS status,
      c.id AS source_id,
      'active' AS source_table
    FROM complaint c
    WHERE c.user_id = ?
  )
  UNION ALL
  (
    SELECT
      d.id,
      d.author_name,
      d.user_id,
      d.location_name,
      d.description,
      d.created_at,
      d.done_at,
      'completed' AS status,
      d.id AS source_id,
      'done' AS source_table
    FROM complaint_done d
    WHERE d.user_id = ?
  )
  ORDER BY created_at DESC
  LIMIT 200
`;

function fetchCommentsForComplaints(complaintIds) {
  return new Promise((resolve, reject) => {
    if (!complaintIds.length) return resolve(new Map());
    const placeholders = complaintIds.map(() => "?").join(",");
    const sql = `
      SELECT complaint_id, commenter_name, comment_text, created_at
      FROM complaint_comment
      WHERE complaint_id IN (${placeholders})
      ORDER BY created_at ASC
    `;
    pool.query(sql, complaintIds, (err, rows) => {
      if (err) return reject(err);
      const map = new Map();
      rows.forEach((row) => {
        if (!map.has(row.complaint_id)) {
          map.set(row.complaint_id, []);
        }
        map.get(row.complaint_id).push(row);
      });
      resolve(map);
    });
  });
}

function fetchCommentsForDone(doneIds) {
  return new Promise((resolve, reject) => {
    if (!doneIds.length) return resolve(new Map());
    const placeholders = doneIds.map(() => "?").join(",");
    const sql = `
      SELECT done_id, commenter_name, comment_text, created_at
      FROM complaint_done_comment
      WHERE done_id IN (${placeholders})
      ORDER BY created_at ASC
    `;
    pool.query(sql, doneIds, (err, rows) => {
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

async function fetchMyComplaints(userId) {
  return new Promise((resolve, reject) => {
    pool.query(myComplaintsSql, [userId, userId], async (err, rows) => {
      if (err) return reject(err);

      try {
        const activeIds = rows
          .filter((row) => row.source_table === "active")
          .map((row) => row.source_id);
        const doneIds = rows
          .filter((row) => row.source_table === "done")
          .map((row) => row.source_id);

        const [activeComments, doneComments] = await Promise.all([
          fetchCommentsForComplaints(activeIds),
          fetchCommentsForDone(doneIds),
        ]);

        const result = rows.map((row) => {
          const comments =
            row.source_table === "active"
              ? activeComments.get(row.source_id) || []
              : doneComments.get(row.source_id) || [];
          return {
            user_id: row.user_id,
            name: row.author_name,
            address: row.location_name || "",
            detail: row.description || "",
            status: row.status,
            registered_at: row.created_at,
            completed_at: row.done_at,
            comments,
          };
        });

        resolve(result);
      } catch (innerErr) {
        reject(innerErr);
      }
    });
  });
}

module.exports = {
  fetchMyComplaints,
};
