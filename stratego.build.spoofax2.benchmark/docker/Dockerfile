FROM spoofax/spoofax2-bench:latest

USER root

RUN apt-get update \
    && apt-get install --no-install-recommends -y \
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

# Git config
RUN git config --global user.name "Stratego2 Evaluation" && \
    git config --global user.email "t.j.b.hartman@student.tudelft.nl"

USER myuser
WORKDIR /home/myuser

COPY Makefile .

ENTRYPOINT make -n all; make all
