# ==========================================
# STAGE 1: Build the application
# ==========================================
# We use a heavy image with Maven and Java 25 to compile your code
FROM maven:3.9-eclipse-temurin-25 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Compile the app and skip tests (since we already ran them locally!)
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Run the application
# ==========================================
# We use a lightweight Java 25 JRE image to actually run the app
FROM eclipse-temurin:25-jre

WORKDIR /app

# Steal the compiled .jar file from STAGE 1 and drop it here
COPY --from=builder /app/target/*.jar app.jar

# Tell the cloud provider we are listening on port 8080
EXPOSE 8080

# The command to start the Bouncer
ENTRYPOINT ["java", "-jar", "app.jar"]