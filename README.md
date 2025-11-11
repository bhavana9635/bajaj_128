## Bajaj Finserv Health | Qualifier 1 | JAVA (Spring Boot)

This Spring Boot app performs the entire flow on startup:
- Generates a webhook and JWT access token
- Determines which SQL question applies based on the last two digits of `regNo`
- Loads the final SQL query (from either `application.properties` or classpath `sql/` files)
- Submits the final SQL query to the returned webhook URL (or default submission URL) using the JWT token

### Tech
- Java 17
- Spring Boot 3
- RestTemplate
- Maven build (fat JAR)

### Configure
Edit `src/main/resources/application.properties`:
```
app.name=John Doe
app.regNo=REG12347
app.email=john@example.com

# Option A: provide final SQL directly (higher priority)
# app.finalQuery=SELECT ... ;

# Option B: put SQL inside these files
app.question1SqlPath=sql/question1.sql
app.question2SqlPath=sql/question2.sql
```
Rules:
- If `app.finalQuery` is set, it will be used directly.
- Otherwise, the app checks last two digits of `regNo`:
  - Odd  → load `sql/question1.sql`
  - Even → load `sql/question2.sql`

### Build
```
mvn -q -e -DskipTests clean package
```
Artifact:
```
target/bajaj-qualifier-java-0.0.1-SNAPSHOT.jar
``>

### Run
```
java -jar target/bajaj-qualifier-java-0.0.1-SNAPSHOT.jar
```

On startup it will:
1) POST to `https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA` with your details
2) Use the returned `webhook` and `accessToken`
3) Submit:
```
POST <webhook or https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA>
Authorization: <accessToken>
Content-Type: application/json
Body: {"finalQuery":"<YOUR_SQL_QUERY_HERE>"}
```

### Notes
- No controller/endpoint triggers anything; the flow runs on app startup via `ApplicationRunner`.
- Authorization header uses the value from `accessToken` exactly as returned by API.
- Replace placeholder SQL in `src/main/resources/sql/*.sql` or set `app.finalQuery`.

### Submission
Upload to a public GitHub repository with:
- Code
- Final JAR in `target/`
- Provide the raw, downloadable link to the JAR file


