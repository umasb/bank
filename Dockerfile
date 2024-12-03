# Base image with JDK 17
FROM eclipse-temurin:17-jdk as builder

# Set working directory inside the container
WORKDIR /app

# Copy the Maven project files to the container
COPY pom.xml ./
COPY src ./src/

# Install Maven dependencies and build the project
RUN apt-get update && apt-get install -y maven \
    && mvn clean package -DskipTests

# --------------------------------------------------
# Build the runtime image
FROM eclipse-temurin:17-jre

# Set working directory inside the container
WORKDIR /app

# Copy the built application JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port your application listens on
EXPOSE 8081

# Command to run the application
CMD ["java", "-jar", "app.jar"]
