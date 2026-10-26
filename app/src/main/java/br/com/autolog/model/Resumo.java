package br.com.autolog.model;

/**
 * Resumo exibido no topo da tela principal (regra de negócio de totalização):
 * total gasto, quantidade de registros e quantidade de manutenções vencidas.
 */
public class Resumo {

    private final double totalGasto;
    private final int quantidade;
    private final int vencidas;

    public Resumo(double totalGasto, int quantidade, int vencidas) {
        this.totalGasto = totalGasto;
        this.quantidade = quantidade;
        this.vencidas = vencidas;
    }

    public double getTotalGasto() {
        return totalGasto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public int getVencidas() {
        return vencidas;
    }
}
