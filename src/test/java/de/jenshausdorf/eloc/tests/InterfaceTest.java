package de.jenshausdorf.eloc.tests;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import de.jenshausdorf.eloc.FileAnalyzer;

public class InterfaceTest {
    @Test
    void countTestInterfaceFile() throws IOException {
        FileAnalyzer analyzer = new FileAnalyzer(
                Paths.get("src/test/java/de/jenshausdorf/eloc/fixtures/TestInterface.java"));

        assertSame(0, analyzer.getEffectiveLinesOfCode());
    }
}
