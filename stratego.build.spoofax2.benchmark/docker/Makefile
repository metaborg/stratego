# CONFIG
STRATEGO_REPO?=https://github.com/toinehartman/stratego.git
STRATEGO_VERSION_REF?=remotes/origin/benchmarks
STRATEGO_PROJECT?=stratego.lang
BENCHMARK_PROJECT?=stratego.build.spoofax2.benchmark
SPOOFAX_VERSION=2.6.0-SNAPSHOT

DATA_DIR?=./data
BUILD_DIR?=./build

REPO=$(BUILD_DIR)/stratego
RESULTS_DIR=$(DATA_DIR)/benchmark_results

# DERIVATiVES - DO NOT CHANGE
STRATEGO_DIR=$(REPO)/$(STRATEGO_PROJECT)
BENCHMARK_DIR=$(REPO)/$(BENCHMARK_PROJECT)

MAVEN_REPO=$(MAVEN_HOME)/repository
BENCHMARK_JAR=$(BENCHMARK_DIR)/target/benchmarks.jar
STRATEGO_LANGUAGE=$(MAVEN_REPO)/org/metaborg/stratego.lang/$(SPOOFAX_VERSION)/stratego.lang-$(SPOOFAX_VERSION).spoofax-language

UPDATE_REPO=$(REPO)/.git/HEAD

# Targets
.PHONY: all
all: run-benchmark


$(REPO):
	git clone $(STRATEGO_REPO) $(REPO)

.PHONY: fetch-repo
fetch-repo: $(REPO)
	cd $(REPO) && \
	git fetch -q --tags

$(UPDATE_REPO): fetch-repo
	cd $(REPO) && \
	git checkout -q $(STRATEGO_VERSION_REF)

$(STRATEGO_LANGUAGE): $(UPDATE_REPO)
	cd $(STRATEGO_DIR) && \
	mvn -q install

.PHONY: benchmark-jar
benchmark-jar: $(BENCHMARK_JAR)

$(BENCHMARK_JAR): $(UPDATE_REPO)
	cd $(BENCHMARK_DIR) && \
	mvn -q package

.SILENT: $(RESULTS_DIR)
$(RESULTS_DIR):
	-mkdir -p $(RESULTS_DIR)

.PHONY: run-benchmark
run-benchmark: $(STRATEGO_LANGUAGE) $(BENCHMARK_JAR) | $(RESULTS_DIR)
	cd $(BENCHMARK_DIR) && \
	java -jar $(BENCHMARK_JAR) -e failing -rff $(RESULTS_DIR)/`date +%Y%m%d-%H%M%S`.csv $(TARGET)
	@-curl "http://xdroid.net/api/message?k=k-9f63903546c4&t=Benchmark%20finished&c=Benchmark%20finished"

.PHONY: prepare-benchmark
prepare-benchmark: $(STRATEGO_LANGUAGE) $(BENCHMARK_JAR) | $(RESULTS_DIR)
	@-curl "http://xdroid.net/api/message?k=k-9f63903546c4&t=Benchmark%20prepared&c=Benchmark%20prepared"
