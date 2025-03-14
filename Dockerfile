FROM openjdk:21-ea-1-jdk-slim
LABEL authors="JuliusAgbame"
WORKDIR /app
COPY target/ABOUT-S3.jar app.jar
EXPOSE 8070


ENTRYPOINT ["java", "-jar", "/app/app.jar"]