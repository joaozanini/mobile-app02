package br.com.autolog.controller;

/**
 * Lançada quando algum campo do formulário é inválido. Carrega o campo com problema
 * para que a tela possa focá-lo automaticamente e exibir a mensagem (Toast).
 */
public class ValidacaoException extends Exception {

    public enum Campo { VEICULO, TIPO, DATA, KM, CUSTO, OBSERVACOES }

    private final Campo campo;

    public ValidacaoException(Campo campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public Campo getCampo() {
        return campo;
    }
}
