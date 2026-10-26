package br.com.autolog.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.time.LocalDate;

import br.com.autolog.controller.ValidacaoException.Campo;
import br.com.autolog.model.Manutencao;
import br.com.autolog.model.TipoManutencao;

/** Testes do Requisito 7 – validação de dados antes de chamar o DAO. */
public class ValidadorManutencaoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 26);

    private Campo campoInvalido(String veiculo, TipoManutencao tipo, String data,
                                String km, String custo, String obs) {
        try {
            ValidadorManutencao.validar(null, veiculo, tipo, data, km, custo, obs, HOJE);
            fail("Era esperada uma ValidacaoException");
            return null;
        } catch (ValidacaoException e) {
            return e.getCampo();
        }
    }

    @Test
    public void dadosValidosGeramManutencaoPreenchida() throws ValidacaoException {
        Manutencao m = ValidadorManutencao.validar(null, "  Gol 2015 ", TipoManutencao.TROCA_DE_OLEO,
                "01/10/2026", "45000", "150,50", " Óleo 5W30 ", HOJE);
        assertEquals(0, m.getId());
        assertEquals("Gol 2015", m.getVeiculo());
        assertEquals(TipoManutencao.TROCA_DE_OLEO, m.getTipo());
        assertEquals(LocalDate.of(2026, 10, 1), m.getData());
        assertEquals(45000, m.getKm());
        assertEquals(150.5, m.getCusto(), 0.001);
        assertEquals("Óleo 5W30", m.getObservacoes());
    }

    @Test
    public void mantemIdQuandoEdicao() throws ValidacaoException {
        Manutencao m = ValidadorManutencao.validar(7L, "Gol 2015", TipoManutencao.PNEUS,
                "01/10/2026", "45000", "0", "", HOJE);
        assertEquals(7, m.getId());
    }

    @Test
    public void camposObrigatoriosEmBranco() {
        assertEquals(Campo.VEICULO, campoInvalido("", TipoManutencao.PNEUS, "01/10/2026", "1", "1", ""));
        assertEquals(Campo.VEICULO, campoInvalido(null, TipoManutencao.PNEUS, "01/10/2026", "1", "1", ""));
        assertEquals(Campo.TIPO, campoInvalido("Gol", null, "01/10/2026", "1", "1", ""));
        assertEquals(Campo.DATA, campoInvalido("Gol", TipoManutencao.PNEUS, "", "1", "1", ""));
        assertEquals(Campo.KM, campoInvalido("Gol", TipoManutencao.PNEUS, "01/10/2026", "", "1", ""));
        assertEquals(Campo.CUSTO, campoInvalido("Gol", TipoManutencao.PNEUS, "01/10/2026", "1", "", ""));
    }

    @Test
    public void tamanhoDosTextosForaDoLimite() {
        assertEquals(Campo.VEICULO, campoInvalido("G", TipoManutencao.PNEUS, "01/10/2026", "1", "1", ""));
        String longo = "a".repeat(41);
        assertEquals(Campo.VEICULO, campoInvalido(longo, TipoManutencao.PNEUS, "01/10/2026", "1", "1", ""));
        String obsLonga = "x".repeat(201);
        assertEquals(Campo.OBSERVACOES, campoInvalido("Gol", TipoManutencao.PNEUS, "01/10/2026", "1", "1", obsLonga));
    }

    @Test
    public void datasInvalidasOuFuturas() {
        assertEquals(Campo.DATA, campoInvalido("Gol", TipoManutencao.PNEUS, "31/02/2026", "1", "1", ""));
        assertEquals(Campo.DATA, campoInvalido("Gol", TipoManutencao.PNEUS, "2026-10-01", "1", "1", ""));
        assertEquals(Campo.DATA, campoInvalido("Gol", TipoManutencao.PNEUS, "27/10/2026", "1", "1", "")); // amanhã
    }

    @Test
    public void numerosNegativosOuInvalidos() {
        assertEquals(Campo.KM, campoInvalido("Gol", TipoManutencao.PNEUS, "01/10/2026", "-10", "1", ""));
        assertEquals(Campo.KM, campoInvalido("Gol", TipoManutencao.PNEUS, "01/10/2026", "abc", "1", ""));
        assertEquals(Campo.KM, campoInvalido("Gol", TipoManutencao.PNEUS, "01/10/2026", "99999999", "1", ""));
        assertEquals(Campo.CUSTO, campoInvalido("Gol", TipoManutencao.PNEUS, "01/10/2026", "1", "-5", ""));
        assertEquals(Campo.CUSTO, campoInvalido("Gol", TipoManutencao.PNEUS, "01/10/2026", "1", "abc", ""));
        assertEquals(Campo.CUSTO, campoInvalido("Gol", TipoManutencao.PNEUS, "01/10/2026", "1", "1000000", ""));
    }
}
