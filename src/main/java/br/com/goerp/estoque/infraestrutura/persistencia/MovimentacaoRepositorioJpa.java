package br.com.goerp.estoque.infraestrutura.persistencia;

import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.infraestrutura.persistencia.Paginas;
import br.com.goerp.estoque.dominio.Movimentacao;
import br.com.goerp.estoque.dominio.MovimentacaoRepositorio;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class MovimentacaoRepositorioJpa implements MovimentacaoRepositorio {

    private static final Sort MAIS_RECENTES_PRIMEIRO = Sort.by(Sort.Direction.DESC, "id");

    private final MovimentacaoJpaRepository jpa;

    @Override
    public Movimentacao registrar(Movimentacao movimentacao) {
        if (movimentacao.id() != null) {
            throw new IllegalArgumentException("Movimentação já registrada não pode ser alterada");
        }
        var registro = new MovimentacaoJpa();
        registro.setEmpresaId(movimentacao.empresaId());
        registro.setProdutoId(movimentacao.produtoId());
        registro.setTipo(movimentacao.tipo());
        registro.setQuantidade(movimentacao.quantidade().valor());
        registro.setCustoUnitario(movimentacao.custoUnitario().valor());
        registro.setSaldoResultante(movimentacao.saldoResultante().valor());
        registro.setCustoMedioResultante(movimentacao.custoMedioResultante().valor());
        registro.setOrigem(movimentacao.origem());
        registro.setDocumentoDeOrigem(movimentacao.documentoDeOrigem());
        registro.setMotivo(movimentacao.motivo());
        registro.setEstornada(movimentacao.estornada());
        registro.setUsuarioId(movimentacao.usuarioId());
        registro.setMomento(movimentacao.momento());
        return paraDominio(jpa.saveAndFlush(registro));
    }

    @Override
    public Optional<Movimentacao> buscarPorId(Long id) {
        return jpa.findById(id).map(MovimentacaoRepositorioJpa::paraDominio);
    }

    @Override
    public boolean foiEstornada(Long id) {
        return jpa.existsByEstornada(id);
    }

    @Override
    public Pagina<Movimentacao> extrato(Long produtoId, Paginacao paginacao) {
        return Paginas.daSpring(jpa.findByProdutoId(produtoId, Paginas.paraSpring(paginacao, MAIS_RECENTES_PRIMEIRO)),
                MovimentacaoRepositorioJpa::paraDominio);
    }

    static Movimentacao paraDominio(MovimentacaoJpa registro) {
        return new Movimentacao(registro.getId(), registro.getEmpresaId(), registro.getProdutoId(), registro.getTipo(),
                new Quantidade(registro.getQuantidade()), new CustoUnitario(registro.getCustoUnitario()),
                new Quantidade(registro.getSaldoResultante()),
                new CustoUnitario(registro.getCustoMedioResultante()), registro.getOrigem(),
                registro.getDocumentoDeOrigem(), registro.getMotivo(), registro.getEstornada(), registro.getUsuarioId(),
                registro.getMomento());
    }
}
