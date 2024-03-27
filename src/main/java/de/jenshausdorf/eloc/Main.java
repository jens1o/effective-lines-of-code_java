package de.jenshausdorf.eloc;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        FileAnalyzer analyzer = new FileAnalyzer(Paths.get("src/main/java/de/jenshausdorf/eloc/Main.java"));

        System.out.println(analyzer.getEffectiveLinesOfCode());
    }
}