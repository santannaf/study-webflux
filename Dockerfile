FROM ghcr.io/graalvm/graalvm-ce:21 as builder

RUN gu install native-image

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew &&\
   ./gradlew nativeCompile


FROM alpine:latest

RUN apk --no-cache add ca-certificates wget && \
    wget -q -O /etc/apk/keys/sgerrand.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub && \
    wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.28-r0/glibc-2.28-r0.apk && \
    apk add --force-overwrite glibc-2.28-r0.apk && \
    rm glibc-2.28-r0.apk

ENV LD_LIBRARY_PATH="/usr/glibc-compact/lib"
ENV PATH="/usr/glibc-compact/bin:$PATH"

COPY --from=builder /app/build/native/nativeCompile/meuapp /app/meuapp

RUN chmod +x /app/meuapp

WORKDIR /app

CMD ["./meuapp"]
