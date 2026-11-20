package br.com.goerp.empresa.dominio;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import br.com.goerp.compartilhado.dominio.Cnpj;

public interface EmpresaRepositorio {

    Empresa salvar(Empresa empresa);

    Optional<Empresa> buscarPorId(Long id);

    boolean existeComCnpj(Cnpj cnpj);

    List<Empresa> listarAtivasPorIds(Collection<Long> ids);
}
