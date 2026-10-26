package br.com.autolog;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;

import br.com.autolog.controller.ManutencaoController;
import br.com.autolog.controller.ValidacaoException;
import br.com.autolog.model.Manutencao;
import br.com.autolog.model.TipoManutencao;
import br.com.autolog.util.DataUtil;
import br.com.autolog.util.NumeroUtil;

/**
 * Tela de cadastro / edição / exclusão de uma manutenção.
 * <p>
 * Sem extra na Intent: cadastro de um novo registro.
 * Com {@link #EXTRA_ID}: carrega o registro do banco, permite editar e exibe o botão Excluir.
 */
public class CadastroActivity extends AppCompatActivity {

    /** Chave do extra da Intent com o id do registro a editar. */
    public static final String EXTRA_ID = "br.com.autolog.EXTRA_ID";

    private ManutencaoController controller;
    private Long idEdicao;   // null = novo registro

    private MaterialToolbar toolbar;
    private TextInputLayout tilVeiculo;
    private TextInputLayout tilData;
    private TextInputLayout tilKm;
    private TextInputLayout tilCusto;
    private TextInputLayout tilObservacoes;
    private TextInputEditText etVeiculo;
    private TextInputEditText etData;
    private TextInputEditText etKm;
    private TextInputEditText etCusto;
    private TextInputEditText etObservacoes;
    private Spinner spTipo;
    private TextView tvProximaPrevista;
    private MaterialButton btnExcluir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        controller = new ManutencaoController(this);

        toolbar = findViewById(R.id.toolbarCadastro);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilVeiculo = findViewById(R.id.tilVeiculo);
        tilData = findViewById(R.id.tilData);
        tilKm = findViewById(R.id.tilKm);
        tilCusto = findViewById(R.id.tilCusto);
        tilObservacoes = findViewById(R.id.tilObservacoes);
        etVeiculo = findViewById(R.id.etVeiculo);
        etData = findViewById(R.id.etData);
        etKm = findViewById(R.id.etKm);
        etCusto = findViewById(R.id.etCusto);
        etObservacoes = findViewById(R.id.etObservacoes);
        spTipo = findViewById(R.id.spTipo);
        tvProximaPrevista = findViewById(R.id.tvProximaPrevista);
        btnExcluir = findViewById(R.id.btnExcluir);
        MaterialButton btnSalvar = findViewById(R.id.btnSalvar);
        MaterialButton btnCancelar = findViewById(R.id.btnCancelar);

        // Spinner com os tipos de manutenção (o enum fornece a descrição via toString)
        ArrayAdapter<TipoManutencao> adapterTipos = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, TipoManutencao.values());
        adapterTipos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipo.setAdapter(adapterTipos);

        // Dados recebidos da tela principal pela Intent
        long idRecebido = getIntent().getLongExtra(EXTRA_ID, -1);
        if (idRecebido > 0) {
            carregarRegistro(idRecebido);
        } else {
            toolbar.setTitle(R.string.titulo_nova);
            etData.setText(DataUtil.formatar(LocalDate.now()));
        }

        // Calendário para preencher a data (também é possível digitar)
        tilData.setEndIconOnClickListener(v -> abrirCalendario());

        // Atualiza a previsão da próxima manutenção quando o tipo ou a data mudam
        spTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                atualizarProximaPrevista();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        etData.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                atualizarProximaPrevista();
            }
        });

        btnSalvar.setOnClickListener(v -> salvar());
        btnExcluir.setOnClickListener(v -> confirmarExclusao());
        btnCancelar.setOnClickListener(v -> finish());

        atualizarProximaPrevista();
    }

    @Override
    protected void onDestroy() {
        controller.fechar();
        super.onDestroy();
    }

    /** Modo edição: busca o registro no banco e preenche o formulário. */
    private void carregarRegistro(long id) {
        Manutencao m = controller.buscarPorId(id);
        if (m == null) {
            Toast.makeText(this, R.string.msg_nao_encontrado, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        idEdicao = m.getId();
        toolbar.setTitle(R.string.titulo_editar);
        etVeiculo.setText(m.getVeiculo());
        spTipo.setSelection(m.getTipo().ordinal());
        etData.setText(DataUtil.formatar(m.getData()));
        etKm.setText(String.valueOf(m.getKm()));
        etCusto.setText(NumeroUtil.formatarDecimalCampo(m.getCusto()));
        etObservacoes.setText(m.getObservacoes());
        btnExcluir.setVisibility(View.VISIBLE);
    }

    /** Valida (no Controller) e grava; em caso de erro, mostra Toast e foca o campo inválido. */
    private void salvar() {
        limparErros();
        try {
            long id = controller.salvar(idEdicao, texto(etVeiculo),
                    (TipoManutencao) spTipo.getSelectedItem(), texto(etData),
                    texto(etKm), texto(etCusto), texto(etObservacoes));
            if (id <= 0) {
                Toast.makeText(this, R.string.msg_erro_salvar, Toast.LENGTH_SHORT).show();
                return;
            }
            int mensagem = idEdicao == null ? R.string.msg_inserido : R.string.msg_atualizado;
            Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
            finish();
        } catch (ValidacaoException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            focarCampo(e.getCampo(), e.getMessage());
        }
    }

    private void confirmarExclusao() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirmar_exclusao_titulo)
                .setMessage(R.string.confirmar_exclusao_msg)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.excluir, (dialog, which) -> excluir())
                .show();
    }

    private void excluir() {
        if (idEdicao != null && controller.excluir(idEdicao)) {
            Toast.makeText(this, R.string.msg_excluido, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.msg_nao_encontrado, Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirCalendario() {
        LocalDate base = DataUtil.converter(texto(etData));
        if (base == null) {
            base = LocalDate.now();
        }
        DatePickerDialog dialogo = new DatePickerDialog(this,
                (view, ano, mes, dia) -> etData.setText(DataUtil.formatar(LocalDate.of(ano, mes + 1, dia))),
                base.getYear(), base.getMonthValue() - 1, base.getDayOfMonth());
        dialogo.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialogo.show();
    }

    /** Mostra ao usuário a regra de negócio aplicada: data prevista da próxima manutenção. */
    private void atualizarProximaPrevista() {
        TipoManutencao tipo = (TipoManutencao) spTipo.getSelectedItem();
        LocalDate data = DataUtil.converter(texto(etData));
        if (tipo == null || data == null) {
            tvProximaPrevista.setText(getString(R.string.proxima_prevista,
                    getString(R.string.proxima_indefinida)));
            return;
        }
        LocalDate proxima = tipo.calcularProximaData(data);
        tvProximaPrevista.setText(getString(R.string.proxima_prevista_detalhe,
                DataUtil.formatar(proxima), tipo.getIntervaloMeses()));
    }

    /** Foca automaticamente o campo inválido e exibe a mensagem junto ao campo. */
    private void focarCampo(ValidacaoException.Campo campo, String mensagem) {
        switch (campo) {
            case VEICULO:
                tilVeiculo.setError(mensagem);
                etVeiculo.requestFocus();
                break;
            case TIPO:
                spTipo.requestFocus();
                spTipo.performClick();
                break;
            case DATA:
                tilData.setError(mensagem);
                etData.requestFocus();
                break;
            case KM:
                tilKm.setError(mensagem);
                etKm.requestFocus();
                break;
            case CUSTO:
                tilCusto.setError(mensagem);
                etCusto.requestFocus();
                break;
            case OBSERVACOES:
                tilObservacoes.setError(mensagem);
                etObservacoes.requestFocus();
                break;
        }
    }

    private void limparErros() {
        tilVeiculo.setError(null);
        tilData.setError(null);
        tilKm.setError(null);
        tilCusto.setError(null);
        tilObservacoes.setError(null);
    }

    private static String texto(EditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString();
    }
}
