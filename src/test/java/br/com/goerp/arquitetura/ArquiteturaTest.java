package br.com.goerp.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.hibernate.annotations.TenantId;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import br.com.goerp.compartilhado.aplicacao.AcessoEntreEmpresas;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.usuario.aplicacao.autenticacao.Permissoes;
import br.com.goerp.usuario.dominio.Permissao;
import jakarta.persistence.Column;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /** Únicos endpoints sem permissão específica: login, renovação, logout e "quem sou eu". */
    private static final String CONTROLLER_PUBLICO = "br.com.goerp.usuario.web.AutenticacaoController";

    private static final Pattern AUTORIDADE = Pattern.compile("hasAuthority\\('([A-Z_]+)'\\)");

    @ArchTest
    static final ArchRule todoEndpointExigePermissao = methods()
            .that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
            .and().areDeclaredInClassesThat().doNotHaveFullyQualifiedName(CONTROLLER_PUBLICO)
            .and().arePublic()
            .and(saoEndpoints())
            .should().beAnnotatedWith(PreAuthorize.class)
            .because("todo endpoint de negócio exige uma permissão específica");

    @ArchTest
    static final ArchRule preAuthorizeSoUsaPermissoesExistentes = methods()
            .that().areAnnotatedWith(PreAuthorize.class)
            .should(usarSomentePermissoesExistentes());

    private static DescribedPredicate<JavaMethod> saoEndpoints() {
        return DescribedPredicate.describe("são endpoints", metodo -> metodo.getAnnotations().stream()
                .anyMatch(anotacao -> anotacao.getRawType().isAnnotatedWith(RequestMapping.class)));
    }

    private static ArchCondition<JavaMethod> usarSomentePermissoesExistentes() {
        Set<String> existentes = Arrays.stream(Permissao.values()).map(Enum::name).collect(Collectors.toSet());
        existentes.add(Permissoes.ADMINISTRAR_PLATAFORMA);
        return new ArchCondition<>("usar somente permissões existentes") {
            @Override
            public void check(JavaMethod metodo, ConditionEvents eventos) {
                String expressao = metodo.getAnnotationOfType(PreAuthorize.class).value();
                Matcher autoridades = AUTORIDADE.matcher(expressao);
                boolean citaAlguma = false;
                while (autoridades.find()) {
                    citaAlguma = true;
                    if (!existentes.contains(autoridades.group(1))) {
                        eventos.add(SimpleConditionEvent.violated(metodo,
                                metodo.getFullName() + " cita a permissão inexistente " + autoridades.group(1)));
                    }
                }
                if (!citaAlguma) {
                    eventos.add(SimpleConditionEvent.violated(metodo,
                            metodo.getFullName() + " não usa hasAuthority('...'): " + expressao));
                }
            }
        };
    }
}
