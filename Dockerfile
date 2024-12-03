# Use a base image with Java installed
FROM openjdk:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built application JAR file into the container
# Replace "bank-application.jar" with the name of your generated JAR file
COPY target/bank-application.jar bank-application.jar

# Expose the port your Spring Boot application runs on (default is 8080)
EXPOSE 8080

# Set the command to run the application
ENTRYPOINT ["java", "-jar", "bank-application.jar"]
