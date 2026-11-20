package br.com.goerp.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Regras da Clean Architecture verificadas a cada build. */
@AnalyzeClasses(packages = "br.com.goerp", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    @ArchTest
    static final ArchRule camadas = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Dominio").definedBy("..dominio..")
            .layer("Aplicacao").definedBy("..aplicacao..")
            .layer("Infraestrutura").definedBy("..infraestrutura..")
            .layer("Web").definedBy("..web..")
            .whereLayer("Web").mayNotBeAccessedByAnyLayer()
            .whereLayer("Infraestrutura").mayNotBeAccessedByAnyLayer()
            .whereLayer("Aplicacao").mayOnlyBeAccessedByLayers("Web", "Infraestrutura")
            .whereLayer("Dominio").mayOnlyBeAccessedByLayers("Aplicacao", "Web", "Infraestrutura");

    @ArchTest
    static final ArchRule dominioNaoDependeDeFramework = classes()
            .that().resideInAPackage("..dominio..")
            .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "..dominio..")
            .because("o domínio deve ser Java puro, sem Spring, JPA ou outras bibliotecas");

    @ArchTest
    static final ArchRule aplicacaoNaoDependeDeFramework = classes()
            .that().resideInAPackage("..aplicacao..")
            .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "..dominio..", "..aplicacao..")
            .because("os casos de uso não devem conhecer Spring, JPA ou HTTP");

    @ArchTest
    static final ArchRule webNaoUsaRepositorios = noClasses()
            .that().resideInAPackage("..web..")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repositorio")
            .because("controllers falam com casos de uso, nunca direto com repositórios");
}
