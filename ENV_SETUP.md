# 🔐 Environment Variables Setup Guide

## What Changed

Your `application.properties` file now uses **environment variables** instead of hardcoded secrets:

### Before (INSECURE ❌):
```properties
spring.datasource.password=your-actual-password
spring.mail.password=your-actual-app-password
aws.s3.access-key=your-actual-aws-key
aws.s3.secret-key=your-actual-aws-secret
```

### After (SECURE ✅):
```properties
spring.datasource.password=${DB_PASSWORD:your-password}
spring.mail.password=${MAIL_PASSWORD:your-app-password}
aws.s3.access-key=${AWS_ACCESS_KEY_ID:your-access-key}
aws.s3.secret-key=${AWS_SECRET_ACCESS_KEY:your-secret-key}
```

---

## 📁 Files Created/Modified

| File | Purpose | Description |
|------|---------|-------------|
| `.env` | Local Development | **DO NOT COMMIT** - Contains your real credentials |
| `.env.example` | Repository Template | Safe example file to show required variables |
| `.gitignore` | Already Protected | `.env` is already ignored from git |
| `build.gradle` | Dependencies | Added `dotenv-java` library |
| `config/EnvConfig.java` | Loader | Auto-loads `.env` on startup |
| `application.properties` | Updated | Now uses environment variables |

---

## 🚀 Quick Start

### Option 1: Using .env File (Recommended for Local Development)

1. **Edit your `.env` file** with your real credentials:
   ```bash
   nano .env
   # or use your IDE
   ```

2. **Fill in your secrets:**
   ```
   DB_PASSWORD=your-database-password
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-specific-password
   AWS_ACCESS_KEY_ID=your-aws-access-key
   AWS_SECRET_ACCESS_KEY=your-aws-secret-key
   ```

3. **Build and run:**
   ```bash
   ./gradlew clean build
   ./gradlew bootRun
   ```

### Option 2: Using Environment Variables (Production)

Set variables before running:

```bash
export DB_PASSWORD="your-password"
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-app-password"
export AWS_ACCESS_KEY_ID="your-access-key"
export AWS_SECRET_ACCESS_KEY="your-secret-key"

./gradlew bootRun
```

### Option 3: Using Docker ENV File

Create `.env.docker`:
```
DB_PASSWORD=your-password
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
```

Then run with Docker:
```bash
docker run --env-file .env.docker your-app
```

---

## 📋 Environment Variables Reference

### Database
```
DB_URL=jdbc:postgresql://localhost:5432/instagram
DB_USERNAME=postgres
DB_PASSWORD=your-password
```

### Email (Gmail SMTP)
```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### JWT
```
JWT_SECRET=your-jwt-secret-key-base64
```

### AWS S3
```
AWS_S3_BUCKET_NAME=your-bucket-name
AWS_S3_REGION=us-east-1
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
```

### Server
```
SERVER_PORT=8080
```

---

## ✅ Security Best Practices

1. **Never commit `.env` file**
   - ✅ `.gitignore` already protects it
   - Always check: `git status` before pushing

2. **Share `.env.example` instead**
   - Shows required variables
   - No real credentials

3. **For production, use:**
   - AWS Secrets Manager
   - HashiCorp Vault
   - Container orchestration secrets
   - CI/CD platform secrets (GitHub Actions, GitLab CI, etc.)

4. **Rotate secrets regularly:**
   - Change AWS keys quarterly
   - Use temporary credentials when possible

---

## 🔄 How It Works

### Load Order:
1. `.env` file is loaded by `EnvConfig.java` at startup
2. Variables are set in System properties
3. `application.properties` reads from System properties with fallbacks

### Fallback Values:
```properties
${VARIABLE_NAME:default-value}
```

If `VARIABLE_NAME` is not set, it uses `default-value`.

---

## ❌ Troubleshooting

### Issue: "Failed to read environment variable"
**Solution:** Make sure `.env` file exists in project root:
```bash
ls -la .env
```

### Issue: "Gradle won't rebuild"
**Solution:** Run clean build:
```bash
./gradlew clean build
```

### Issue: "Changes not applied"
**Solution:** Make sure application restarted:
1. Stop the running app
2. Update `.env`
3. Run `./gradlew bootRun` again

---

## 📚 Additional Resources

- [dotenv-java Documentation](https://github.com/cdimascio/dotenv-java)
- [Spring Boot Environment Variables](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [12-Factor App Config](https://12factor.net/config)

---

**Remember:** `.env` should NEVER be committed to Git. It's already in `.gitignore` ✅

