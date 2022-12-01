FROM openjdk:17-alpine

WORKDIR /app
COPY ./target/binotify-soap-1.0-SNAPSHOT.jar /app

EXPOSE 5001

CMD ["java", "-jar", "binotify-soap-1.0-SNAPSHOT.jar"]