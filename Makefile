.PHONY: test native-smoke native-validator validator-verify native-boundaries boundary-isolation verify

BUILD_ROOT := build/maintained
CLASS_DIR := $(BUILD_ROOT)/classes
NATIVE_SMOKE := $(BUILD_ROOT)/native-smoke
VALIDATE_NATIVE := $(BUILD_ROOT)/mundanereq-validate
FORMAT_NATIVE := $(BUILD_ROOT)/mundanereq-format
TRACE_NATIVE := $(BUILD_ROOT)/mundanereq-trace
MAIN_SOURCES := $(shell find src/main/java -type f -name '*.java' -print | LC_ALL=C sort)
TEST_SOURCES := $(shell find src/test/java -type f -name '*.java' -print | LC_ALL=C sort)

test:
	mkdir -p $(CLASS_DIR)
	javac --release 21 -Xlint:all -Werror -d $(CLASS_DIR) $(MAIN_SOURCES) $(TEST_SOURCES)
	java -ea -cp $(CLASS_DIR) mundanereq.test.MaintainedTestSuite

native-smoke: test
	native-image -O0 --no-fallback -cp $(CLASS_DIR) -o $(abspath $(NATIVE_SMOKE)) mundanereq.smoke.MaintainedBuildTest
	$(NATIVE_SMOKE)

native-validator: test
	native-image -O0 --no-fallback -cp $(CLASS_DIR) -o $(abspath $(VALIDATE_NATIVE)) mundanereq.cli.ValidatorMain
	$(VALIDATE_NATIVE) --version

validator-verify: native-validator
	java -ea -cp $(CLASS_DIR) mundanereq.cli.ValidatorVerificationTest $(VALIDATE_NATIVE)

native-boundaries: native-validator
	native-image -O0 --no-fallback -cp $(CLASS_DIR) -o $(abspath $(FORMAT_NATIVE)) mundanereq.cli.FormatBoundary
	native-image -O0 --no-fallback -cp $(CLASS_DIR) -o $(abspath $(TRACE_NATIVE)) mundanereq.cli.TraceBoundary
	$(FORMAT_NATIVE) --boundary-smoke
	$(TRACE_NATIVE) --boundary-smoke

boundary-isolation: native-boundaries
	java -ea -cp $(CLASS_DIR) mundanereq.boundary.NativeBoundaryIsolationTest \
		$(VALIDATE_NATIVE) $(FORMAT_NATIVE) $(TRACE_NATIVE)

verify: test native-smoke boundary-isolation validator-verify
