# Dockerfile para desplegar en Render (runtime: Docker).
# El build ocurre en el proveedor: se compila con el wrapper de Maven
# (sin tests: las corren el CI local y de GitHub Actions) y se ejecuta
# el jar con JRE 25.

FROM eclipse-temurin:25-jdk AS build
WORKDIR /src

# Primero las dependencias (capa cacheada): cambia sólo si cambia el pom
COPY api-blank/pom.xml ./pom.xml
COPY api-blank/mvnw ./mvnw
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline || true

COPY api-blank/src ./src
RUN ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /src/target/api-blank-0.0.1-SNAPSHOT.jar app.jar

# Render asigna el puerto por la variable de entorno PORT (8080 por defecto)
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
