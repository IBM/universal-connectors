# Arguments for the Docker repository and image tag
ARG DOCKER_REPO
ARG IMAGE_TAG

# Base image
FROM ${DOCKER_REPO}/${IMAGE_TAG}

# Set locale
ENV LANG='en_US.UTF-8' \
    LANGUAGE='en_US:en' \
    LC_ALL='en_US.UTF-8'

# Install necessary packages and clean up
RUN microdnf install -y \
        tzdata \
        openssl \
        curl \
        wget \
        ca-certificates \
        fontconfig \
        glibc-langpack-en \
        zip \
        gzip \
        tar \
        git \
        vim \
        findutils \
    && microdnf update -y \
    && microdnf clean all

# Metadata as described in the image
LABEL name="Universal-Connector plugins" \
      vendor="International Business Machines Corporation" \
      version="11.0.24.0" \
      release="11" \
      summary="Universal-Connector plugins build image based on IBM Semeru Runtime Certified Edition Docker Image for OpenJDK with openj9 and ubi-minimal" \
      description="For more information on this image please see https://github.com/IBM/universal-connectors/blob/main/README.md"

# Environment variables for Logstash
ENV LOGSTASH_VERSION=logstash-oss-8.3.3-linux-x86_64 \
    LOGSTASH_GIT_TAG=v8.3.3 \
    LOGSTASH_DIR=/usr/share/logstash \
    LOGSTASH_SRC_DIR=/usr/share/logstashSRC

# Create and set ownership for Logstash directories
RUN groupadd -g 991 guardium \
    && useradd -G guardium -u 1001 guc \
    && mkdir -p ${LOGSTASH_DIR} ${LOGSTASH_SRC_DIR} \
    && chown -R guc:guardium ${LOGSTASH_DIR} ${LOGSTASH_SRC_DIR}

# Clone and build Logstash from source
USER guc
RUN git clone -b "${LOGSTASH_GIT_TAG}" https://github.com/elastic/logstash.git ${LOGSTASH_SRC_DIR}/logstash \
    && cd ${LOGSTASH_SRC_DIR}/logstash \
    && ./gradlew --version \
    && ./gradlew clean --warning-mode all \
    && ./gradlew assemble

# Download and extract Logstash
USER root
RUN curl -L https://artifacts.elastic.co/downloads/logstash/${LOGSTASH_VERSION}.tar.gz -o ${LOGSTASH_DIR}/${LOGSTASH_VERSION}.tar.gz \
    && tar -xzf ${LOGSTASH_DIR}/${LOGSTASH_VERSION}.tar.gz -C ${LOGSTASH_DIR} --strip-components=1 \
    && rm ${LOGSTASH_DIR}/${LOGSTASH_VERSION}.tar.gz

# Set environment variables to use Java and JRuby bundled in Logstash
ENV JAVA_HOME=${LOGSTASH_DIR}/jdk \
    PATH=${LOGSTASH_DIR}/jdk/bin:${LOGSTASH_DIR}/vendor/jruby/bin:$PATH \
    LS_JAVA_HOME=${LOGSTASH_DIR}/jdk

# Set working directory
WORKDIR ${LOGSTASH_DIR}
