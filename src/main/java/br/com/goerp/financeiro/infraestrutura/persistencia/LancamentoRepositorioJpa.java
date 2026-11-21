package br.com.goerp.financeiro.infraestrutura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.infraestrutura.persistencia.ContadorSequencial;
import br.com.goerp.compartilhado.infraestrutura.persistencia.Paginas;
import br.com.goerp.financeiro.dominio.FiltroDeExtrato;
import br.com.goerp.financeiro.dominio.Lancamento;
import br.com.goerp.financeiro.dominio.LancamentoRepositorio;
import br.com.goerp.financeiro.dominio.OrigemDoLancamento;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class LancamentoRepositorioJpa implements LancamentoRepositorio {

    private static final Sort MAIS_RECENTES_PRIMEIRO = Sort.by(Sort.Direction.DESC, "id");

    private final LancamentoJpaRepository jpa;
    private final ContadorSequencial contador;

    @Override
    public Lancamento registrar(Lancamento lancamento) {
        if (lancamento.id() != null) {
            throw new IllegalArgumentException("Lançamento já registrado não pode ser alterado");
        }
        var registro = new LancamentoJpa();
        registro.setEmpresaId(lancamento.empresaId());
        registro.setContaId(lancamento.contaId());
        registro.setTipo(lancamento.tipo());
        registro.setValor(lancamento.valor().valor());
        registro.setSaldoResultante(lancamento.saldoResultante().valor());
        registro.setForma(lancamento.forma());
        registro.setCategoriaId(lancamento.categoriaId());
        registro.setDescricao(lancamento.descricao());
        registro.setOrigem(lancamento.origem());
        registro.setDocumentoDeOrigem(lancamento.documentoDeOrigem());
        registro.setEstornado(lancamento.estornado());
        registro.setUsuarioId(lancamento.usuarioId());
        registro.setMomento(lancamento.momento());
        return paraDominio(jpa.saveAndFlush(registro));
    }

    @Override
    public Optional<Lancamento> buscarPorId(Long id) {
        return jpa.findById(id).map(LancamentoRepositorioJpa::paraDominio);
    }

    @Override
    public boolean foiEstornado(Long id) {
        return jpa.existsByEstornado(id);
    }

    @Override
    public Pagina<Lancamento> extrato(Long contaId, FiltroDeExtrato filtro, Paginacao paginacao) {
        Specification<LancamentoJpa> daConta = (l, q, cb) -> cb.equal(l.get("contaId"), contaId);
        Specification<LancamentoJpa> daForma = (l, q, cb) -> filtro.forma() == null ? null
                : cb.equal(l.get("forma"), filtro.forma());
        Specification<LancamentoJpa> aPartirDe = (l, q, cb) -> filtro.de() == null ? null
                : cb.greaterThanOrEqualTo(l.get("momento"), filtro.de());
        Specification<LancamentoJpa> antesDe = (l, q, cb) -> filtro.ate() == null ? null
                : cb.lessThan(l.get("momento"), filtro.ate());
        return Paginas.daSpring(jpa.findAll(Specification.allOf(daConta, daForma, aPartirDe, antesDe),
                Paginas.paraSpring(paginacao, MAIS_RECENTES_PRIMEIRO)), LancamentoRepositorioJpa::paraDominio);
    }

    @Override
    public List<Lancamento> doDocumento(OrigemDoLancamento origem, Long documentoDeOrigem) {
        return jpa.findByOrigemAndDocumentoDeOrigemAndEstornadoIsNullOrderById(origem, documentoDeOrigem).stream()
                .map(LancamentoRepositorioJpa::paraDominio)
                .toList();
    }

    @Override
    public long proximoNumeroDeTransferencia(Long empresaId) {
        return contador.proximo(ContadorSequencial.Serie.TRANSFERENCIA_FINANCEIRA, empresaId);
    }

    private static Lancamento paraDominio(LancamentoJpa registro) {
        return new Lancamento(registro.getId(), registro.getEmpresaId(), registro.getContaId(), registro.getTipo(),
                Dinheiro.de(registro.getValor()), Dinheiro.de(registro.getSaldoResultante()), registro.getForma(),
                registro.getCategoriaId(), registro.getDescricao(), registro.getOrigem(),
                registro.getDocumentoDeOrigem(), registro.getEstornado(), registro.getUsuarioId(),
                registro.getMomento());
    }
}
