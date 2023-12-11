# CONFIG
SPOOFAX_VERSION=2.6.0-SNAPSHOT

BENCHMARK_REPO_URL=https://github.com/rensux/stratego.git
BENCHMARK_PROJECT=stratego.build.spoofax2.benchmark

DATA_DIR?=./data
BUILD_DIR?=./build

STRATEGO_REPO_DIR=$(BUILD_DIR)/stratego
RESULTS_DIR=$(DATA_DIR)/benchmark_results

BENCHMARK_ARGS?=-e failing -rf json -rff "$(RESULTS_DIR)/`date +%Y%m%d-%H%M%S` `echo $(TARGET) | tr -d '()|.'`.json" $(TARGET)

# DERIVATIVES - DO NOT CHANGE
MAVEN_REPO=$(HOME)/.m2/repository

BENCHMARK_DIR=$(STRATEGO_REPO_DIR)/$(BENCHMARK_PROJECT)
BENCHMARK_JAR=$(BENCHMARK_DIR)/target/benchmarks.jar

# Targets
.PHONY: all
all: run-benchmark

.PHONY: clean
clean:
	-rm -rf $(BUILD_DIR)

.PHONY: benchmark-jar
benchmark-jar: $(BENCHMARK_JAR)

$(BENCHMARK_JAR): $(STRATEGO_REPO_DIR)
	@echo "Building benchmark jar"
	@cd $(BENCHMARK_DIR) && \
	mvn -q package

$(RESULTS_DIR):
	@echo "Result dir: $(RESULTS_DIR)"
	@mkdir -p $(RESULTS_DIR)

$(STRATEGO_REPO_DIR):
	@echo "Cloning Stratego repo"
	@git clone -q  $(BENCHMARK_REPO_URL) $(STRATEGO_REPO_DIR)

.INTERMEDIATE: %/_metaborg.yaml
%/_metaborg.yaml: %/metaborg.yaml
	@mv $*/metaborg.yaml $*/_metaborg.yaml

.PHONY: run-benchmark
run-benchmark: $(BENCHMARK_JAR) | $(RESULTS_DIR)
	java -jar $(BENCHMARK_JAR) $(BENCHMARK_ARGS)

.PHONY: prepare-benchmark
prepare-benchmark: chocopy-variants $(BENCHMARK_JAR) | $(RESULTS_DIR)