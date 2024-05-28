# CONFIG
SPOOFAX_VERSION=2.6.0-SNAPSHOT

BENCHMARK_REPO_URL=https://github.com/rensux/stratego.git
BENCHMARK_PROJECT=stratego.build.spoofax2.benchmark

TIL_REPO_URL?=https://github.com/MetaborgCube/TIL.git
TIL_PROJECT?=TIL
TIL_VERSION=0.1.0-SNAPSHOT

DATA_DIR?=./data
BUILD_DIR?=./build

STRATEGO_REPO_DIR=$(BUILD_DIR)/stratego
TIL_REPO_DIR=$(BUILD_DIR)/til
TIL_REPO=$(TIL_REPO_DIR)/.git/HEAD
TIL_POM_DIR=$(TIL_REPO_DIR)/$(TIL_PROJECT)
TIL_LANGUAGE_PROJECT=$(TIL_POM_DIR)
RESULTS_DIR=$(DATA_DIR)/benchmark_results

BENCHMARK_ARGS?=-e failing -rf json -rff "$(RESULTS_DIR)/`date +%Y%m%d-%H%M%S`_`echo $(TARGET) | tr -d '()|.'`.json" $(TARGET)

# DERIVATIVES - DO NOT CHANGE
MAVEN_REPO=$(HOME)/.m2/repository

BENCHMARK_DIR=$(STRATEGO_REPO_DIR)/$(BENCHMARK_PROJECT)
BENCHMARK_JAR=$(BENCHMARK_DIR)/target/benchmarks.jar

TIL_ARTIFACT=TIL-$(TIL_VERSION).spoofax-language
TIL_METABORG_FILE=$(TIL_LANGUAGE_PROJECT)/metaborg.yaml
TIL_METABORG_TEMPLATE_FILE=$(TIL_LANGUAGE_PROJECT)/_metaborg.yaml

TIL_MAVEN_FOLDER=$(MAVEN_REPO)/mb/cube/TIL/$(TIL_VERSION)
TIL_LANGUAGE=$(TIL_MAVEN_FOLDER)/$(TIL_ARTIFACT)

# Targets
.PHONY: all
all: run-benchmark

.PHONY: clean
clean:
	-rm -rf $(BUILD_DIR)

.PHONY: benchmark-jar
benchmark-jar: $(BENCHMARK_JAR)

$(BENCHMARK_JAR): $(STRATEGO_REPO_DIR)
	@echo "Building benchmark jar (includes testing benchmarks)"
	@cd $(BENCHMARK_DIR) && \
	mvn -q -nsu package

$(RESULTS_DIR):
	@echo "Result dir: $(RESULTS_DIR)"
	@mkdir -p $(RESULTS_DIR)

$(TIL_REPO_DIR):
	@echo "Cloning TIL repo"
	@git clone -q --depth 1 $(TIL_REPO_URL) $(TIL_REPO_DIR)

$(STRATEGO_REPO_DIR):
	@echo "Cloning Stratego repo"
	@git clone -q --depth 1 $(BENCHMARK_REPO_URL) $(STRATEGO_REPO_DIR)

$(TIL_REPO): $(TIL_REPO_DIR)
	@echo "Updating existing TIL repo"
	@cd $(TIL_REPO_DIR) && \
	git fetch -qap && \
	git checkout -qf $(TIL_BRANCH) && \
	git pull -q

$(TIL_METABORG_FILE): $(TIL_REPO)

.INTERMEDIATE: %/_metaborg.yaml
%/_metaborg.yaml: %/metaborg.yaml
	@mv $*/metaborg.yaml $*/_metaborg.yaml

$(TIL_LANGUAGE).O%: $(TIL_REPO) | $(TIL_METABORG_TEMPLATE_FILE)
	@echo "Building TIL with -O $*"
	@yq e '.language.stratego.args += ["-O", $*]' $(TIL_METABORG_TEMPLATE_FILE) > $(TIL_METABORG_FILE)
	@cd $(TIL_POM_DIR) && \
	mvn -q -nsu dependency:go-offline -DoverWriteIfNewer=false
	@$(MAKE) $(TIL_LANGUAGE)
	@mv $(TIL_LANGUAGE) $(TIL_LANGUAGE).O$*

# Don't depend on $(TIL_REPO), that overwrites the change made in $(TIL_LANGUAGE).O%
$(TIL_LANGUAGE):
	@cd $(TIL_POM_DIR) && \
	mvn -q -o clean install

.PHONY: til.O2 til.O3 til.O4
til.O2: $(TIL_LANGUAGE).O2
til.O3: $(TIL_LANGUAGE).O3
til.O4: $(TIL_LANGUAGE).O4

.PHONY: til-variants
til-variants: $(TIL_LANGUAGE).O2 $(TIL_LANGUAGE).O3 $(TIL_LANGUAGE).O4

.PHONY: prepare-benchmark
prepare-benchmark: $(BENCHMARK_JAR) | $(RESULTS_DIR)

.PHONY: run-benchmark
run-benchmark: prepare-benchmark
	cd $(BENCHMARK_DIR) && \
	java -jar $(BENCHMARK_JAR) $(BENCHMARK_ARGS)