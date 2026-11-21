package br.com.goerp.plataforma.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.goerp.empresa.aplicacao.AlterarDadosDaEmpresa;
import br.com.goerp.empresa.aplicacao.BuscarEmpresa;
import br.com.goerp.empresa.aplicacao.InativarEmpresa;
import br.com.goerp.empresa.aplicacao.ListarEmpresas;
import br.com.goerp.empresa.aplicacao.ReativarEmpresa;
import br.com.goerp.plataforma.aplicacao.CadastrarEmpresaComAdministrador;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Gestão das empresas clientes, exclusiva do administrador da plataforma. */
@RestController
@RequestMapping("/api/v1/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final CadastrarEmpresaComAdministrador cadastrarEmpresa;
    private final ListarEmpresas listarEmpresas;
    private final BuscarEmpresa buscarEmpresa;
    private final AlterarDadosDaEmpresa alterarDados;
    private final InativarEmpresa inativarEmpresa;
    private final ReativarEmpresa reativarEmpresa;

    @PostMapping
    @PreAuthorize("hasAuthority('PLATAFORMA_ADMINISTRAR')")
    public ResponseEntity<EmpresaCadastradaResponse> cadastrar(@Valid @RequestBody NovaEmpresaRequest request) {
        var cadastrada = EmpresaCadastradaResponse.de(cadastrarEmpresa.executar(request.paraDados()));
        return ResponseEntity.created(URI.create("/api/v1/empresas/" + cadastrada.empresa().id())).body(cadastrada);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PLATAFORMA_ADMINISTRAR')")
    public List<EmpresaResponse> listar() {
        return listarEmpresas.executar().stream().map(EmpresaResponse::de).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PLATAFORMA_ADMINISTRAR')")
    public EmpresaResponse buscar(@PathVariable Long id) {
        return EmpresaResponse.de(buscarEmpresa.executar(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PLATAFORMA_ADMINISTRAR')")
    public EmpresaResponse alterar(@PathVariable Long id, @Valid @RequestBody DadosDaEmpresaRequest request) {
        return EmpresaResponse.de(alterarDados.executar(id, request.razaoSocial(), request.nomeFantasia()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PLATAFORMA_ADMINISTRAR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        inativarEmpresa.executar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reativar")
    @PreAuthorize("hasAuthority('PLATAFORMA_ADMINISTRAR')")
    public EmpresaResponse reativar(@PathVariable Long id) {
        return EmpresaResponse.de(reativarEmpresa.executar(id));
    }
}
