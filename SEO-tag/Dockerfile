
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/SEO-tag-0.0.1-SNAPSHOT.jar app.jar
COPY seo.html seo.html
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
