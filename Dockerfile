#FROM gradle:8.12.1-jdk21-graal AS build
FROM gradle:8.13-jdk21-graal AS build
WORKDIR /project
COPY . .
#COPY build.gradle.kts .
#COPY settings.gradle.kts .
#RUN gradle -p rest clean build && gradle -p rest nativeCompile
RUN ./gradlew -p rest nativeCompile

FROM container-registry.oracle.com/os/oraclelinux:9-slim
RUN groupadd graalvm && useradd -r -g graalvm app_user
COPY --from=build --chown=app_user:graalvm /project/build/native/nativeCompile/app app
EXPOSE 8080
USER app_user
ENTRYPOINT ["/app", "-Xms64m", "-Xmx128m"]
