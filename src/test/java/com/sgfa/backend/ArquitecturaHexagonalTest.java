package com.sgfa.backend;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArquitecturaHexagonalTest {

    private static JavaClasses clases;

    @BeforeAll
    static void importarClases() {
        clases = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.sgfa.backend");
    }

    @Test
    void el_dominio_no_depende_de_aplicacion_ni_infraestructura() {
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..application..", "..infrastructure..")
                .check(clases);
    }

    @Test
    void el_dominio_no_depende_de_frameworks() {
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "jakarta.persistence..", "io.jsonwebtoken..")
                .check(clases);
    }

    @Test
    void la_aplicacion_no_depende_de_infraestructura() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(clases);
    }

    @Test
    void la_aplicacion_no_depende_de_frameworks() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "jakarta.persistence..", "io.jsonwebtoken..")
                .check(clases);
    }
}