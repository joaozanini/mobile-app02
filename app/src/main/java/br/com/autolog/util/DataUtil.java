package br.com.autolog.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Conversões de data: a interface usa dd/mm/aaaa e o banco grava no formato ISO (yyyy-MM-dd).
 */
public final class DataUtil {

    // "uuuu" + STRICT rejeita datas inexistentes, como 31/02/2026
    private static final DateTimeFormatter FORMATO_BR =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter FORMATO_BR_CURTO =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private DataUtil() {
    }

    /** Converte "dd/mm/aaaa" (ou "d/m/aaaa") em LocalDate; retorna null se a data for inválida. */
    public static LocalDate converter(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        String limpo = texto.trim();
        try {
            return LocalDate.parse(limpo, FORMATO_BR);
        } catch (DateTimeParseException ignorada) {
            try {
                return LocalDate.parse(limpo, FORMATO_BR_CURTO);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }

    /** Formata para exibição: dd/mm/aaaa. */
    public static String formatar(LocalDate data) {
        return data == null ? "" : data.format(FORMATO_BR);
    }

    /** Formato gravado no banco (ISO yyyy-MM-dd), que permite ordenar e comparar como texto. */
    public static String paraBanco(LocalDate data) {
        return data.toString();
    }

    public static LocalDate doBanco(String iso) {
        return LocalDate.parse(iso);
    }
}
