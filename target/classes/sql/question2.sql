SELECT
  e.emp_id,
  e.first_name,
  e.last_name,
  d.department_name,
  COUNT(e2.emp_id) AS younger_employees_count
FROM employee e
JOIN department d
  ON d.department_id = e.department
LEFT JOIN employee e2
  ON e2.department = e.department
 AND e2.dob > e.dob
GROUP BY
  e.emp_id, e.first_name, e.last_name, d.department_name
ORDER BY
  e.emp_id DESC;
