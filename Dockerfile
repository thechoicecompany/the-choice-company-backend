FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY pom.xml .

RUN apt-get update && apt-get install -y maven

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8089

CMD ["java", "-jar", "app.jar"]