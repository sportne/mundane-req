.PHONY: test native

PARSER_SOURCE := ../0002-deterministic-interpretation/src/mundanereq/Probe.java
MAIN_SOURCE := src/mundanereq/ReqifProbe.java
TEST_SOURCE := test/mundanereq/ReqifProbeTest.java
CLASS_DIR := build/classes
TEST_CLASS_DIR := build/test-classes
NATIVE_EXECUTABLE := build/reqifprobe

test:
	mkdir -p $(TEST_CLASS_DIR)
	javac -Xlint:all -Werror -d $(TEST_CLASS_DIR) $(PARSER_SOURCE) $(MAIN_SOURCE) $(TEST_SOURCE)
	java -ea -cp $(TEST_CLASS_DIR) mundanereq.ReqifProbeTest

native:
	mkdir -p $(CLASS_DIR)
	javac -Xlint:all -Werror -d $(CLASS_DIR) $(PARSER_SOURCE) $(MAIN_SOURCE)
	native-image -O0 --no-fallback -cp $(CLASS_DIR) -o $(abspath $(NATIVE_EXECUTABLE)) mundanereq.ReqifProbe
