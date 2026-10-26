package br.com.autolog;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import br.com.autolog.adapter.ManutencaoAdapter;
import br.com.autolog.controller.ManutencaoController;
import br.com.autolog.model.Manutencao;
import br.com.autolog.model.Resumo;
import br.com.autolog.model.StatusManutencao;
import br.com.autolog.model.TipoManutencao;
import br.com.autolog.util.NumeroUtil;

/**
 * Tela principal: exibe as manutenções gravadas no SQLite em uma ListView, com resumo
 * (total gasto / registros / vencidas), busca, filtro por status e botão para novo registro.
 * Ao tocar em um item, abre a tela de edição carregando o registro selecionado.
 */
public class MainActivity extends AppCompatActivity {

    private ManutencaoController controller;
    private ManutencaoAdapter adapter;

    private EditText etBusca;
    private Spinner spFiltroStatus;
    private TextView tvTotalGasto;
    private TextView tvQuantidade;
    private TextView tvVencidas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        controller = new ManutencaoController(this);

        etBusca = findViewById(R.id.etBusca);
        spFiltroStatus = findViewById(R.id.spFiltroStatus);
        tvTotalGasto = findViewById(R.id.tvTotalGasto);
        tvQuantidade = findViewById(R.id.tvQuantidade);
        tvVencidas = findViewById(R.id.tvVencidas);
        ListView listaManutencoes = findViewById(R.id.listaManutencoes);
        TextView tvVazio = findViewById(R.id.tvVazio);
        FloatingActionButton fabNova = findViewById(R.id.fabNova);

        adapter = new ManutencaoAdapter(this, new ArrayList<>());
        listaManutencoes.setAdapter(adapter);
        listaManutencoes.setEmptyView(tvVazio);

        // Requisito 4: ao tocar em um item, navega para a tela de edição com o id do registro
        listaManutencoes.setOnItemClickListener((parent, view, position, id) -> {
            Manutencao selecionada = adapter.getItem(position);
            if (selecionada != null) {
                abrirCadastro(selecionada.getId());
            }
        });

        // Botão "+" abre a tela de cadastro de uma nova manutenção
        fabNova.setOnClickListener(v -> abrirCadastro(null));

        // Filtro 2: busca por item específico (atualiza a lista enquanto o usuário digita)
        etBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                carregarLista();
            }
        });

        // Filtro 3: status calculado pela regra de negócio
        spFiltroStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                carregarLista();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega do banco sempre que a tela volta a ficar visível (após salvar/excluir)
        carregarLista();
    }

    @Override
    protected void onDestroy() {
        controller.fechar();
        super.onDestroy();
    }

    /** Navega para a tela de cadastro; com id, abre em modo de edição (passagem de dados via Intent). */
    private void abrirCadastro(Long id) {
        Intent intent = new Intent(this, CadastroActivity.class);
        if (id != null) {
            intent.putExtra(CadastroActivity.EXTRA_ID, id);
        }
        startActivity(intent);
    }

    /** Consulta o banco com os filtros atuais e atualiza a ListView e o resumo. */
    private void carregarLista() {
        String termo = etBusca.getText() == null ? "" : etBusca.getText().toString();
        List<Manutencao> itens = controller.listar(termo, statusSelecionado());

        adapter.clear();
        adapter.addAll(itens);
        adapter.notifyDataSetChanged();

        atualizarResumo(itens);
    }

    /** Converte a posição do Spinner (ver arrays.xml) no status do filtro; null = todas. */
    private StatusManutencao statusSelecionado() {
        switch (spFiltroStatus.getSelectedItemPosition()) {
            case 1:
                return StatusManutencao.VENCIDA;
            case 2:
                return StatusManutencao.VENCE_EM_BREVE;
            case 3:
                return StatusManutencao.EM_DIA;
            default:
                return null;
        }
    }

    private void atualizarResumo(List<Manutencao> itens) {
        Resumo resumo = controller.calcularResumo(itens);
        tvTotalGasto.setText(NumeroUtil.formatarMoeda(resumo.getTotalGasto()));
        tvQuantidade.setText(String.valueOf(resumo.getQuantidade()));
        tvVencidas.setText(String.valueOf(resumo.getVencidas()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.acao_limpar_filtros) {
            // Filtro 1: listar todos (limpa a busca e o status)
            etBusca.setText("");
            spFiltroStatus.setSelection(0);
            carregarLista();
            return true;
        }
        if (id == R.id.acao_regras) {
            mostrarRegras();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Exibe as regras de negócio (intervalos por tipo e critérios de status) para o usuário. */
    private void mostrarRegras() {
        StringBuilder texto = new StringBuilder(getString(R.string.regras_intervalos));
        for (TipoManutencao tipo : TipoManutencao.values()) {
            texto.append(getString(R.string.regras_item, tipo.getDescricao(), tipo.getIntervaloMeses()));
        }
        texto.append(getString(R.string.regras_status, StatusManutencao.DIAS_ALERTA));

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.menu_regras)
                .setMessage(texto.toString())
                .setPositiveButton(R.string.ok, null)
                .show();
    }
}
