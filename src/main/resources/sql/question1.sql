WITH max_pay AS (
	SELECT MAX(p.amount) AS max_amount
	FROM payments p
	WHERE EXTRACT(DAY FROM p.payment_time) <> 1
)
SELECT
	p.amount AS salary,
	CONCAT(e.first_name, ' ', e.last_name) AS name,
	EXTRACT(YEAR FROM AGE(CURRENT_DATE, e.dob)) AS age,
	d.department_name
FROM payments p
JOIN max_pay mp
	ON p.amount = mp.max_amount
JOIN employee e
	ON e.emp_id = p.emp_id
JOIN department d
	ON d.department_id = e.department
WHERE EXTRACT(DAY FROM p.payment_time) <> 1
ORDER BY p.payment_time DESC, p.payment_id DESC
LIMIT 1;


