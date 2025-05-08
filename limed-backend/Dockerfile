FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu

WORKDIR /app

# Копируем собранный исполняемый JAR-файл
COPY build/libs/app.jar /app/app.jar

EXPOSE 8443

CMD ["java", "-jar", "/app/app.jar"]
