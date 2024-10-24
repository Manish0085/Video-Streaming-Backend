FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build target/BrunchBliss-Canteen-0.0.1-SNAPSHOT.jar Video_Streaming_App.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "Video_Streaming_App.jar"]