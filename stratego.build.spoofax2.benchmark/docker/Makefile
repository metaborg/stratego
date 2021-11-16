# CONFIG
SPOOFAX_DOCKER_REPO?=https://gitlab.ewi.tudelft.nl/spoofax/docker-images.git

BENCHMARK_PROJECT=stratego.build.spoofax2.benchmark

CHOCOPY_REPO_URL?=git@gitlab.ewi.tudelft.nl:CS4200/admin/chocopy-grading.git
CHOCOPY_BRANCH?=toine-dev
CHOCOPY_PROJECT?=milestone3/reference
CHOCOPY_VERSION=0.1.0-SNAPSHOT

DATA_DIR?=./data
BUILD_DIR?=./build

SPOOFAX_REPO_DIR=$(BUILD_DIR)/spoofax-releng
CHOCOPY_REPO_DIR=$(BUILD_DIR)/chocopy-grading
CHOCOPY_REPO=$(CHOCOPY_REPO_DIR)/.git/HEAD
CHOCOPY_POM_DIR=$(CHOCOPY_REPO_DIR)/$(CHOCOPY_PROJECT)
CHOCOPY_LANGUAGE_PROJECT=$(CHOCOPY_POM_DIR)/chocopy.backend
RISCV_LANGUAGE_PROJECT=$(CHOCOPY_POM_DIR)/risc-v/RV32IM
RESULTS_DIR=$(DATA_DIR)/benchmark_results

BENCHMARK_ARGS?=-e failing -rff "$(RESULTS_DIR)/`date +%Y%m%d-%H%M%S` `echo $(TARGET) | tr -d '()|.'`.csv" $(TARGET)

# DERIVATIVES - DO NOT CHANGE
MAVEN_HOME?=$(HOME)/.m2
MAVEN_REPO=$(MAVEN_HOME)/repository

BENCHMARK_DIR=$(STRATEGO_DIR)/$(BENCHMARK_PROJECT)
BENCHMARK_JAR=$(BENCHMARK_DIR)/target/benchmarks.jar

STRATEGO_DIR=$(SPOOFAX_REPO_DIR)/stratego
STRATEGO_LANGUAGE=$(MAVEN_REPO)/org/metaborg/stratego.lang/$(METABORG_VERSION)/stratego.lang-$(SPOOFAX_VERSION).spoofax-language

CHOCOPY_ARTIFACT=chocopy.backend-$(CHOCOPY_VERSION).spoofax-language
CHOCOPY_METABORG_FILE=$(CHOCOPY_LANGUAGE_PROJECT)/metaborg.yaml
CHOCOPY_METABORG_TEMPLATE_FILE=$(CHOCOPY_LANGUAGE_PROJECT)/_metaborg.yaml
RISCV_METABORG_FILE=$(RISCV_LANGUAGE_PROJECT)/metaborg.yaml
RISCV_METABORG_TEMPLATE_FILE=$(RISCV_LANGUAGE_PROJECT)/_metaborg.yaml

CHOCOPY_MAVEN_FOLDER=$(MAVEN_REPO)/org/example/chocopy.backend/$(CHOCOPY_VERSION)
CHOCOPY_LANGUAGE=$(CHOCOPY_MAVEN_FOLDER)/$(CHOCOPY_ARTIFACT)

# Targets
.PHONY: all
all: run-benchmark

.PHONY: clean
clean:
	rm -rf $(BUILD_DIR)

.PHONY: benchmark-jar
benchmark-jar: $(BENCHMARK_JAR)

$(BENCHMARK_JAR):
	cd $(BENCHMARK_DIR) && \
	mvn -q package

.SILENT: $(RESULTS_DIR)
$(RESULTS_DIR):
	-mkdir -p $(RESULTS_DIR)

$(CHOCOPY_REPO_DIR):
	git clone --recursive --branch $(CHOCOPY_BRANCH) $(CHOCOPY_REPO_URL) $(CHOCOPY_REPO_DIR)

.PHONY: fetch-chocopy
pull-chocopy: $(CHOCOPY_REPO_DIR)
	cd $(CHOCOPY_REPO_DIR) && \
	git fetch -ap && \
	git checkout -f $(CHOCOPY_BRANCH) && \
	git pull

$(CHOCOPY_REPO): | pull-chocopy

$(CHOCOPY_METABORG_FILE): $(CHOCOPY_REPO)
$(RISCV_METABORG_FILE): $(CHOCOPY_REPO)

.INTERMEDIATE: %/_metaborg.yaml
%/_metaborg.yaml: %/metaborg.yaml
	mv $*/metaborg.yaml $*/_metaborg.yaml

$(CHOCOPY_LANGUAGE).O%: $(CHOCOPY_REPO) | $(CHOCOPY_METABORG_TEMPLATE_FILE) $(RISCV_METABORG_TEMPLATE_FILE)
	+yq e '.language.stratego.args += ["-O", $*]' $(CHOCOPY_METABORG_TEMPLATE_FILE) > $(CHOCOPY_METABORG_FILE)
	+yq e '.language.stratego.args += ["-O", $*]' $(RISCV_METABORG_TEMPLATE_FILE) > $(RISCV_METABORG_FILE)
	$(MAKE) $(CHOCOPY_LANGUAGE)
	mv $(CHOCOPY_LANGUAGE) $(CHOCOPY_LANGUAGE).O$*

$(CHOCOPY_LANGUAGE): $(CHOCOPY_REPO)
	cd $(CHOCOPY_POM_DIR) && \
	mvn -q clean install

.PHONY: chocopy-variants
chocopy-variants: $(CHOCOPY_LANGUAGE).O2 $(CHOCOPY_LANGUAGE).O3 $(CHOCOPY_LANGUAGE).O4


.PHONY: run-benchmark
run-benchmark: $(STRATEGO_LANGUAGE) chocopy-variants $(BENCHMARK_JAR) | $(RESULTS_DIR)
	cd $(BENCHMARK_DIR) && \
	java -jar $(BENCHMARK_JAR) $(BENCHMARK_ARGS)
	@-curl "http://xdroid.net/api/message?k=k-9f63903546c4&t=Benchmark%20finished&c=Benchmark%20finished%3A%0A`echo ${TARGET} | tr -d ' .'`"

.PHONY: prepare-benchmark
prepare-benchmark: $(STRATEGO_LANGUAGE) chocopy-variants $(BENCHMARK_JAR) | $(RESULTS_DIR)
	@-curl "http://xdroid.net/api/message?k=k-9f63903546c4&t=Benchmark%20prepared&c=Benchmark%20prepared"
