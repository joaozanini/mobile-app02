package br.com.autolog.model;

import java.time.LocalDate;

/**
 * Status de uma manutenção, calculado automaticamente (regra de negócio) a partir da
 * data prevista da próxima manutenção em relação à data atual.
 */
public enum StatusManutencao {
    EM_DIA("Em dia"),
    VENCE_EM_BREVE("Vence em breve"),
    VENCIDA("Vencida");

    /** Dias antes da data prevista a partir dos quais a manutenção passa a "Vence em breve". */
    public static final int DIAS_ALERTA = 30;

    private final String descricao;

    StatusManutencao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Regra de negócio do status:
     * <ul>
     *   <li>VENCIDA: a data prevista já passou;</li>
     *   <li>VENCE_EM_BREVE: faltam até {@link #DIAS_ALERTA} dias para a data prevista;</li>
     *   <li>EM_DIA: faltam mais de {@link #DIAS_ALERTA} dias.</li>
     * </ul>
     */
    public static StatusManutencao calcular(LocalDate proximaData, LocalDate hoje) {
        if (proximaData.isBefore(hoje)) {
            return VENCIDA;
        }
        if (!proximaData.isAfter(hoje.plusDays(DIAS_ALERTA))) {
            return VENCE_EM_BREVE;
        }
        return EM_DIA;
    }
}
