package br.com.autolog.dao;

import static br.com.autolog.database.DBHelper.COL_CUSTO;
import static br.com.autolog.database.DBHelper.COL_DATA;
import static br.com.autolog.database.DBHelper.COL_ID;
import static br.com.autolog.database.DBHelper.COL_KM;
import static br.com.autolog.database.DBHelper.COL_OBSERVACOES;
import static br.com.autolog.database.DBHelper.COL_PROXIMA_DATA;
import static br.com.autolog.database.DBHelper.COL_TIPO;
import static br.com.autolog.database.DBHelper.COL_VEICULO;
import static br.com.autolog.database.DBHelper.TABELA_MANUTENCAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.com.autolog.database.DBHelper;
import br.com.autolog.model.Manutencao;
import br.com.autolog.model.StatusManutencao;
import br.com.autolog.model.TipoManutencao;
import br.com.autolog.util.DataUtil;

/**
 * DAO: centraliza todas as operações SQL da tabela "manutencao" (CRUD completo).
 * Todas as consultas usam parâmetros ("?") para evitar SQL injection.
 */
public class ManutencaoDAO {

    /** Ordenação padrão: data do serviço decrescente (mais recente primeiro); desempate pelo id. */
    public static final String ORDENACAO_PADRAO = COL_DATA + " DESC, " + COL_ID + " DESC";

    private final DBHelper helper;

    public ManutencaoDAO(Context context) {
        this.helper = new DBHelper(context);
    }

    /** INSERT – retorna o id gerado (ou -1 em caso de erro). */
    public long inserir(Manutencao manutencao) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.insert(TABELA_MANUTENCAO, null, paraContentValues(manutencao));
    }

    /** UPDATE – retorna a quantidade de linhas afetadas. */
    public int atualizar(Manutencao manutencao) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.update(TABELA_MANUTENCAO, paraContentValues(manutencao),
                COL_ID + " = ?", new String[]{String.valueOf(manutencao.getId())});
    }

    /** DELETE – retorna a quantidade de linhas removidas. */
    public int excluir(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(TABELA_MANUTENCAO, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    /** SELECT por id – retorna null se não existir. */
    public Manutencao buscarPorId(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.query(TABELA_MANUTENCAO, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return deCursor(cursor);
            }
            return null;
        }
    }

    /** Lista todas as manutenções com a ordenação padrão. */
    public List<Manutencao> listarTodas() {
        return listar(null, null, LocalDate.now());
    }

    /**
     * SELECT com os filtros da tela principal.
     *
     * @param termoBusca texto a procurar em veículo, observações ou no tipo (pode ser nulo/vazio = todos)
     * @param status     filtro de status calculado pela regra de negócio (nulo = todos)
     * @param hoje       data de referência usada para calcular o status
     */
    public List<Manutencao> listar(String termoBusca, StatusManutencao status, LocalDate hoje) {
        List<String> condicoes = new ArrayList<>();
        List<String> argumentos = new ArrayList<>();

        // Filtro 2 – busca por item específico (veículo, observações ou tipo de serviço)
        if (termoBusca != null && !termoBusca.trim().isEmpty()) {
            String termo = "%" + termoBusca.trim() + "%";
            StringBuilder busca = new StringBuilder();
            busca.append("(").append(COL_VEICULO).append(" LIKE ? OR ")
                    .append(COL_OBSERVACOES).append(" LIKE ?");
            argumentos.add(termo);
            argumentos.add(termo);

            List<TipoManutencao> tipos = TipoManutencao.buscarPorDescricao(termoBusca);
            if (!tipos.isEmpty()) {
                busca.append(" OR ").append(COL_TIPO).append(" IN (");
                for (int i = 0; i < tipos.size(); i++) {
                    busca.append(i == 0 ? "?" : ", ?");
                    argumentos.add(tipos.get(i).name());
                }
                busca.append(")");
            }
            busca.append(")");
            condicoes.add(busca.toString());
        }

        // Filtro 3 – status (regra de negócio): compara a data prevista com a data de hoje
        if (status != null) {
            String hojeIso = DataUtil.paraBanco(hoje);
            String limiteIso = DataUtil.paraBanco(hoje.plusDays(StatusManutencao.DIAS_ALERTA));
            switch (status) {
                case VENCIDA:
                    condicoes.add(COL_PROXIMA_DATA + " < ?");
                    argumentos.add(hojeIso);
                    break;
                case VENCE_EM_BREVE:
                    condicoes.add(COL_PROXIMA_DATA + " >= ? AND " + COL_PROXIMA_DATA + " <= ?");
                    argumentos.add(hojeIso);
                    argumentos.add(limiteIso);
                    break;
                case EM_DIA:
                    condicoes.add(COL_PROXIMA_DATA + " > ?");
                    argumentos.add(limiteIso);
                    break;
            }
        }

        String selecao = condicoes.isEmpty() ? null : String.join(" AND ", condicoes);
        String[] selecaoArgs = argumentos.isEmpty() ? null : argumentos.toArray(new String[0]);

        SQLiteDatabase db = helper.getReadableDatabase();
        List<Manutencao> lista = new ArrayList<>();
        try (Cursor cursor = db.query(TABELA_MANUTENCAO, null, selecao, selecaoArgs,
                null, null, ORDENACAO_PADRAO)) {
            while (cursor.moveToNext()) {
                lista.add(deCursor(cursor));
            }
        }
        return lista;
    }

    /** Fecha a conexão com o banco (chamado no onDestroy da Activity). */
    public void fechar() {
        helper.close();
    }

    // ----- conversões entre o Model e o banco -----

    private ContentValues paraContentValues(Manutencao m) {
        ContentValues valores = new ContentValues();
        valores.put(COL_VEICULO, m.getVeiculo());
        valores.put(COL_TIPO, m.getTipo().name());
        valores.put(COL_DATA, DataUtil.paraBanco(m.getData()));
        valores.put(COL_KM, m.getKm());
        valores.put(COL_CUSTO, m.getCusto());
        valores.put(COL_PROXIMA_DATA, DataUtil.paraBanco(m.getProximaData()));
        valores.put(COL_OBSERVACOES, m.getObservacoes());
        return valores;
    }

    private Manutencao deCursor(Cursor c) {
        Manutencao m = new Manutencao();
        m.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        m.setVeiculo(c.getString(c.getColumnIndexOrThrow(COL_VEICULO)));
        m.setTipo(TipoManutencao.deNome(c.getString(c.getColumnIndexOrThrow(COL_TIPO))));
        m.setData(DataUtil.doBanco(c.getString(c.getColumnIndexOrThrow(COL_DATA))));
        m.setKm(c.getInt(c.getColumnIndexOrThrow(COL_KM)));
        m.setCusto(c.getDouble(c.getColumnIndexOrThrow(COL_CUSTO)));
        m.setProximaData(DataUtil.doBanco(c.getString(c.getColumnIndexOrThrow(COL_PROXIMA_DATA))));
        int indiceObs = c.getColumnIndexOrThrow(COL_OBSERVACOES);
        m.setObservacoes(c.isNull(indiceObs) ? "" : c.getString(indiceObs));
        return m;
    }
}
