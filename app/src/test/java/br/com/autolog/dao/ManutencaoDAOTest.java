package br.com.autolog.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.time.LocalDate;
import java.util.List;

import br.com.autolog.database.DBHelper;
import br.com.autolog.model.Manutencao;
import br.com.autolog.model.StatusManutencao;
import br.com.autolog.model.TipoManutencao;

/**
 * Testes do CRUD no SQLite real (via Robolectric), cobrindo inserção, listagem,
 * busca, atualização, exclusão, ordenação, filtros e persistência após reabrir o banco.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ManutencaoDAOTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 26);

    private Context contexto;
    private ManutencaoDAO dao;

    @Before
    public void prepararBanco() {
        contexto = RuntimeEnvironment.getApplication();
        contexto.deleteDatabase(DBHelper.NOME_BANCO);
        dao = new ManutencaoDAO(contexto);
    }

    @After
    public void fecharBanco() {
        dao.fechar();
    }

    private Manutencao nova(String veiculo, TipoManutencao tipo, LocalDate data, int km,
                            double custo, String observacoes) {
        Manutencao m = new Manutencao();
        m.setVeiculo(veiculo);
        m.setTipo(tipo);
        m.setData(data);
        m.setKm(km);
        m.setCusto(custo);
        m.setObservacoes(observacoes);
        m.setProximaData(tipo.calcularProximaData(data));
        return m;
    }

    @Test
    public void inserirEBuscarPorId() {
        long id = dao.inserir(nova("Gol 2015", TipoManutencao.TROCA_DE_OLEO,
                LocalDate.of(2026, 10, 1), 45000, 150.0, "Óleo 5W30"));
        assertTrue(id > 0);

        Manutencao salva = dao.buscarPorId(id);
        assertNotNull(salva);
        assertEquals(id, salva.getId());
        assertEquals("Gol 2015", salva.getVeiculo());
        assertEquals(TipoManutencao.TROCA_DE_OLEO, salva.getTipo());
        assertEquals(LocalDate.of(2026, 10, 1), salva.getData());
        assertEquals(45000, salva.getKm());
        assertEquals(150.0, salva.getCusto(), 0.001);
        assertEquals(LocalDate.of(2027, 4, 1), salva.getProximaData());
        assertEquals("Óleo 5W30", salva.getObservacoes());
    }

    @Test
    public void buscarIdInexistenteRetornaNull() {
        assertNull(dao.buscarPorId(999));
    }

    @Test
    public void atualizarRegistro() {
        long id = dao.inserir(nova("Gol 2015", TipoManutencao.PNEUS,
                LocalDate.of(2026, 5, 10), 40000, 900.0, ""));

        Manutencao editada = dao.buscarPorId(id);
        editada.setVeiculo("Gol 2016");
        editada.setCusto(1200.0);
        editada.setKm(41000);
        assertEquals(1, dao.atualizar(editada));

        Manutencao relida = dao.buscarPorId(id);
        assertEquals("Gol 2016", relida.getVeiculo());
        assertEquals(1200.0, relida.getCusto(), 0.001);
        assertEquals(41000, relida.getKm());
        assertEquals(1, dao.listarTodas().size());
    }

    @Test
    public void excluirRegistro() {
        long id = dao.inserir(nova("Civic 2020", TipoManutencao.FREIOS,
                LocalDate.of(2026, 8, 1), 30000, 600.0, ""));
        assertEquals(1, dao.excluir(id));
        assertNull(dao.buscarPorId(id));
        assertEquals(0, dao.excluir(id));
        assertTrue(dao.listarTodas().isEmpty());
    }

    @Test
    public void listarTodasOrdenaPorDataDecrescente() {
        dao.inserir(nova("A", TipoManutencao.PNEUS, LocalDate.of(2026, 3, 1), 1, 1, ""));
        dao.inserir(nova("B", TipoManutencao.PNEUS, LocalDate.of(2026, 9, 1), 1, 1, ""));
        dao.inserir(nova("C", TipoManutencao.PNEUS, LocalDate.of(2026, 6, 1), 1, 1, ""));

        List<Manutencao> lista = dao.listar(null, null, HOJE);
        assertEquals(3, lista.size());
        assertEquals("B", lista.get(0).getVeiculo());
        assertEquals("C", lista.get(1).getVeiculo());
        assertEquals("A", lista.get(2).getVeiculo());
    }

    @Test
    public void buscarPorTermoEmVeiculoTipoEObservacoes() {
        dao.inserir(nova("Honda CG 160", TipoManutencao.PNEUS, LocalDate.of(2026, 1, 1), 1, 1, ""));
        dao.inserir(nova("Gol 2015", TipoManutencao.TROCA_DE_OLEO, LocalDate.of(2026, 2, 1), 1, 1, ""));
        dao.inserir(nova("Civic", TipoManutencao.FREIOS, LocalDate.of(2026, 3, 1), 1, 1, "Oficina do João"));

        assertEquals(1, dao.listar("honda", null, HOJE).size());        // veículo
        assertEquals(1, dao.listar("oleo", null, HOJE).size());         // tipo (sem acento)
        assertEquals("Gol 2015", dao.listar("óleo", null, HOJE).get(0).getVeiculo());
        assertEquals(1, dao.listar("Oficina", null, HOJE).size());      // observações
        assertEquals(0, dao.listar("caminhão", null, HOJE).size());
        assertEquals(3, dao.listar("   ", null, HOJE).size());          // termo vazio = todos
    }

    @Test
    public void filtrarPorStatusCalculado() {
        // Óleo (6 meses): serviço em 01/01/2026 -> prevista 01/07/2026 (vencida em 26/10/2026)
        dao.inserir(nova("Vencida", TipoManutencao.TROCA_DE_OLEO, LocalDate.of(2026, 1, 1), 1, 10, ""));
        // Óleo: serviço em 10/05/2026 -> prevista 10/11/2026 (faltam 15 dias -> vence em breve)
        dao.inserir(nova("Breve", TipoManutencao.TROCA_DE_OLEO, LocalDate.of(2026, 5, 10), 1, 20, ""));
        // Revisão (12 meses): serviço em 01/10/2026 -> prevista 01/10/2027 (em dia)
        dao.inserir(nova("EmDia", TipoManutencao.REVISAO_GERAL, LocalDate.of(2026, 10, 1), 1, 30, ""));

        List<Manutencao> vencidas = dao.listar(null, StatusManutencao.VENCIDA, HOJE);
        assertEquals(1, vencidas.size());
        assertEquals("Vencida", vencidas.get(0).getVeiculo());

        List<Manutencao> breve = dao.listar(null, StatusManutencao.VENCE_EM_BREVE, HOJE);
        assertEquals(1, breve.size());
        assertEquals("Breve", breve.get(0).getVeiculo());

        List<Manutencao> emDia = dao.listar(null, StatusManutencao.EM_DIA, HOJE);
        assertEquals(1, emDia.size());
        assertEquals("EmDia", emDia.get(0).getVeiculo());

        // busca e status combinados
        assertEquals(1, dao.listar("Vencida", StatusManutencao.VENCIDA, HOJE).size());
        assertEquals(0, dao.listar("Vencida", StatusManutencao.EM_DIA, HOJE).size());
    }

    @Test
    public void dadosPersistemAposFecharEReabrirOBanco() {
        dao.inserir(nova("Gol 2015", TipoManutencao.BATERIA, LocalDate.of(2026, 10, 1), 1, 350.0, ""));
        dao.fechar();

        dao = new ManutencaoDAO(contexto);   // nova conexão, mesmo arquivo autolog.db
        List<Manutencao> lista = dao.listarTodas();
        assertEquals(1, lista.size());
        assertEquals("Gol 2015", lista.get(0).getVeiculo());
    }
}
