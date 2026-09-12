FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY pom.xml .

RUN apt-get update && apt-get install -y maven

COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/etc/secrets/application.properties"]