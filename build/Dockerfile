# Arguments for the Docker repository and image tag
ARG DOCKER_REPO
ARG IMAGE_TAG

# Base image
FROM ${DOCKER_REPO}/${IMAGE_TAG}

# Environment variables for Logstash
ARG LOGSTASH_VERSION=8.3.3

# Metadata as described in the image
LABEL name="Universal-Connector plugins" \
      vendor="International Business Machines Corporation" \
      summary="Universal-Connector plugins build image based on ubi-minimal" \
      description="For more information on this image please see https://github.com/IBM/universal-connectors/blob/main/README.md"

# Set environment variables for Logstash installation
ENV LOGSTASH_VERSION=${LOGSTASH_VERSION} \
    LOGSTASH_OSS_FULL_VERSION=logstash-oss-${LOGSTASH_VERSION}-linux-x86_64 \
    LOGSTASH_GIT_TAG=v${LOGSTASH_VERSION} \
    LOGSTASH_SRC_DIR=/usr/share/logstashSRC \
    LOGSTASH_DIR=/usr/share/logstash

# Install necessary packages and clean up
RUN microdnf install -y tzdata openssl curl wget ca-certificates fontconfig \
                       glibc-langpack-en zip gzip tar git vim findutils && \
    microdnf update -y && \
    microdnf clean all

# Download and extract Logstash
RUN mkdir -p ${LOGSTASH_DIR} && \
    curl -L https://artifacts.elastic.co/downloads/logstash/${LOGSTASH_OSS_FULL_VERSION}.tar.gz \
         -o ${LOGSTASH_DIR}/${LOGSTASH_OSS_FULL_VERSION}.tar.gz && \
    tar -xzf ${LOGSTASH_DIR}/${LOGSTASH_OSS_FULL_VERSION}.tar.gz -C ${LOGSTASH_DIR} --strip-components=1 && \
    rm ${LOGSTASH_DIR}/${LOGSTASH_OSS_FULL_VERSION}.tar.gz

# Set environment variables for Logstash JDK and JRuby after installation
ENV JAVA_HOME="${LOGSTASH_DIR}/jdk" \
    JRUBY_HOME="${LOGSTASH_DIR}/vendor/jruby"

# Set up directories and permissions
RUN mkdir -p ${LOGSTASH_SRC_DIR} && \
    chown -R 1000:1000 ${LOGSTASH_DIR} ${LOGSTASH_SRC_DIR}

USER 1000

# Clone Logstash source code and build
RUN git clone -b "${LOGSTASH_GIT_TAG}" https://github.com/elastic/logstash.git ${LOGSTASH_SRC_DIR}/logstash && \
    cd ${LOGSTASH_SRC_DIR}/logstash && \
    ./gradlew clean -qq --warning-mode all && ./gradlew assemble -qq

ENV PATH="${PATH}:${JAVA_HOME}/bin:${JRUBY_HOME}/bin"

WORKDIR ${LOGSTASH_DIR}
