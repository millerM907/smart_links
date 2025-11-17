package org.millerM907.edge.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JacocoCoveragePrinter {

    private JacocoCoveragePrinter() {
    }

    public static void main(String[] args) throws IOException {
        String path = args.length > 0
                ? args[0]
                : "target/site/jacoco/jacoco.csv";

        Path csv = Path.of(path);
        if (!Files.exists(csv)) {
            System.out.println("[JaCoCo] Coverage CSV not found: " + csv);
            return;
        }

        long instructionsMissed = 0;
        long instructionsCovered = 0;

        try (BufferedReader reader = Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) {
                    first = false; // пропускаем заголовок
                    continue;
                }
                if (line.isBlank()) continue;

                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                long missed = Long.parseLong(parts[3].trim());
                long covered = Long.parseLong(parts[4].trim());

                instructionsMissed += missed;
                instructionsCovered += covered;
            }
        }

        long total = instructionsMissed + instructionsCovered;
        if (total == 0) {
            System.out.println("[JaCoCo] Instruction coverage: 0.00% (no instructions)");
            return;
        }

        double percent = (instructionsCovered * 100.0) / total;
        System.out.printf("[JaCoCo] Instruction coverage: %.2f%%%n", percent);
    }
}

