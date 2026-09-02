# TraceFinder

## Overview

TraceFinder is a Java command-line tool for analyzing access logs. It reads a log file together with a CSV rulebook and produces a report showing normal activity, suspicious activity, unknown patterns, and malformed log entries.

I built the project using Java and Maven, with JUnit tests to verify the main parts of the application.

## How It Works

The application takes three arguments:

text
logs.txt rules.csv report.txt


* `logs.txt` contains the access log entries.
* `rules.csv` contains the activity levels and their severity scores.
* `report.txt` is the generated analysis report.

Each valid log entry follows this format:

text
timestamp | level | source IP | target | action


The rulebook uses:

text
level,severity_score


For example:

text
INFO,1
WARN,3
ERROR,5
ALERT,9


Entries with a severity score of 3 or higher are flagged. Unknown activity levels are recorded separately, while malformed lines are reported with their line numbers.

## Report

The generated report contains five sections:

1. Activity Summary
2. Flagged Entries
3. Suspicious Activity by IP
4. Unknown Patterns
5. Malformed Lines

Suspicious IP activity includes all valid entries belonging to an IP address that has at least one flagged entry.

## Running the Project

To build the project:

bash
mvn package


To run the application:

bash
java -cp target/classes Main logs.txt rules.csv report.txt


To run the tests:

bash
mvn test


## Testing

The project includes JUnit tests for the parser, rulebook, rulebook reader, log reader, analyzer, report writer, and command-line argument validation.

At the current stage, all 16 tests pass successfully.

## Project Structure

The application is separated into different classes so that parsing, reading files, analysis, and report generation are handled independently.

   text
src/main/java/
    Main.java
    Parser.java
    LogReader.java
    RulebookReader.java
    Analyzer.java
    ReportWriter.java
    LogEntry.java
    MalformedLine.java
    ParseResult.java
    LogReadResult.java
    Rule.java
    Rulebook.java
    AnalysisResult.java

src/test/java/
    JUnit tests


## Technologies Used

* Java 17
* Maven
* JUnit 5
* Git
* Linux
