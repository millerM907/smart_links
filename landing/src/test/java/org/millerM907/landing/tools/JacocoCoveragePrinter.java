package org.millerM907.landing.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Small helper used from Maven to print Jacoco instruction coverage to the console.
 */
public final class JacocoCoveragePrinter {

    private JacocoCoveragePrinter() {
    }

    public static void main(String[] args) throws IOException {
        String csvPathArg = args.length > 0
                ? args[0]
                : "target/site/jacoco/jacoco.csv";

        Path coverageCsvPath = Path.of(csvPathArg);
        if (!Files.exists(coverageCsvPath)) {
            System.out.println("[JaCoCo] Coverage CSV not found: " + coverageCsvPath);
            return;
        }

        long missedInstructions = 0;
        long coveredInstructions = 0;

        try (BufferedReader reader = Files.newBufferedReader(coverageCsvPath, StandardCharsets.UTF_8)) {
            String csvLine;
            boolean headerLine = true;
            while ((csvLine = reader.readLine()) != null) {
                if (headerLine) {
                    headerLine = false;
                    continue;
                }
                if (csvLine.isBlank()) continue;

                String[] columns = csvLine.split(",");
                if (columns.length < 5) continue;

                long missedForRow = Long.parseLong(columns[3].trim());
                long coveredForRow = Long.parseLong(columns[4].trim());

                missedInstructions += missedForRow;
                coveredInstructions += coveredForRow;
            }
        }

        long totalInstructions = missedInstructions + coveredInstructions;
        if (totalInstructions == 0) {
            System.out.println("[JaCoCo] Instruction coverage: 0.00% (no instructions)");
            return;
        }

        double coveragePercent = (coveredInstructions * 100.0) / totalInstructions;
        String message = String.format(
                Locale.ROOT,
                "[JaCoCo] Instruction coverage: %.2f%%%n",
                coveragePercent
        );
        System.out.print(message);
    }
}
