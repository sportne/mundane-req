.PHONY: test native-smoke verify

BUILD_ROOT := build/maintained
CLASS_DIR := $(BUILD_ROOT)/classes
NATIVE_SMOKE := $(BUILD_ROOT)/native-smoke
MAIN_SOURCES := $(shell find src/main/java -type f -name '*.java' -print | LC_ALL=C sort)
TEST_SOURCES := $(shell find src/test/java -type f -name '*.java' -print | LC_ALL=C sort)

test:
	mkdir -p $(CLASS_DIR)
	javac --release 21 -Xlint:all -Werror -d $(CLASS_DIR) $(MAIN_SOURCES) $(TEST_SOURCES)
	java -ea -cp $(CLASS_DIR) mundanereq.test.MaintainedTestSuite

native-smoke: test
	native-image -O0 --no-fallback -cp $(CLASS_DIR) -o $(abspath $(NATIVE_SMOKE)) mundanereq.smoke.MaintainedBuildTest
	$(NATIVE_SMOKE)

verify: test native-smoke
