STRATEGO_REPO?=https://github.com/toinehartman/stratego.git
STRATEGO_VERSION?=benchmarks
STRATEGO_PROJECT?=stratego.lang
BENCHMARK_PROJECT?=stratego.build.spoofax2.benchmark

DATA_DIR?=./data/
BUILD_DIR?=./build/

REPO_DIR=$(BUILD_DIR)/stratego
STRATEGO_DIR=$(REPO_DIR)/$(STRATEGO_PROJECT)
BENCHMARK_DIR=$(REPO_DIR)/$(BENCHMARK_PROJECT)

all: build-stratego build-benchmark

pull-stratego:
	if [ ! -d $(REPO_DIR)/.git ]; then git clone $(STRATEGO_REPO) $(REPO_DIR); fi
	cd $(REPO_DIR) && \
	git checkout $(STRATEGO_VERSION) && \
	git pull --rebase

maven-opts:
	export MAVEN_OPTS="-Xms512m -Xmx1024m -Xss16m"

build-stratego: maven-opts pull-stratego
	cd $(STRATEGO_DIR) && \
	mvn install

build-benchmark: maven-opts pull-stratego
	cd $(BENCHMARK_DIR) && \
	mvn package

run-benchmark: build-benchmark build-stratego
	cd $(BENCHMARK_DIR) && \
	java -jar target/benchmarks.jar -f 2 -e failing -rff $(DATA_DIR)/benchmark_results/`date +%Y%m%d`.csv
