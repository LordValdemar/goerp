package br.com.goerp.suporte;

import org.springframework.stereotype.Component;

import br.com.goerp.compartilhado.dominio.Cnpj;
import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.empresa.dominio.EmpresaRepositorio;
import br.com.goerp.usuario.aplicacao.CodificadorDeSenha;
import br.com.goerp.usuario.dominio.Senha;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;
import br.com.goerp.usuario.dominio.VinculoEmpresa;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;
import lombok.RequiredArgsConstructor;

/** Monta dados de teste (empresas, usuários e vínculos) direto pelos repositórios. */
@Component
@RequiredArgsConstructor
public class Cenario {

    public static final String SENHA = "segredo123";

    private final EmpresaRepositorio empresas;
    private final UsuarioRepositorio usuarios;
    private final VinculoEmpresaRepositorio vinculos;
    private final CodificadorDeSenha codificador;

    public Empresa empresa(String cnpj, String razaoSocial) {
        return empresas.salvar(Empresa.nova(Cnpj.de(cnpj), razaoSocial, null));
    }

    public Usuario usuario(String email) {
        return usuarios.salvar(Usuario.novo("Usuário " + email, Email.de(email), codificador.criptografar(Senha.de(SENHA))));
    }

    public Usuario administradorDaPlataforma(String email) {
        return usuarios.salvar(Usuario.novoAdministradorDaPlataforma("Admin", Email.de(email),
                codificador.criptografar(Senha.de(SENHA))));
    }

    public VinculoEmpresa vincular(Usuario usuario, Empresa empresa) {
        return vinculos.salvar(VinculoEmpresa.novo(usuario.id(), empresa.id()));
    }
}
