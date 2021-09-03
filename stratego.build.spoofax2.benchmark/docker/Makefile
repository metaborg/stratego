STRATEGO_REPO?=https://github.com/toinehartman/stratego.git
STRATEGO_VERSION?=benchmarks
STRATEGO_PROJECT?=stratego.lang
BENCHMARK_PROJECT?=stratego.build.spoofax2.benchmark
DATA_DIR?=./data/

STRATEGO_DIR=$(DATA_DIR)/stratego

all: build-stratego build-benchmark

pull-stratego:
	if [ ! -d $(STRATEGO_DIR)/.git ]; then git clone $(STRATEGO_REPO) $(STRATEGO_DIR); fi
	cd $(STRATEGO_DIR) && \
	git checkout $(STRATEGO_VERSION) && \
	git pull --rebase

build-stratego: pull-stratego
	cd $(STRATEGO_DIR)/$(STRATEGO_PROJECT) && \
	mvn clean install

build-benchmark: pull-stratego
	cd $(STRATEGO_DIR)/$(BENCHMARK_PROJECT) && \
	mvn package
