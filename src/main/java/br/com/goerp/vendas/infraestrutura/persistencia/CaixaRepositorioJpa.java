package br.com.goerp.vendas.infraestrutura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.vendas.dominio.Caixa;
import br.com.goerp.vendas.dominio.CaixaRepositorio;
import br.com.goerp.vendas.dominio.ConferenciaDeCaixa;
import br.com.goerp.vendas.dominio.MovimentoDeCaixa;
import br.com.goerp.vendas.dominio.SituacaoDoCaixa;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CaixaRepositorioJpa implements CaixaRepositorio {

    private final CaixaJpaRepository jpa;

    @Override
    public Caixa salvar(Caixa caixa) {
        CaixaJpa registro = caixa.id() == null ? novoRegistro(caixa) : buscarRegistro(caixa.id());
        registro.setSituacao(caixa.situacao());
        registro.setFechadoEm(caixa.fechadoEm());
        registro.setFechadoPor(caixa.fechadoPor());
        List<MovimentoDeCaixa> movimentos = caixa.movimentos();
        for (int i = registro.getMovimentos().size(); i < movimentos.size(); i++) {
            MovimentoDeCaixa movimento = movimentos.get(i);
            registro.getMovimentos().add(new MovimentoDeCaixaJpa(movimento.tipo(), movimento.valor().valor(),
                    movimento.motivo(), movimento.usuarioId(), movimento.momento()));
        }
        if (registro.getConferencias().isEmpty()) {
            caixa.conferencias().forEach(conferencia -> registro.getConferencias().add(new ConferenciaDeCaixaJpa(
                    conferencia.forma(), conferencia.esperado().valor(), conferencia.contado().valor())));
        }
        return paraDominio(jpa.saveAndFlush(registro));
    }

    @Override
    public Optional<Caixa> buscarPorId(Long id) {
        return jpa.findById(id).map(CaixaRepositorioJpa::paraDominio);
    }

    @Override
    public Optional<Caixa> buscarAbertoDoOperador(Long operadorId) {
        return jpa.findByOperadorIdAndSituacao(operadorId, SituacaoDoCaixa.ABERTO).map(CaixaRepositorioJpa::paraDominio);
    }

    @Override
    public List<Caixa> listar(SituacaoDoCaixa situacao) {
        var registros = situacao == null ? jpa.findAllByOrderByIdDesc() : jpa.findBySituacaoOrderByIdDesc(situacao);
        return registros.stream().map(CaixaRepositorioJpa::paraDominio).toList();
    }

    private CaixaJpa novoRegistro(Caixa caixa) {
        var registro = new CaixaJpa();
        registro.setEmpresaId(caixa.empresaId());
        registro.setOperadorId(caixa.operadorId());
        registro.setTrocoInicial(caixa.trocoInicial().valor());
        registro.setAbertoEm(caixa.abertoEm());
        return registro;
    }

    private CaixaJpa buscarRegistro(Long id) {
        return jpa.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Caixa", id));
    }

    private static Caixa paraDominio(CaixaJpa registro) {
        List<MovimentoDeCaixa> movimentos = registro.getMovimentos().stream()
                .map(m -> new MovimentoDeCaixa(m.getTipo(), Dinheiro.de(m.getValor()), m.getMotivo(), m.getUsuarioId(),
                        m.getMomento()))
                .toList();
        List<ConferenciaDeCaixa> conferencias = registro.getConferencias().stream()
                .map(c -> new ConferenciaDeCaixa(c.getForma(), Dinheiro.de(c.getEsperado()), Dinheiro.de(c.getContado())))
                .toList();
        return Caixa.reconstituir(registro.getId(), registro.getEmpresaId(), registro.getOperadorId(),
                Dinheiro.de(registro.getTrocoInicial()), registro.getAbertoEm(), movimentos, registro.getSituacao(),
                registro.getFechadoEm(), registro.getFechadoPor(), conferencias);
    }
}
