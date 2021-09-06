# CONFIG
STRATEGO_REPO?=https://github.com/toinehartman/stratego.git
STRATEGO_VERSION?=benchmarks
STRATEGO_PROJECT?=stratego.lang
BENCHMARK_PROJECT?=stratego.build.spoofax2.benchmark
SPOOFAX_VERSION=2.6.0-SNAPSHOT

DATA_DIR?=./data
BUILD_DIR?=./build

REPO_DIR=$(BUILD_DIR)/stratego

# DERIVATiVES - DO NOT CHANGE
STRATEGO_DIR=$(REPO_DIR)/$(STRATEGO_PROJECT)
BENCHMARK_DIR=$(REPO_DIR)/$(BENCHMARK_PROJECT)

MAVEN_REPO=$(MAVEN_HOME)/repository
BENCHMARK_JAR=$(BENCHMARK_DIR)/target/benchmarks.jar
UPDATE_REPO=$(REPO_DIR)/.git/HEAD
STRATEGO_LANGUAGE=$(MAVEN_REPO)/org/metaborg/stratego.lang/$(SPOOFAX_VERSION)/stratego.lang-$(SPOOFAX_VERSION).spoofax-language

all: run-benchmark

.PHONY: all run-benchmark $(UPDATE_REPO)

.DELETE_ON_ERROR: $(REPO_DIR)

$(REPO_DIR):
	git clone $(STRATEGO_REPO) $(REPO_DIR)

$(UPDATE_REPO): $(REPO_DIR)
	cd $(REPO_DIR) && \
	git checkout $(STRATEGO_VERSION) && \
	git pull --rebase

$(STRATEGO_LANGUAGE): $(UPDATE_REPO)
	cd $(STRATEGO_DIR) && \
	mvn install

$(BENCHMARK_JAR): $(UPDATE_REPO)
	cd $(BENCHMARK_DIR) && \
	mvn package

run-benchmark: $(STRATEGO_LANGUAGE) $(BENCHMARK_JAR)
	java -jar $(BENCHMARK_JAR) -f 2 -e failing -rff $(DATA_DIR)/benchmark_results/`date +%Y%m%d`.csv
