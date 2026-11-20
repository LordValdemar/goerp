package br.com.goerp.compartilhado.infraestrutura.multiempresa;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Permite trocar, por um trecho de código, a empresa usada para filtrar os dados.
 * Fora desses trechos vale a empresa do usuário autenticado.
 */
public final class ContextoDeEmpresa {

    /** Valor especial: enxerga todas as empresas. */
    static final Long TODAS_AS_EMPRESAS = 0L;
    /** Valor especial: não enxerga nenhuma empresa (ninguém autenticado em uma empresa). */
    static final Long NENHUMA_EMPRESA = -1L;

    private static final ThreadLocal<Long> ESCOPO = new ThreadLocal<>();

    private ContextoDeEmpresa() {
    }

    public static <T> T emTodasAsEmpresas(Supplier<T> acao) {
        return executarCom(TODAS_AS_EMPRESAS, acao);
    }

    public static void emTodasAsEmpresas(Runnable acao) {
        emTodasAsEmpresas(() -> {
            acao.run();
            return null;
        });
    }

    static Optional<Long> escopoAtual() {
        return Optional.ofNullable(ESCOPO.get());
    }

    private static <T> T executarCom(Long empresa, Supplier<T> acao) {
        Long anterior = ESCOPO.get();
        ESCOPO.set(empresa);
        try {
            return acao.get();
        } finally {
            restaurar(anterior);
        }
    }

    private static void restaurar(Long anterior) {
        if (anterior == null) {
            ESCOPO.remove();
        } else {
            ESCOPO.set(anterior);
        }
    }
}
