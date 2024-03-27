package de.jenshausdorf.eloc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.SimpleName;

/**
 * Counts the effective lines of code (eloc) for a specific file
 */
public class FileAnalyzer {
    private Path path;

    public FileAnalyzer(Path path) {
        this.path = path;
    }

    /**
     * Calculates the eloc for the given file.
     * We assume that the file is formatted so it doesn't take up any unnecessary
     * blank lines/space.
     *
     * Effective lines of code require programmers to actually implement something.
     * Hence, interfaces don't count as well as enums when there are no implemented
     * methods.
     * In case there is no effective type declared (e.g. plain interface) the whole
     * file has 0 lines of effective code.
     *
     * @return eloc
     */
    public int getEffectiveLinesOfCode() throws IOException {
        int eloc = 0;

        CompilationUnit cu = StaticJavaParser.parse(Files.newInputStream(this.path));

        if (cu.getPackageDeclaration().isPresent()) {
            eloc++;
        }

        if (cu.getImports() != null) {
            eloc += this.countImports(cu.getImports());
        }

        boolean foundEffectiveType = false;

        for (TypeDeclaration<?> typeDeclaration : cu.getTypes()) {
            int effectiveLinesInsideType = this.countEffectiveLinesInType(typeDeclaration);

            if (effectiveLinesInsideType != 0) {
                foundEffectiveType = true;
                eloc += effectiveLinesInsideType;
            }
        }

        return foundEffectiveType ? eloc : 0;
    }

    private int countEffectiveLinesInType(TypeDeclaration<?> typeDeclaration) {
        int eloc = 0;

        if (typeDeclaration.isClassOrInterfaceDeclaration()) {
            eloc += this.countEffectiveLinesInClassOrInterface(typeDeclaration.asClassOrInterfaceDeclaration());
        }

        if (typeDeclaration.isEnumDeclaration()) {
            eloc += this.countEffectiveLinesInEnum(typeDeclaration.asEnumDeclaration());
        }

        return eloc;
    }

    private int countEffectiveLinesInEnum(EnumDeclaration enumDeclaration) {
        // filter out enums that only have declarations (or comments)
        boolean hasEffectiveCode = enumDeclaration.getChildNodes().stream().anyMatch(x -> {
            return !(x instanceof Modifier) && !(x instanceof SimpleName) && !(x instanceof EnumConstantDeclaration)
                    && !(x instanceof Comment);
        });

        if (!hasEffectiveCode) {
            return 0;
        }

        int eloc = 0;

        List<Node> childNodes = enumDeclaration.getChildNodes();

        eloc += this.getLinesOfCodeBetweenToNodes(Optional.ofNullable(childNodes.get(0)),
                Optional.ofNullable(enumDeclaration.getChildNodes().get(childNodes.size() - 1)));

        for (Comment commentNode : enumDeclaration.getAllContainedComments()) {
            eloc -= this.getLinesSpanned(commentNode);
        }

        return eloc; // +1 to count the last line with the closing bracket as well
    }

    private int countEffectiveLinesInClassOrInterface(ClassOrInterfaceDeclaration dec) {
        // interfaces shall not count
        if (dec.isInterface()) {
            return 0;
        }

        int eloc = 0;

        Optional<BodyDeclaration<?>> first = dec.getMembers().getFirst();
        Optional<BodyDeclaration<?>> last = dec.getMembers().getLast();

        eloc += this.getLinesOfCodeBetweenToNodes(first, last);

        for (Comment commentNode : dec.getAllContainedComments()) {
            eloc -= this.getLinesSpanned(commentNode);
        }

        return eloc + 2; // +2 to count the lines with the brackets as well
    }

    private int countImports(NodeList<ImportDeclaration> imports) {
        return imports.size();
    }

    private int getLinesOfCodeBetweenToNodes(Optional<? extends Node> first, Optional<? extends Node> second) {
        if (first.isPresent() && second.isPresent()) {
            // + 1 to count the first line inclusively
            return 1 + (second.get().getEnd().get().line - first.get().getBegin().get().line);
        }

        return 0;
    }

    private int getLinesSpanned(Node node) {
        return 1 + (node.getEnd().get().line - node.getBegin().get().line);
    }
}
