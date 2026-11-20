package br.com.goerp.plataforma.aplicacao;

import br.com.goerp.cadastros.produto.aplicacao.PrepararCadastrosDaEmpresa;
import br.com.goerp.compartilhado.aplicacao.AcessoEntreEmpresas;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.empresa.aplicacao.CadastrarEmpresa;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.usuario.aplicacao.gestao.AcessoInicial;
import br.com.goerp.usuario.aplicacao.gestao.ConfigurarAcessoInicialDaEmpresa;

/**
 * Cadastro de uma nova empresa pelo administrador da plataforma, já pronta para uso:
 * empresa + perfil Administrador + primeiro usuário administrador + unidades de medida padrão,
 * numa única transação
 * (se qualquer parte falhar, nada é gravado).
 *
 * <p>Fica num módulo próprio porque coordena vários módulos ({@code empresa}, {@code usuario}, {@code cadastros})
 * sem criar dependência circular entre eles. Acessa todas as empresas porque cria dados
 * de uma empresa da qual quem chama não faz parte.
 */
@CasoDeUso
@AcessoEntreEmpresas
public class CadastrarEmpresaComAdministrador {

    private final CadastrarEmpresa cadastrarEmpresa;
    private final ConfigurarAcessoInicialDaEmpresa configurarAcessoInicial;
    private final PrepararCadastrosDaEmpresa prepararCadastros;

    public CadastrarEmpresaComAdministrador(CadastrarEmpresa cadastrarEmpresa,
            ConfigurarAcessoInicialDaEmpresa configurarAcessoInicial, PrepararCadastrosDaEmpresa prepararCadastros) {
        this.cadastrarEmpresa = cadastrarEmpresa;
        this.configurarAcessoInicial = configurarAcessoInicial;
        this.prepararCadastros = prepararCadastros;
    }

    public EmpresaCadastrada executar(NovaEmpresa nova) {
        Empresa empresa = cadastrarEmpresa.executar(nova.empresa());
        AcessoInicial acesso = configurarAcessoInicial.executar(empresa.id(), nova.administrador());
        prepararCadastros.executar(empresa.id());
        return new EmpresaCadastrada(empresa, acesso);
    }
}
