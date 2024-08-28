# (C) Copyright IBM Corporation 2019, 2024
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
ARG DOCKER_REPO
ARG	IMAGE_TAG

FROM ${DOCKER_REPO}/${IMAGE_TAG}

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN microdnf install -y tzdata openssl curl wget ca-certificates fontconfig glibc-langpack-en zip gzip tar git vim findutils \
    && microdnf update -y; microdnf clean all

LABEL name="Universal-Connector plugins" \
      vendor="International Business Machines Corporation" \
      version="11.0.24.0" \
      release="11" \
      summary="Universal-Connector plugins build image based on IBM Semeru Runtime Certified Edition Docker Image for OpenJDK with openj9 and ubi-minimal" \
      description="For more information on this image please see https://github.com/IBM/universal-connectors/blob/main/README.md"

ENV JAVA_VERSION 11.0.24.0

RUN set -eux; \
    ARCH="$(uname -m)"; \
    case "${ARCH}" in \
       amd64|x86_64) \
         ESUM='3b053d212479dea65b4875c5c29684468ec792e21eadfbc9e1c14769eaf470eb'; \
         BINARY_URL='https://github.com/ibmruntimes/semeru11-certified-binaries/releases/download/jdk-11.0.24%2B8_openj9-0.46.0/ibm-semeru-certified-jdk_x64_linux_11.0.24.0.tar.gz'; \
         ;; \
       ppc64el|ppc64le) \
         ESUM='9aeabcd5147902185a3ba397e0cb8ec60469586ce074c5131e4d83dd81578e1b'; \
         BINARY_URL='https://github.com/ibmruntimes/semeru11-certified-binaries/releases/download/jdk-11.0.24%2B8_openj9-0.46.0/ibm-semeru-certified-jdk_ppc64le_linux_11.0.24.0.tar.gz'; \
         ;; \
       s390x) \
         ESUM='431b98816f73e0fa1dda8bed05c86c917ba7bdc07e55a59c495574a36ad54a86'; \
         BINARY_URL='https://github.com/ibmruntimes/semeru11-certified-binaries/releases/download/jdk-11.0.24%2B8_openj9-0.46.0/ibm-semeru-certified-jdk_s390x_linux_11.0.24.0.tar.gz'; \
         ;; \
       aarch64|arm64) \
         ESUM='8f93c63279cf12b07d97eb8935d565d7ffd872c33d1b10b99880a2b6598f94f4'; \
         BINARY_URL='https://github.com/ibmruntimes/semeru11-certified-binaries/releases/download/jdk-11.0.24%2B8_openj9-0.46.0/ibm-semeru-certified-jdk_aarch64_linux_11.0.24.0.tar.gz'; \
         ;; \
       *) \
         echo "Unsupported arch: ${ARCH}"; \
         exit 1; \
         ;; \
    esac; \
    curl -LfsSo /tmp/openjdk.tar.gz ${BINARY_URL}; \
    echo "${ESUM} */tmp/openjdk.tar.gz" | sha256sum -c -; \
    mkdir -p /opt/ibm/; \
    cd /opt/ibm/; \
    tar -xf /tmp/openjdk.tar.gz ; \
    mkdir -p /licenses; \
    mv /opt/ibm/jdk-11* /opt/ibm/java; \
    cp /opt/ibm/java/legal/java.base/license/license_en.txt /licenses; \
    rm -rf /tmp/openjdk.tar.gz;

ENV JAVA_HOME=/opt/ibm/java \
    PATH=/opt/ibm/java/bin:$PATH \
    IBM_JAVA_OPTIONS="-XX:+UseContainerSupport"

# Create OpenJ9 SharedClassCache (SCC) for bootclasses to improve the java startup.
# Downloads and runs tomcat to generate SCC for bootclasses at /opt/ibm/java/.scc/openj9_system_scc
# Does a dry-run and calculates the optimal cache size and recreates the cache with the appropriate size.
# With SCC, OpenJ9 startup is improved ~50% with an increase in image size of ~14MB
# Application classes can be create a separate cache layer with this as the base for further startup improvement

RUN set -eux; \
    unset OPENJ9_JAVA_OPTIONS; \
    SCC_SIZE="50m"; \
    DOWNLOAD_PATH_TOMCAT=/tmp/tomcat; \
    INSTALL_PATH_TOMCAT=/opt/tomcat-home; \
    TOMCAT_CHECKSUM="3069924eb7041ccc0f2aeceb7d8626793a1a073a5b739a840d7974a18ebeb26cc3374cc5f4a3ffc74d3b019c0cb33e3d1fe96296e6663ac75a73c1171811726d"; \
    TOMCAT_DWNLD_URL="https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.93/bin/apache-tomcat-9.0.93.tar.gz"; \
    \
    mkdir -p "${DOWNLOAD_PATH_TOMCAT}" "${INSTALL_PATH_TOMCAT}"; \
    curl -LfsSo "${DOWNLOAD_PATH_TOMCAT}"/tomcat.tar.gz "${TOMCAT_DWNLD_URL}"; \
    echo "${TOMCAT_CHECKSUM} *${DOWNLOAD_PATH_TOMCAT}/tomcat.tar.gz" | sha512sum -c -; \
    tar -xf "${DOWNLOAD_PATH_TOMCAT}"/tomcat.tar.gz -C "${INSTALL_PATH_TOMCAT}" --strip-components=1; \
    rm -rf "${DOWNLOAD_PATH_TOMCAT}"; \
    \
    java -Xshareclasses:name=dry_run_scc,cacheDir=/opt/ibm/java/.scc,bootClassesOnly,nonFatal,createLayer -Xscmx$SCC_SIZE -version; \
    export OPENJ9_JAVA_OPTIONS="-XX:+IProfileDuringStartupPhase -Xshareclasses:name=dry_run_scc,cacheDir=/opt/ibm/java/.scc,bootClassesOnly,nonFatal"; \
    "${INSTALL_PATH_TOMCAT}"/bin/startup.sh; \
    sleep 5; \
    "${INSTALL_PATH_TOMCAT}"/bin/shutdown.sh -force; \
    sleep 15; \
    FULL=$( (java -Xshareclasses:name=dry_run_scc,cacheDir=/opt/ibm/java/.scc,printallStats 2>&1 || true) | awk '/^Cache is [0-9.]*% .*full/ {print substr($3, 1, length($3)-1)}'); \
    DST_CACHE=$(java -Xshareclasses:name=dry_run_scc,cacheDir=/opt/ibm/java/.scc,destroy 2>&1 || true); \
    SCC_SIZE=$(echo $SCC_SIZE | sed 's/.$//'); \
    SCC_SIZE=$(awk "BEGIN {print int($SCC_SIZE * $FULL / 100.0)}"); \
    [ "${SCC_SIZE}" -eq 0 ] && SCC_SIZE=1; \
    SCC_SIZE="${SCC_SIZE}m"; \
    java -Xshareclasses:name=openj9_system_scc,cacheDir=/opt/ibm/java/.scc,bootClassesOnly,nonFatal,createLayer -Xscmx$SCC_SIZE -version; \
    unset OPENJ9_JAVA_OPTIONS; \
    \
    export OPENJ9_JAVA_OPTIONS="-XX:+IProfileDuringStartupPhase -Xshareclasses:name=openj9_system_scc,cacheDir=/opt/ibm/java/.scc,bootClassesOnly,nonFatal"; \
    "${INSTALL_PATH_TOMCAT}"/bin/startup.sh; \
    sleep 5; \
    "${INSTALL_PATH_TOMCAT}"/bin/shutdown.sh -force; \
    sleep 5; \
    FULL=$( (java -Xshareclasses:name=openj9_system_scc,cacheDir=/opt/ibm/java/.scc,printallStats 2>&1 || true) | awk '/^Cache is [0-9.]*% .*full/ {print substr($3, 1, length($3)-1)}'); \
    echo "SCC layer is $FULL% full."; \
    rm -rf "${INSTALL_PATH_TOMCAT}"; \
    if [ -d "/opt/ibm/java/.scc" ]; then \
          chmod -R 0777 /opt/ibm/java/.scc; \
    fi; \
    \
    echo "SCC generation phase completed";

ENV ETC=/etc \
    LOGSTASH_VERSION=logstash-oss-8.3.3-linux-x86_64 \
    LOGSTASH_GIT_TAG=v8.3.3 \
    JRUBY_VERSION=9.4.5.0 \
    LOGSTASH_DIR=/usr/share/logstash \
    LOGSTASH_SRC_DIR=/usr/share/logstashSRC

#Change ownership to guc
RUN groupadd -g 991 guardium && useradd -G guardium -u 1001 guc && \
    mkdir -p ${LOGSTASH_DIR} ${LOGSTASH_SRC_DIR} && chown -R guc:guardium ${LOGSTASH_DIR} ${LOGSTASH_SRC_DIR}
RUN cd ${LOGSTASH_SRC_DIR} && git clone -b "${LOGSTASH_GIT_TAG}" https://github.com/elastic/logstash.git
RUN cd ${LOGSTASH_SRC_DIR}/logstash && ./gradlew --version && ./gradlew clean --warning-mode all && ./gradlew assemble
#Jruby
RUN curl https://repo1.maven.org/maven2/org/jruby/jruby-dist/${JRUBY_VERSION}/jruby-dist-${JRUBY_VERSION}-bin.tar.gz -o jruby-dist-${JRUBY_VERSION}-bin.tar.gz && \
    tar -xzf jruby-dist-${JRUBY_VERSION}-bin.tar.gz && rm -rf jruby-dist-${JRUBY_VERSION}-bin.tar.gz && \
    chown -R guc:guardium jruby-${JRUBY_VERSION}
ENV PATH=$PATH:/jruby-${JRUBY_VERSION}/bin

#Change User to guc
USER guc
#Download and extract logstash
RUN curl https://artifacts.elastic.co/downloads/logstash/${LOGSTASH_VERSION}.tar.gz -o ${LOGSTASH_DIR}/${LOGSTASH_VERSION}.tar.gz && \
    tar -xzf ${LOGSTASH_DIR}/${LOGSTASH_VERSION}.tar.gz  -C ${LOGSTASH_DIR} --strip-components=1 && \
	rm ${LOGSTASH_DIR}/*.tar.gz

ENV LS_JAVA_HOME=${JAVA_HOME}

WORKDIR ${LOGSTASH_DIR}