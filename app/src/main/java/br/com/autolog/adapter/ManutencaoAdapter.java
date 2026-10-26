package br.com.autolog.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.List;

import br.com.autolog.R;
import br.com.autolog.model.Manutencao;
import br.com.autolog.model.StatusManutencao;
import br.com.autolog.util.DataUtil;
import br.com.autolog.util.NumeroUtil;

/**
 * Adapter da ListView da tela principal: converte cada {@link Manutencao} em um item visual
 * (layout item_manutencao.xml), usando o padrão ViewHolder para reaproveitar as views.
 */
public class ManutencaoAdapter extends ArrayAdapter<Manutencao> {

    public ManutencaoAdapter(@NonNull Context context, @NonNull List<Manutencao> itens) {
        super(context, 0, itens);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_manutencao, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Manutencao manutencao = getItem(position);
        if (manutencao != null) {
            holder.preencher(manutencao, getContext());
        }
        return convertView;
    }

    private static class ViewHolder {
        final TextView tvTipo;
        final TextView tvVeiculo;
        final TextView tvDataKm;
        final TextView tvCusto;
        final TextView tvStatus;
        final TextView tvProxima;

        ViewHolder(View view) {
            tvTipo = view.findViewById(R.id.tvTipo);
            tvVeiculo = view.findViewById(R.id.tvVeiculo);
            tvDataKm = view.findViewById(R.id.tvDataKm);
            tvCusto = view.findViewById(R.id.tvCusto);
            tvStatus = view.findViewById(R.id.tvStatus);
            tvProxima = view.findViewById(R.id.tvProxima);
        }

        void preencher(Manutencao m, Context contexto) {
            tvTipo.setText(m.getTipo().getDescricao());
            tvVeiculo.setText(m.getVeiculo());
            tvDataKm.setText(contexto.getString(R.string.data_km_formato,
                    DataUtil.formatar(m.getData()), NumeroUtil.formatarInteiro(m.getKm())));
            tvCusto.setText(NumeroUtil.formatarMoeda(m.getCusto()));
            tvProxima.setText(contexto.getString(R.string.proxima_formato,
                    DataUtil.formatar(m.getProximaData())));

            // Cores conforme o status calculado pela regra de negócio
            StatusManutencao status = m.getStatus();
            int corTexto;
            int corFundo;
            switch (status) {
                case VENCIDA:
                    corTexto = R.color.vermelho;
                    corFundo = R.color.vermelho_claro;
                    break;
                case VENCE_EM_BREVE:
                    corTexto = R.color.laranja;
                    corFundo = R.color.laranja_claro;
                    break;
                default:
                    corTexto = R.color.verde;
                    corFundo = R.color.verde_claro;
                    break;
            }
            tvStatus.setText(status.getDescricao());
            tvStatus.setTextColor(ContextCompat.getColor(contexto, corTexto));
            tvStatus.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(contexto, corFundo)));
            tvProxima.setTextColor(ContextCompat.getColor(contexto, corTexto));
        }
    }
}
