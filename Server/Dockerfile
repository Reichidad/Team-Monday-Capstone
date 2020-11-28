FROM openjdk:11-jdk
VOLUME /tmp
EXPOSE 8080
ARG JAR_FILE=target/demo-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} demo.jar
CMD ["java", "-jar", "/demo.jar"]
