FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests && \
    cp target/concurrency-lab-1.0.0.jar app.jar && \
    apk del maven && \
    rm -rf target ~/.m2

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]