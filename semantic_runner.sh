#!/usr/bin/env bash
# .\semantic_runner.sh ./target/Javana-1.jar ./src/main/java/edu/yu/compilers/jvPrograms/examplePrograms/TestAssignmentStatements.jv
JAR_PATH=$1
TEST_PATH=$2

mvn clean compile install test-compile -DskipTests

java -jar $JAR_PATH -symbols $TEST_PATH >./exampleProgramsOutput.txt 2>&1
