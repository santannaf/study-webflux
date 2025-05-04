FROM ghcr.io/graalvm/graalvm-community:21 AS builder

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew && ./gradlew nativeCompile

FROM container-registry.oracle.com/os/oraclelinux:9-slim
COPY --from=builder /app/rest/build/native/nativeCompile/meuapp /app/meuapp
EXPOSE 30001 30002 8080

RUN chmod +x /app/meuapp

WORKDIR /app

ENTRYPOINT ["/app/meuapp", "-Xmx256m"]
