FROM openjdk:11-slim
ADD /driftlog.jar /driftlog.jar
ENTRYPOINT ["java", "-jar", "driftlog.jar"]
