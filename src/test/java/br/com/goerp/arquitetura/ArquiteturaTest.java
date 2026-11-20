package br.com.goerp.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import java.util.Set;

import org.hibernate.annotations.TenantId;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import br.com.goerp.compartilhado.aplicacao.AcessoEntreEmpresas;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import jakarta.persistence.Column;

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

    /**
     * Tabelas que guardam empresa_id sem serem dados DA empresa: são registros do próprio sistema
     * sobre o que aconteceu em cada empresa (auditoria e segurança). Incluir aqui exige justificativa.
     */
    private static final Set<String> SEM_ISOLAMENTO_POR_EMPRESA = Set.of(
            "br.com.goerp.compartilhado.infraestrutura.auditoria.Revisao",
            "br.com.goerp.usuario.infraestrutura.persistencia.SessaoDeAcessoJpa",
            "br.com.goerp.usuario.infraestrutura.persistencia.EventoDeSegurancaJpa");

    @ArchTest
    static final ArchRule todaColunaEmpresaIdTemTenantId = fields()
            .that(mapeiamAColunaEmpresaId())
            .and().areDeclaredInClassesThat(DescribedPredicate.not(
                    DescribedPredicate.describe("sem isolamento declarado",
                            classe -> SEM_ISOLAMENTO_POR_EMPRESA.contains(classe.getName()))))
            .should().beAnnotatedWith(TenantId.class)
            .because("dados de empresa precisam do filtro automático do Hibernate (multiempresa)");

    @ArchTest
    static final ArchRule acessoEntreEmpresasSoEmCasosDeUso = classes()
            .that().areAnnotatedWith(AcessoEntreEmpresas.class)
            .should().beAnnotatedWith(CasoDeUso.class)
            .because("o escopo de todas as empresas só é aberto na entrada de um caso de uso");

    private static DescribedPredicate<JavaField> mapeiamAColunaEmpresaId() {
        return DescribedPredicate.describe("mapeiam a coluna empresa_id", campo ->
                campo.tryGetAnnotationOfType(Column.class)
                        .map(coluna -> "empresa_id".equals(coluna.name()))
                        .orElse(false));
    }
}
