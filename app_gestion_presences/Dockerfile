# ── Stage 1 : Build ───────────────────────────────────────────────────────────
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17 AS build
WORKDIR /app

RUN yum install -y tar gzip

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -q

COPY src src
RUN ./mvnw package -DskipTests -q

# ── Stage 2 : Runtime ─────────────────────────────────────────────────────────
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17
WORKDIR /app
COPY --from=build /app/target/app_gestion_presences-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
