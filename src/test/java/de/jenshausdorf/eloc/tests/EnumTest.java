package de.jenshausdorf.eloc.tests;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.file.Paths;

import de.jenshausdorf.eloc.FileAnalyzer;
import org.junit.jupiter.api.Test;

public class EnumTest {
    @Test
    void countTestEnumFile() throws IOException {
        FileAnalyzer analyzer = new FileAnalyzer(
                Paths.get("src/test/java/de/jenshausdorf/eloc/fixtures/TestEnum.java"));

        assertSame(0, analyzer.getEffectiveLinesOfCode());
    }
}
