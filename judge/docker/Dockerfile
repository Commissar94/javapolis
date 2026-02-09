FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

# Предварительно скачиваем зависимости для кэширования
COPY pom.xml .
# Используем help:evaluate и test-compile, чтобы Maven скачал плагины и провайдеры surefire
RUN mvn dependency:go-offline -B && \
    mvn org.apache.maven.plugins:maven-dependency-plugin:3.6.1:get -Dartifact=org.apache.maven.surefire:surefire-junit-platform:3.2.5 && \
    mvn test-compile -B || true

# Копируем остальное
COPY . .

CMD ["mvn","-ntp","-q","test"]
