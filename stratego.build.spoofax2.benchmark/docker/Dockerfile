FROM ubuntu:20.04

ENV WORKDIR=/app
WORKDIR $WORKDIR

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install --no-install-recommends -y \
    git \
    make \
    wget \
    openjdk-8-jdk \
    locales \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Set the locale to UTF-8 (http://jaredmarkell.com/docker-and-locales/)
RUN locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# Directories
ENV DATA_DIR=${WORKDIR}/data
RUN mkdir ${DATA_DIR}

ENV BIN_DIR=${WORKDIR}/bin
RUN mkdir ${BIN_DIR}

ENV BUILD_DIR=${WORKDIR}/build
RUN mkdir ${BUILD_DIR}

# Install Maven 3
RUN cd ${BIN_DIR} &&\
    wget http://apache.mirror.anlx.net/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz &&\
    tar -zxf apache-maven-3.6.3-bin.tar.gz &&\
    cp -R apache-maven-3.6.3 /usr/local &&\
    ln -s /usr/local/apache-maven-3.6.3/bin/mvn /usr/bin/mvn &&\
    ln -s /usr/local/apache-maven-3.6.3/bin/mvnDebug /usr/bin/mvnDebug

# Local Maven settings (http://metaborg.org/dev/maven/#local-settings-file)
ENV MAVEN_HOME=/root/.m2
RUN mkdir ${MAVEN_HOME}
COPY maven-settings.xml /root/.m2/settings.xml
ENV MAVEN_OPTS="-Xms2G -Xmx2G -Xss16m"

# Git config
RUN git config --global user.name "Stratego2 Evaluation"
RUN git config --global user.email "t.j.b.hartman@student.tudelft.nl"

COPY Makefile .

ENTRYPOINT make all
