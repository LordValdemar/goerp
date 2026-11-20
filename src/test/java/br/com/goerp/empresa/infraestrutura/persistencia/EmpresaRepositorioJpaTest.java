package br.com.goerp.empresa.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.goerp.compartilhado.dominio.Cnpj;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.empresa.dominio.EmpresaRepositorio;
import br.com.goerp.suporte.TesteDeIntegracao;

class EmpresaRepositorioJpaTest extends TesteDeIntegracao {

    @Autowired
    EmpresaRepositorio repositorio;

    @Test
    void salvaEBuscaEmpresa() {
        Empresa salva = repositorio.salvar(Empresa.nova(Cnpj.de("11222333000181"), "Loja Exemplo LTDA", "Loja"));

        Empresa encontrada = repositorio.buscarPorId(salva.id()).orElseThrow();

        assertThat(encontrada.razaoSocial()).isEqualTo("Loja Exemplo LTDA");
        assertThat(encontrada.cnpj()).isEqualTo(Cnpj.de("11222333000181"));
        assertThat(repositorio.existeComCnpj(Cnpj.de("11222333000181"))).isTrue();
    }

    @Test
    void preencheDataDeCriacaoEAlteracao() {
        Empresa salva = repositorio.salvar(Empresa.nova(Cnpj.de("11222333000181"), "Loja Exemplo LTDA", null));

        Integer semDatas = jdbc.queryForObject(
                "SELECT count(*) FROM empresa WHERE id = ? AND (criado_em IS NULL OR atualizado_em IS NULL)",
                Integer.class, salva.id());

        assertThat(semDatas).isZero();
    }

    @Test
    void cadaAlteracaoGeraUmaRevisaoNaAuditoria() {
        Empresa empresa = repositorio.salvar(Empresa.nova(Cnpj.de("11222333000181"), "Nome Antigo LTDA", null));
        empresa.alterarDados("Nome Novo LTDA", null);
        repositorio.salvar(empresa);
        empresa.inativar();
        repositorio.salvar(empresa);

        var historico = jdbc.queryForList("""
                SELECT a.razao_social, a.ativa, a.revtype, r.momento
                FROM empresa_aud a JOIN revisao r ON r.id = a.rev
                WHERE a.id = ? ORDER BY a.rev
                """, empresa.id());

        assertThat(historico).hasSize(3);
        assertThat(historico.get(0)).containsEntry("razao_social", "Nome Antigo LTDA").containsEntry("revtype", 0);
        assertThat(historico.get(1)).containsEntry("razao_social", "Nome Novo LTDA").containsEntry("revtype", 1);
        assertThat(historico.get(2)).containsEntry("ativa", false);
        assertThat(historico).allSatisfy(linha -> assertThat(linha.get("momento")).isNotNull());
    }
}
