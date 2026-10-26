package br.com.autolog.model;

import androidx.annotation.NonNull;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Tipos de manutenção suportados pelo AutoLog.
 * <p>
 * Cada tipo possui um intervalo recomendado (em meses) que é usado pela regra de negócio
 * que calcula a data prevista da próxima manutenção.
 */
public enum TipoManutencao {
    TROCA_DE_OLEO("Troca de óleo", 6),
    FILTROS("Troca de filtros", 6),
    ALINHAMENTO("Alinhamento e balanceamento", 6),
    FREIOS("Freios (pastilhas/discos)", 12),
    PNEUS("Pneus", 12),
    REVISAO_GERAL("Revisão geral", 12),
    BATERIA("Bateria", 24),
    OUTRO("Outro serviço", 12);

    private final String descricao;
    private final int intervaloMeses;

    TipoManutencao(String descricao, int intervaloMeses) {
        this.descricao = descricao;
        this.intervaloMeses = intervaloMeses;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getIntervaloMeses() {
        return intervaloMeses;
    }

    /**
     * Regra de negócio: a próxima manutenção é prevista somando o intervalo
     * recomendado do tipo à data em que o serviço foi realizado.
     */
    public LocalDate calcularProximaData(LocalDate dataServico) {
        return dataServico.plusMonths(intervaloMeses);
    }

    /** Converte o nome gravado no banco de volta para o enum (OUTRO caso desconhecido). */
    public static TipoManutencao deNome(String nome) {
        if (nome == null) {
            return OUTRO;
        }
        try {
            return valueOf(nome);
        } catch (IllegalArgumentException e) {
            return OUTRO;
        }
    }

    /**
     * Retorna os tipos cuja descrição contém o termo informado, ignorando acentos e
     * maiúsculas/minúsculas. Usado pela busca da tela principal (ex.: "oleo" encontra "Troca de óleo").
     */
    public static List<TipoManutencao> buscarPorDescricao(String termo) {
        List<TipoManutencao> encontrados = new ArrayList<>();
        if (termo == null || termo.trim().isEmpty()) {
            return encontrados;
        }
        String termoNormalizado = normalizar(termo);
        for (TipoManutencao tipo : values()) {
            if (normalizar(tipo.descricao).contains(termoNormalizado)) {
                encontrados.add(tipo);
            }
        }
        return encontrados;
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    /** O Spinner da tela de cadastro usa toString() para exibir cada opção. */
    @NonNull
    @Override
    public String toString() {
        return descricao;
    }
}
