FROM spoofax/spoofax2-bench:latest

USER root

RUN apt-get update \
    && apt-get install --no-install-recommends -y \
    locales \
    make \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

RUN mkdir bin && \
    cd bin && \
    wget -q https://github.com/mikefarah/yq/releases/download/v4.14.2/yq_linux_amd64.tar.gz -O - |\
    tar xz && mv yq_linux_amd64 /usr/bin/yq

# Set the locale to UTF-8 (http://jaredmarkell.com/docker-and-locales/)
RUN locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

USER myuser
ENV WORKDIR=/home/myuser
WORKDIR ${WORKDIR}

# Directories
ENV DATA_DIR=${WORKDIR}/data
RUN mkdir ${DATA_DIR}

ENV BUILD_DIR=${WORKDIR}/build
RUN mkdir ${BUILD_DIR}

# Git config
RUN git config --global user.name "Stratego2 Evaluation" && \
    git config --global user.email "t.j.b.hartman@student.tudelft.nl"

COPY Makefile .

ENTRYPOINT ["make", "all"]
