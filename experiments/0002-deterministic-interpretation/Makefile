.PHONY: test native experiment

MAIN_SOURCE := src/mundanereq/Probe.java
TEST_SOURCE := test/mundanereq/ProbeTest.java
CLASS_DIR := build/classes
TEST_CLASS_DIR := build/test-classes
NATIVE_EXECUTABLE := build/mundanereq

test:
	mkdir -p $(TEST_CLASS_DIR)
	javac -Xlint:all -Werror -d $(TEST_CLASS_DIR) $(MAIN_SOURCE) $(TEST_SOURCE)
	java -ea -cp $(TEST_CLASS_DIR) mundanereq.ProbeTest

native:
	mkdir -p $(CLASS_DIR)
	javac -Xlint:all -Werror -d $(CLASS_DIR) $(MAIN_SOURCE)
	native-image -O0 --no-fallback -cp $(CLASS_DIR) -o $(abspath $(NATIVE_EXECUTABLE)) mundanereq.Probe

experiment: test native
	$(NATIVE_EXECUTABLE) ../0001-source-representations/candidate-a-modules
	$(NATIVE_EXECUTABLE) ../0001-source-representations/candidate-b-one-per-file/requirements
