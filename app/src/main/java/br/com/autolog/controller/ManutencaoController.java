package br.com.autolog.controller;

import android.content.Context;

import java.time.LocalDate;
import java.util.List;

import br.com.autolog.dao.ManutencaoDAO;
import br.com.autolog.model.Manutencao;
import br.com.autolog.model.Resumo;
import br.com.autolog.model.StatusManutencao;
import br.com.autolog.model.TipoManutencao;

/**
 * Controller: coordena a lógica de negócio entre as telas (Activities) e o acesso aos dados (DAO).
 * As Activities nunca acessam o DAO diretamente.
 */
public class ManutencaoController {

    private final ManutencaoDAO dao;

    public ManutencaoController(Context context) {
        this.dao = new ManutencaoDAO(context);
    }

    /**
     * Valida os dados do formulário, aplica a regra da próxima manutenção e grava no banco.
     * Se o id for informado, atualiza o registro; caso contrário, insere um novo.
     *
     * @return o id do registro gravado, ou -1 se nada foi gravado
     * @throws ValidacaoException quando algum campo é inválido (nada é enviado ao DAO)
     */
    public long salvar(Long id, String veiculo, TipoManutencao tipo, String dataTexto,
                       String kmTexto, String custoTexto, String observacoes) throws ValidacaoException {

        Manutencao manutencao = ValidadorManutencao.validar(id, veiculo, tipo, dataTexto,
                kmTexto, custoTexto, observacoes, LocalDate.now());

        // Regra de negócio: data prevista da próxima manutenção conforme o tipo de serviço
        manutencao.setProximaData(manutencao.getTipo().calcularProximaData(manutencao.getData()));

        if (manutencao.getId() > 0) {
            int linhasAfetadas = dao.atualizar(manutencao);
            return linhasAfetadas > 0 ? manutencao.getId() : -1;
        }
        return dao.inserir(manutencao);
    }

    public Manutencao buscarPorId(long id) {
        return dao.buscarPorId(id);
    }

    /**
     * Lista as manutenções aplicando os filtros da tela principal e a ordenação padrão
     * (data do serviço decrescente).
     *
     * @param termoBusca   texto da busca (nulo/vazio = sem filtro)
     * @param filtroStatus status desejado (nulo = todas)
     */
    public List<Manutencao> listar(String termoBusca, StatusManutencao filtroStatus) {
        return dao.listar(termoBusca, filtroStatus, LocalDate.now());
    }

    public boolean excluir(long id) {
        return dao.excluir(id) > 0;
    }

    /** Regra de negócio de totalização: soma dos custos e contagem de manutenções vencidas. */
    public Resumo calcularResumo(List<Manutencao> itens) {
        LocalDate hoje = LocalDate.now();
        double total = 0;
        int vencidas = 0;
        for (Manutencao m : itens) {
            total += m.getCusto();
            if (m.calcularStatus(hoje) == StatusManutencao.VENCIDA) {
                vencidas++;
            }
        }
        return new Resumo(total, itens.size(), vencidas);
    }

    public void fechar() {
        dao.fechar();
    }
}
