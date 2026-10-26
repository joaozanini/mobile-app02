package br.com.autolog.model;

import androidx.annotation.NonNull;

import java.time.LocalDate;

/**
 * Model: representa um registro de manutenção veicular (uma linha da tabela "manutencao").
 */
public class Manutencao {

    private long id;
    private String veiculo;
    private TipoManutencao tipo;
    private LocalDate data;          // data em que o serviço foi realizado
    private int km;                  // quilometragem do veículo no dia do serviço
    private double custo;            // valor pago pelo serviço (R$)
    private LocalDate proximaData;   // data prevista da próxima manutenção (regra de negócio)
    private String observacoes;      // campo opcional

    public Manutencao() {
    }

    public Manutencao(long id, String veiculo, TipoManutencao tipo, LocalDate data, int km,
                      double custo, LocalDate proximaData, String observacoes) {
        this.id = id;
        this.veiculo = veiculo;
        this.tipo = tipo;
        this.data = data;
        this.km = km;
        this.custo = custo;
        this.proximaData = proximaData;
        this.observacoes = observacoes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(String veiculo) {
        this.veiculo = veiculo;
    }

    public TipoManutencao getTipo() {
        return tipo;
    }

    public void setTipo(TipoManutencao tipo) {
        this.tipo = tipo;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public int getKm() {
        return km;
    }

    public void setKm(int km) {
        this.km = km;
    }

    public double getCusto() {
        return custo;
    }

    public void setCusto(double custo) {
        this.custo = custo;
    }

    public LocalDate getProximaData() {
        if (proximaData == null && tipo != null && data != null) {
            proximaData = tipo.calcularProximaData(data);
        }
        return proximaData;
    }

    public void setProximaData(LocalDate proximaData) {
        this.proximaData = proximaData;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    /** Status calculado em relação a uma data de referência (facilita os testes). */
    public StatusManutencao calcularStatus(LocalDate hoje) {
        return StatusManutencao.calcular(getProximaData(), hoje);
    }

    /** Status calculado em relação à data de hoje. */
    public StatusManutencao getStatus() {
        return calcularStatus(LocalDate.now());
    }

    @NonNull
    @Override
    public String toString() {
        return "Manutencao{id=" + id + ", veiculo='" + veiculo + "', tipo=" + tipo
                + ", data=" + data + ", km=" + km + ", custo=" + custo
                + ", proximaData=" + proximaData + "}";
    }
}
