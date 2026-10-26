package br.com.autolog.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DBHelper: responsável por criar o banco SQLite e controlar a sua versão.
 * <p>
 * Os dados ficam no arquivo "autolog.db", no armazenamento interno do aplicativo,
 * e por isso sobrevivem ao fechamento do app e à reinicialização do dispositivo.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String NOME_BANCO = "autolog.db";
    public static final int VERSAO_BANCO = 1;

    public static final String TABELA_MANUTENCAO = "manutencao";
    public static final String COL_ID = "id";
    public static final String COL_VEICULO = "veiculo";
    public static final String COL_TIPO = "tipo";
    public static final String COL_DATA = "data";
    public static final String COL_KM = "km";
    public static final String COL_CUSTO = "custo";
    public static final String COL_PROXIMA_DATA = "proxima_data";
    public static final String COL_OBSERVACOES = "observacoes";

    // As datas são gravadas no formato ISO (yyyy-MM-dd) para que a ordenação e as
    // comparações possam ser feitas diretamente no SQL.
    private static final String SQL_CRIAR_TABELA =
            "CREATE TABLE " + TABELA_MANUTENCAO + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_VEICULO + " TEXT NOT NULL, "
                    + COL_TIPO + " TEXT NOT NULL, "
                    + COL_DATA + " TEXT NOT NULL, "
                    + COL_KM + " INTEGER NOT NULL, "
                    + COL_CUSTO + " REAL NOT NULL, "
                    + COL_PROXIMA_DATA + " TEXT NOT NULL, "
                    + COL_OBSERVACOES + " TEXT"
                    + ")";

    public DBHelper(Context context) {
        super(context, NOME_BANCO, null, VERSAO_BANCO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CRIAR_TABELA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versaoAntiga, int versaoNova) {
        // Estratégia simples para evolução do esquema: recria a tabela.
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_MANUTENCAO);
        onCreate(db);
    }
}
