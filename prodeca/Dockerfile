FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Ladataan riippuvuudet
COPY pom.xml .
RUN mvn dependency:go-offline

# Kopioidaan lähdekoodi ja rakennetaan JAR-tiedosto
COPY src ./src
RUN mvn clean package -Pproduction -DskipTests

FROM eclipse-temurin:21-jre-alpine AS production
WORKDIR /app

# Kopioidaan rakennettu JAR-tiedosto
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]