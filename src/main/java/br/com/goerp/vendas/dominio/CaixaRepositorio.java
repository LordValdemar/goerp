package br.com.goerp.vendas.dominio;

import java.util.List;
import java.util.Optional;

/** Caixas da empresa atual. */
public interface CaixaRepositorio {

    Caixa salvar(Caixa caixa);

    Optional<Caixa> buscarPorId(Long id);

    Optional<Caixa> buscarAbertoDoOperador(Long operadorId);

    /** Do mais recente para o mais antigo; {@code situacao} nula traz todos. */
    List<Caixa> listar(SituacaoDoCaixa situacao);
}
