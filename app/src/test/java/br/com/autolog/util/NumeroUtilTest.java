package br.com.autolog.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NumeroUtilTest {

    @Test
    public void converteDecimaisNosFormatosAceitos() {
        assertEquals(150.0, NumeroUtil.converterDecimal("150"), 0.001);
        assertEquals(150.5, NumeroUtil.converterDecimal("150,50"), 0.001);
        assertEquals(1250.0, NumeroUtil.converterDecimal("1.250,00"), 0.001);
        assertEquals(150.5, NumeroUtil.converterDecimal("150.50"), 0.001);
        assertEquals(99.9, NumeroUtil.converterDecimal("R$ 99,90"), 0.001);
    }

    @Test
    public void rejeitaDecimaisInvalidos() {
        assertNull(NumeroUtil.converterDecimal("abc"));
        assertNull(NumeroUtil.converterDecimal("1,2,3"));
        assertNull(NumeroUtil.converterDecimal(""));
        assertNull(NumeroUtil.converterDecimal(null));
    }

    @Test
    public void converteInteiros() {
        assertEquals(Integer.valueOf(45000), NumeroUtil.converterInteiro("45000"));
        assertEquals(Integer.valueOf(45000), NumeroUtil.converterInteiro("45.000"));
        assertNull(NumeroUtil.converterInteiro("45,5"));
        assertNull(NumeroUtil.converterInteiro("km"));
    }

    @Test
    public void formataNoPadraoBrasileiro() {
        assertEquals("45.000", NumeroUtil.formatarInteiro(45000));
        assertEquals("150,00", NumeroUtil.formatarDecimalCampo(150.0));
        String moeda = NumeroUtil.formatarMoeda(1234.5);
        assertTrue(moeda, moeda.contains("1.234,50"));
        assertTrue(moeda, moeda.startsWith("R$"));
    }
}
