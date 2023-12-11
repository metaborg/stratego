# CONFIG
RESULTS_DIR=./data/benchmark_results

BENCHMARK_ARGS?=-e failing -rf json -rff "$(RESULTS_DIR)/`date +%Y%m%d-%H%M%S` `echo $(TARGET) | tr -d '()|.'`.json" $(TARGET)

# DERIVATIVES - DO NOT CHANGE
MAVEN_REPO=$(HOME)/.m2/repository

BENCHMARK_JAR=./target/benchmarks.jar

# Targets
.PHONY: all
all: clean run-benchmark

.PHONY: clean
clean:
	@mvn -q clean

.PHONY: benchmark-jar
benchmark-jar: $(BENCHMARK_JAR)

$(BENCHMARK_JAR):
	@echo "Building benchmark jar"
	@mvn -q package

$(RESULTS_DIR):
	@echo "Result dir: $(RESULTS_DIR)"
	@mkdir -p $(RESULTS_DIR)

.INTERMEDIATE: %/_metaborg.yaml
%/_metaborg.yaml: %/metaborg.yaml
	@mv $*/metaborg.yaml $*/_metaborg.yaml

.PHONY: prepare-benchmark
prepare-benchmark: $(BENCHMARK_JAR) | $(RESULTS_DIR)

.PHONY: run-benchmark
run-benchmark: prepare-benchmark
	java -jar $(BENCHMARK_JAR) $(BENCHMARK_ARGS)