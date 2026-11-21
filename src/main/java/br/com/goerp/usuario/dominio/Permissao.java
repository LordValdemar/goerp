package br.com.goerp.usuario.dominio;

/**
 * O que um usuário pode fazer dentro de uma empresa. Cada módulo acrescenta as suas.
 * O nome da constante é o que vai no token e no {@code @PreAuthorize}.
 */
public enum Permissao {
    USUARIO_LER("Consultar usuários da empresa"),
    USUARIO_GERENCIAR("Cadastrar, alterar e inativar usuários da empresa"),
    PERFIL_LER("Consultar perfis de acesso"),
    PERFIL_GERENCIAR("Cadastrar, alterar e inativar perfis de acesso"),
    CLIENTE_LER("Consultar clientes"),
    CLIENTE_GERENCIAR("Cadastrar, alterar e inativar clientes"),
    FORNECEDOR_LER("Consultar fornecedores"),
    FORNECEDOR_GERENCIAR("Cadastrar, alterar e inativar fornecedores"),
    PRODUTO_LER("Consultar produtos, categorias e unidades de medida"),
    PRODUTO_GERENCIAR("Cadastrar, alterar e inativar produtos, categorias e unidades de medida"),
    ESTOQUE_LER("Consultar saldos e movimentações de estoque"),
    ESTOQUE_MOVIMENTAR("Lançar entradas e saídas manuais de estoque"),
    ESTOQUE_AJUSTAR("Ajustar e estornar estoque, fazer inventário e configurar o estoque"),
    CAIXA_OPERAR("Abrir e fechar o próprio caixa e vender no PDV"),
    CAIXA_GERENCIAR("Ver e fechar caixas de outros operadores, lançar suprimento e sangria, configurar vendas"),
    VENDA_LER("Consultar vendas"),
    VENDA_DESCONTO("Dar desconto em vendas (até o máximo configurado)"),
    VENDA_CANCELAR("Cancelar vendas enquanto o caixa da venda estiver aberto"),
    PEDIDO_LER("Consultar pedidos de venda e orçamentos"),
    PEDIDO_GERENCIAR("Criar, alterar, confirmar e cancelar pedidos de venda e orçamentos"),
    PEDIDO_FATURAR("Faturar pedidos de venda (baixa o estoque)");

    private final String descricao;

    Permissao(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }
}
