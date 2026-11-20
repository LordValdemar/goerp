package br.com.goerp.compartilhado.infraestrutura.multiempresa;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

import br.com.goerp.compartilhado.infraestrutura.seguranca.UsuarioAutenticado;

/**
 * Diz ao Hibernate qual empresa usar para filtrar as entidades com {@code @TenantId}.
 *
 * <p>Falha fechada: sem empresa no token (ninguém logado, ou o administrador da plataforma),
 * nenhum dado de empresa é visível. Ver todas as empresas exige um escopo explícito
 * ({@link ContextoDeEmpresa#emTodasAsEmpresas}).
 */
public class ResolvedorDeEmpresa implements CurrentTenantIdentifierResolver<Long> {

    @Override
    public Long resolveCurrentTenantIdentifier() {
        return ContextoDeEmpresa.escopoAtual()
                .or(UsuarioAutenticado::idEmpresa)
                .orElse(ContextoDeEmpresa.NENHUMA_EMPRESA);
    }

    @Override
    public boolean isRoot(Long empresa) {
        return ContextoDeEmpresa.TODAS_AS_EMPRESAS.equals(empresa);
    }

    /** Cada transação abre sua sessão; não exigir que a empresa seja a mesma de uma sessão anterior. */
    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}
