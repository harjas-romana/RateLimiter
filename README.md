
# 🚦 The "You Shall Not Pass" Rate Limiter 

Hey there! 👋 I built this project to figure out what actually happens when 1,000 people try to spam an API at the exact same millisecond. 

(Spoiler alert: My backend survived. Their spam didn't.)

This is a **Distributed Sliding Window Rate Limiter** built with Spring Boot and Redis. I also slapped together a sweet Vanilla JS dashboard so you can actually *see* the requests getting bounced in real-time.

### 🌐 Live Links
* **The Visualizer Dashboard (Try it out!):** https://harjas-ratelimiter-ui.onrender.com
* **The Live Backend API:** https://harjas-ratelimiter.onrender.com

---

## 🛠️ The Tech Stack
* **Backend:** Java 25, Spring Boot 3
* **Database:** Upstash Redis (Serverless Cloud DB)
* **Security:** JWT (JSON Web Tokens)
* **Frontend:** Vanilla HTML/CSS/JS (Because sometimes less is more, right?)
* **Deployment:** Docker, Render CI/CD pipeline

---

## 🧠 How It Works Under The Hood

If you just use a basic counter to limit requests (like 10 requests per minute), smart users can game the system by sending 10 requests at 1:59 and another 10 at 2:01. Bam—they just hit your server 20 times in two seconds. That's a classic "burst problem."

I fixed this by implementing a **Sliding Window** algorithm:
1. Every time a request comes in, I save its exact timestamp in a Redis Sorted Set (`ZSET`).
2. I instantly wipe out any timestamps older than 60 seconds.
3. If there are fewer than 10 timestamps left in the set, you get a `200 OK`.
4. If there are 10 or more, the Bouncer kicks in and hands you a `429 Too Many Requests`.

### 🛡️ The "Aha!" Moment (Beating Race Conditions)
If 50 requests hit the Spring Boot server at the exact same microsecond, Java threads will trip over each other. They'll all read the database at the same time, think it's empty, and let everyone through. 

To fix this, I didn't write the logic in Java. I wrote a custom **atomic Lua script** and pushed it directly into the Redis engine. Redis runs single-threaded, so it processes that script like a perfectly organized queue. No race conditions. Zero. 

I even wrote a stress test using Java's `ExecutorService` and a `CountDownLatch` to fire 1,000 simultaneous threads at it. Exactly 10 passed. Exactly 990 were blocked. Nailed it. 🎯

---

## 🚀 How to Run It Locally

Want to break it on your own machine? Piece of cake.

1. **Clone the repo:**
   ```bash
   git clone https://github.com/your-username/RateLimiter.git
   cd RateLimiter
   ```

2. **Set your environment variables:**
   You'll need your own Upstash Redis account. Set this variable in your terminal:
   ```bash
   export REDIS_PASSWORD=your_super_secret_password
   ```

3. **Fire up the backend:**
   ```bash
   mvn spring-boot:run
   ```

4. **Open the Dashboard:**
   Just double-click `frontend/index.html` to open it in your browser. Grab a dummy JWT from `jwt.io` (using the secret key from the code), paste it in, and hit the big green button.

---
*Built from scratch by Harjas. Always learning, always shipping.* 🚀