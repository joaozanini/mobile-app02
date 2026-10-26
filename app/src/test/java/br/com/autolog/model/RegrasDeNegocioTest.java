package br.com.autolog.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

/** Testes das regras de negócio: próxima manutenção por tipo e status automático. */
public class RegrasDeNegocioTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 26);

    @Test
    public void proximaDataSomaOIntervaloDoTipo() {
        LocalDate servico = LocalDate.of(2026, 10, 1);
        assertEquals(LocalDate.of(2027, 4, 1), TipoManutencao.TROCA_DE_OLEO.calcularProximaData(servico));
        assertEquals(LocalDate.of(2027, 10, 1), TipoManutencao.REVISAO_GERAL.calcularProximaData(servico));
        assertEquals(LocalDate.of(2028, 10, 1), TipoManutencao.BATERIA.calcularProximaData(servico));
    }

    @Test
    public void statusVencidaQuandoDataPrevistaJaPassou() {
        assertEquals(StatusManutencao.VENCIDA, StatusManutencao.calcular(HOJE.minusDays(1), HOJE));
    }

    @Test
    public void statusVenceEmBreveAteTrintaDias() {
        assertEquals(StatusManutencao.VENCE_EM_BREVE, StatusManutencao.calcular(HOJE, HOJE));
        assertEquals(StatusManutencao.VENCE_EM_BREVE, StatusManutencao.calcular(HOJE.plusDays(30), HOJE));
    }

    @Test
    public void statusEmDiaAcimaDeTrintaDias() {
        assertEquals(StatusManutencao.EM_DIA, StatusManutencao.calcular(HOJE.plusDays(31), HOJE));
        assertEquals(StatusManutencao.EM_DIA, StatusManutencao.calcular(HOJE.plusYears(1), HOJE));
    }

    @Test
    public void manutencaoCalculaStatusAPartirDoTipoEData() {
        Manutencao m = new Manutencao();
        m.setTipo(TipoManutencao.TROCA_DE_OLEO);       // 6 meses
        m.setData(LocalDate.of(2026, 1, 10));          // prevista: 10/07/2026 (já passou)
        assertEquals(LocalDate.of(2026, 7, 10), m.getProximaData());
        assertEquals(StatusManutencao.VENCIDA, m.calcularStatus(HOJE));
    }

    @Test
    public void buscaDeTipoIgnoraAcentosEMaiusculas() {
        List<TipoManutencao> tipos = TipoManutencao.buscarPorDescricao("OLEO");
        assertEquals(1, tipos.size());
        assertEquals(TipoManutencao.TROCA_DE_OLEO, tipos.get(0));
        assertTrue(TipoManutencao.buscarPorDescricao("xyz").isEmpty());
        assertTrue(TipoManutencao.buscarPorDescricao("").isEmpty());
    }

    @Test
    public void tipoDesconhecidoViraOutro() {
        assertEquals(TipoManutencao.OUTRO, TipoManutencao.deNome("INEXISTENTE"));
        assertEquals(TipoManutencao.PNEUS, TipoManutencao.deNome("PNEUS"));
    }
}
