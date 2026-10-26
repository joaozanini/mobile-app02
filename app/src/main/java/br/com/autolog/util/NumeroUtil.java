package br.com.autolog.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Formatação e conversão de números no padrão brasileiro (R$ 1.234,56 / 45.000 km).
 */
public final class NumeroUtil {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private NumeroUtil() {
    }

    /** Ex.: 1234.5 -> "R$ 1.234,50" */
    public static String formatarMoeda(double valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    /** Ex.: 45000 -> "45.000" */
    public static String formatarInteiro(int valor) {
        return NumberFormat.getIntegerInstance(PT_BR).format(valor);
    }

    /** Valor para preencher o campo de custo na edição. Ex.: 150.0 -> "150,00" */
    public static String formatarDecimalCampo(double valor) {
        return String.format(PT_BR, "%.2f", valor);
    }

    /** Aceita "150", "150,50", "1.250,00" ou "150.50"; retorna null se não for um número. */
    public static Double converterDecimal(String texto) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.replace("R$", "").replace(" ", "").replace(" ", "").trim();
        if (limpo.isEmpty()) {
            return null;
        }
        if (limpo.contains(",")) {
            // formato brasileiro: ponto de milhar e vírgula decimal
            limpo = limpo.replace(".", "").replace(",", ".");
        }
        try {
            return Double.parseDouble(limpo);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Aceita "45000" ou "45.000"; retorna null se não for um inteiro. */
    public static Integer converterInteiro(String texto) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.replace(".", "").replace(" ", "").trim();
        if (limpo.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(limpo);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
