package br.com.goerp.financeiro.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.dominio.excecao.ConflitoDeEdicaoException;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.compartilhado.infraestrutura.persistencia.Paginas;
import br.com.goerp.financeiro.dominio.Baixa;
import br.com.goerp.financeiro.dominio.DadosDoTitulo;
import br.com.goerp.financeiro.dominio.FiltroDeTitulos;
import br.com.goerp.financeiro.dominio.NaturezaDoTitulo;
import br.com.goerp.financeiro.dominio.OrigemDoTitulo;
import br.com.goerp.financeiro.dominio.Parcela;
import br.com.goerp.financeiro.dominio.SituacaoDoTitulo;
import br.com.goerp.financeiro.dominio.Titulo;
import br.com.goerp.financeiro.dominio.TituloRepositorio;
import br.com.goerp.financeiro.dominio.TotalDeTitulos;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class TituloRepositorioJpa implements TituloRepositorio {

    private static final Sort POR_VENCIMENTO = Sort.by("vencimento", "id");

    private final TituloJpaRepository jpa;
    private final BaixaJpaRepository baixas;
    private final EntityManager entityManager;

    @Override
    public Titulo salvar(Titulo titulo) {
        TituloJpa registro = titulo.id() == null ? novoRegistro(titulo) : registroNaVersao(titulo);
        registro.setDescricao(titulo.descricao());
        registro.setCategoriaId(titulo.categoriaId());
        registro.setDocumento(titulo.documento());
        registro.setValor(titulo.valor().valor());
        registro.setSaldoDevedor(titulo.saldoDevedor().valor());
        registro.setVencimento(titulo.vencimento());
        registro.setSituacao(titulo.situacao());
        registro.setMotivoDoCancelamento(titulo.motivoDoCancelamento());
        registro.setCanceladoEm(titulo.canceladoEm());
        TituloJpa salvo = jpa.saveAndFlush(registro);
        salvarBaixas(salvo, titulo.baixas());
        return paraDominio(salvo, baixas.findByTituloIdOrderByNumero(salvo.getId()));
    }

    @Override
    public Optional<Titulo> buscarPorId(Long id) {
        return jpa.findById(id).map(this::comBaixas);
    }

    /**
     * Relê com SELECT ... FOR UPDATE mesmo se o título já foi lido nesta transação: sem isso, o
     * Hibernate devolveria a cópia antiga em memória, sem o pagamento que outra transação acabou
     * de gravar enquanto esta esperava a trava. Os pagamentos são relidos também.
     */
    @Override
    public Optional<Titulo> travar(Long id) {
        return jpa.findById(id).map(registro -> {
            entityManager.refresh(registro, LockModeType.PESSIMISTIC_WRITE);
            List<BaixaJpa> doTitulo = baixas.findByTituloIdOrderByNumero(id);
            doTitulo.forEach(entityManager::refresh);
            return paraDominio(registro, doTitulo);
        });
    }

    @Override
    public Pagina<Titulo> pesquisar(FiltroDeTitulos filtro, Paginacao paginacao) {
        Page<TituloJpa> pagina = jpa.findAll(especificacao(filtro), Paginas.paraSpring(paginacao, POR_VENCIMENTO));
        List<Long> ids = pagina.map(TituloJpa::getId).toList();
        // uma consulta para os pagamentos da página inteira, em vez de uma por título
        Map<Long, List<BaixaJpa>> baixasPorTitulo = ids.isEmpty() ? Map.of()
                : baixas.findByTituloIdInOrderByNumero(ids).stream().collect(Collectors.groupingBy(BaixaJpa::getTituloId));
        return Paginas.daSpring(pagina, t -> paraDominio(t, baixasPorTitulo.getOrDefault(t.getId(), List.of())));
    }

    @Override
    public TotalDeTitulos totalizar(FiltroDeTitulos filtro) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> consulta = cb.createTupleQuery();
        Root<TituloJpa> titulo = consulta.from(TituloJpa.class);
        consulta.multiselect(cb.count(titulo), cb.coalesce(cb.sum(titulo.<BigDecimal>get("saldoDevedor")), BigDecimal.ZERO))
                .where(especificacao(filtro).toPredicate(titulo, consulta, cb));
        Tuple total = entityManager.createQuery(consulta).getSingleResult();
        return new TotalDeTitulos(total.get(0, Long.class), Dinheiro.de(total.get(1, BigDecimal.class)));
    }

    @Override
    public List<Titulo> doDocumento(OrigemDoTitulo origem, Long documentoDeOrigem) {
        return jpa.findByOrigemAndDocumentoDeOrigemOrderByParcela(origem, documentoDeOrigem).stream()
                .map(this::comBaixas)
                .toList();
    }

    private static Specification<TituloJpa> especificacao(FiltroDeTitulos filtro) {
        String campoDaPessoa = filtro.natureza() == NaturezaDoTitulo.A_PAGAR ? "fornecedorId" : "clienteId";
        Specification<TituloJpa> daNatureza = (t, q, cb) -> cb.equal(t.get("natureza"), filtro.natureza());
        Specification<TituloJpa> daPessoa = (t, q, cb) -> filtro.pessoaId() == null ? null
                : cb.equal(t.get(campoDaPessoa), filtro.pessoaId());
        Specification<TituloJpa> naSituacao = (t, q, cb) -> filtro.situacao() == null ? null
                : cb.equal(t.get("situacao"), filtro.situacao());
        Specification<TituloJpa> emAberto = (t, q, cb) -> filtro.emAberto() == null ? null
                : filtro.emAberto() ? emAberto(t, cb) : cb.not(emAberto(t, cb));
        Specification<TituloJpa> aPartirDe = (t, q, cb) -> filtro.vencimentoDe() == null ? null
                : cb.greaterThanOrEqualTo(t.get("vencimento"), filtro.vencimentoDe());
        Specification<TituloJpa> ate = (t, q, cb) -> filtro.vencimentoAte() == null ? null
                : cb.lessThanOrEqualTo(t.get("vencimento"), filtro.vencimentoAte());
        Specification<TituloJpa> vencidos = (t, q, cb) -> filtro.vencidosAntesDe() == null ? null
                : cb.and(emAberto(t, cb), cb.lessThan(t.get("vencimento"), filtro.vencidosAntesDe()));
        return Specification.allOf(daNatureza, daPessoa, naSituacao, emAberto, aPartirDe, ate, vencidos);
    }

    private static jakarta.persistence.criteria.Predicate emAberto(Root<TituloJpa> titulo, CriteriaBuilder cb) {
        return titulo.get("situacao").in(SituacaoDoTitulo.ABERTO, SituacaoDoTitulo.PARCIALMENTE_PAGO);
    }

    private TituloJpa novoRegistro(Titulo titulo) {
        var registro = new TituloJpa();
        registro.setEmpresaId(titulo.empresaId());
        registro.setNatureza(titulo.natureza());
        if (titulo.natureza() == NaturezaDoTitulo.A_PAGAR) {
            registro.setFornecedorId(titulo.pessoaId());
        } else {
            registro.setClienteId(titulo.pessoaId());
        }
        registro.setFormaPrevista(titulo.formaPrevista());
        registro.setOrigem(titulo.origem());
        registro.setDocumentoDeOrigem(titulo.documentoDeOrigem());
        registro.setParcela(titulo.parcela());
        registro.setParcelas(titulo.parcelas());
        return registro;
    }

    /** A versão do domínio é a que foi lida: se outro alterou nesse meio-tempo, recusa em vez de sobrescrever. */
    private TituloJpa registroNaVersao(Titulo titulo) {
        TituloJpa registro = jpa.findById(titulo.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Título", titulo.id()));
        if (!registro.getVersao().equals(titulo.versao())) {
            throw new ConflitoDeEdicaoException("Título", titulo.id());
        }
        return registro;
    }

    private void salvarBaixas(TituloJpa titulo, List<Baixa> doDominio) {
        Map<Integer, BaixaJpa> existentes = baixas.findByTituloIdOrderByNumero(titulo.getId()).stream()
                .collect(Collectors.toMap(BaixaJpa::getNumero, b -> b));
        for (Baixa baixa : doDominio) {
            BaixaJpa registro = existentes.computeIfAbsent(baixa.numero(), numero -> novaBaixa(titulo, baixa));
            registro.setEstornada(baixa.estornada());
            registro.setMotivoDoEstorno(baixa.motivoDoEstorno());
            registro.setEstornadaEm(baixa.estornadaEm());
            baixas.save(registro);
        }
        baixas.flush();
    }

    private static BaixaJpa novaBaixa(TituloJpa titulo, Baixa baixa) {
        var registro = new BaixaJpa();
        registro.setEmpresaId(titulo.getEmpresaId());
        registro.setTituloId(titulo.getId());
        registro.setNumero(baixa.numero());
        registro.setContaId(baixa.contaId());
        registro.setForma(baixa.forma());
        registro.setValorPago(baixa.valorPago().valor());
        registro.setJuros(baixa.juros().valor());
        registro.setMulta(baixa.multa().valor());
        registro.setDesconto(baixa.desconto().valor());
        registro.setUsuarioId(baixa.usuarioId());
        registro.setMomento(baixa.momento());
        return registro;
    }

    private Titulo comBaixas(TituloJpa registro) {
        return paraDominio(registro, baixas.findByTituloIdOrderByNumero(registro.getId()));
    }

    private static Titulo paraDominio(TituloJpa r, List<BaixaJpa> baixasDoTitulo) {
        Long pessoa = r.getNatureza() == NaturezaDoTitulo.A_PAGAR ? r.getFornecedorId() : r.getClienteId();
        var dados = new DadosDoTitulo(r.getNatureza(), pessoa, r.getDescricao(), r.getCategoriaId(), r.getDocumento(),
                r.getFormaPrevista(), r.getOrigem(), r.getDocumentoDeOrigem());
        var parcela = new Parcela(r.getParcela(), r.getParcelas(), Dinheiro.de(r.getValor()), r.getVencimento());
        return Titulo.reconstituir(r.getId(), r.getEmpresaId(), dados, parcela, r.getSituacao(),
                baixasDoTitulo.stream().map(TituloRepositorioJpa::paraDominio).toList(), r.getMotivoDoCancelamento(),
                r.getCanceladoEm(), r.getVersao());
    }

    private static Baixa paraDominio(BaixaJpa b) {
        return new Baixa(b.getId(), b.getNumero(), b.getContaId(), b.getForma(), Dinheiro.de(b.getValorPago()),
                Dinheiro.de(b.getJuros()), Dinheiro.de(b.getMulta()), Dinheiro.de(b.getDesconto()), b.getUsuarioId(),
                b.getMomento(), b.isEstornada(), b.getMotivoDoEstorno(), b.getEstornadaEm());
    }
}
