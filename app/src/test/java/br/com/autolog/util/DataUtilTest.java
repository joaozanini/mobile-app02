package br.com.autolog.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.time.LocalDate;

public class DataUtilTest {

    @Test
    public void converteDataValidaNoFormatoBrasileiro() {
        assertEquals(LocalDate.of(2026, 10, 26), DataUtil.converter("26/10/2026"));
        assertEquals(LocalDate.of(2026, 1, 5), DataUtil.converter("5/1/2026"));
        assertEquals(LocalDate.of(2024, 2, 29), DataUtil.converter("29/02/2024")); // ano bissexto
    }

    @Test
    public void rejeitaDatasInvalidasOuMalFormatadas() {
        assertNull(DataUtil.converter("31/02/2026"));   // dia inexistente
        assertNull(DataUtil.converter("29/02/2026"));   // não é bissexto
        assertNull(DataUtil.converter("2026-10-26"));   // formato errado
        assertNull(DataUtil.converter("26/13/2026"));   // mês inválido
        assertNull(DataUtil.converter("abc"));
        assertNull(DataUtil.converter(""));
        assertNull(DataUtil.converter(null));
    }

    @Test
    public void formataParaExibicaoEParaBanco() {
        LocalDate data = LocalDate.of(2026, 3, 7);
        assertEquals("07/03/2026", DataUtil.formatar(data));
        assertEquals("2026-03-07", DataUtil.paraBanco(data));
        assertEquals(data, DataUtil.doBanco("2026-03-07"));
    }
}
