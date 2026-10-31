FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src/ src/
# Integration tests require Docker and are run separately on the host/CI.
RUN ./mvnw -B -Pdeployment -Dmaven.test.skip=true package \
    && cp target/voteeasy-*.jar /workspace/app.jar

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd --system voteeasy && useradd --system --gid voteeasy voteeasy
COPY --from=build --chown=voteeasy:voteeasy /workspace/app.jar app.jar
USER voteeasy
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
