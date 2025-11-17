package org.millerM907.landing.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JacocoCoveragePrinter — unit tests (given/when/then)")
class JacocoCoveragePrinterTest {

    private String captureOutput(String... args) throws IOException {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(outputStream, true, StandardCharsets.UTF_8)) {
            System.setOut(ps);
            JacocoCoveragePrinter.main(args);
        } finally {
            System.setOut(originalOut);
        }
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    @Nested
    @DisplayName("Given non-existing CSV path")
    class GivenMissingCsv {

        @Test
        @DisplayName("When main is called then prints not-found message")
        void givenMissingFile_whenMain_thenPrintsNotFound() throws IOException {
            String output = captureOutput("non-existing-file.csv");
            assertThat(output).contains("Coverage CSV not found");
        }
    }

    @Nested
    @DisplayName("Given empty CSV file with only header")
    class GivenHeaderOnly {

        @Test
        @DisplayName("When main is called then coverage is reported as zero")
        void givenHeaderOnly_whenMain_thenZeroCoverage() throws IOException {
            Path tempFile = Files.createTempFile("jacoco-header-only", ".csv");
            Files.writeString(tempFile,
                    "GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED\n",
                    StandardCharsets.UTF_8);

            String output = captureOutput(tempFile.toString());

            assertThat(output).contains("Instruction coverage: 0.00%");
        }
    }

    @Nested
    @DisplayName("Given CSV with missed and covered instructions")
    class GivenCsvWithData {

        @Test
        @DisplayName("When main is called then correct coverage percentage is printed")
        void givenData_whenMain_thenCalculatesCoverage() throws IOException {
            Path tempFile = Files.createTempFile("jacoco-data", ".csv");
            String content = String.join("\n",
                    "GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED",
                    "group,package,MyClass,10,90"
            );
            Files.writeString(tempFile, content + "\n", StandardCharsets.UTF_8);

            String output = captureOutput(tempFile.toString());

            assertThat(output).contains("Instruction coverage: 90.00%");
        }
    }
}