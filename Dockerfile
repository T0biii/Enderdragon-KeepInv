# syntax=docker/dockerfile:1
# Build the plugin jar without any local toolchain:
#   docker build --target artifact --output type=local,dest=dist .
#   docker build --target artifact --build-arg VERSION=1.2.3 --output type=local,dest=dist .
FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
ARG VERSION=

COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
RUN chmod +x gradlew

COPY src ./src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon build ${VERSION:+-Pversion=$VERSION} \
    && mkdir /out \
    && cp build/libs/*.jar /out/ \
    && rm -f /out/*-plain.jar

FROM scratch AS artifact
COPY --from=build /out/ /
