package br.com.autolog.controller;

import java.time.LocalDate;

import br.com.autolog.controller.ValidacaoException.Campo;
import br.com.autolog.model.Manutencao;
import br.com.autolog.model.TipoManutencao;
import br.com.autolog.util.DataUtil;
import br.com.autolog.util.NumeroUtil;

/**
 * Valida os dados digitados no formulário ANTES de qualquer chamada ao DAO (Requisito 7)
 * e monta o objeto {@link Manutencao} já com os valores convertidos.
 */
public final class ValidadorManutencao {

    public static final int VEICULO_MIN = 2;
    public static final int VEICULO_MAX = 40;
    public static final int OBSERVACOES_MAX = 200;
    public static final int KM_MAX = 9_999_999;
    public static final double CUSTO_MAX = 999_999.99;

    private ValidadorManutencao() {
    }

    /**
     * @param id           id do registro em edição (nulo ou zero para um novo registro)
     * @param hoje         data de referência (a data do serviço não pode ser futura)
     * @return a manutenção validada e pronta para ser gravada
     * @throws ValidacaoException descreve o primeiro campo inválido encontrado
     */
    public static Manutencao validar(Long id, String veiculo, TipoManutencao tipo, String dataTexto,
                                     String kmTexto, String custoTexto, String observacoes,
                                     LocalDate hoje) throws ValidacaoException {

        // Veículo: obrigatório, entre 2 e 40 caracteres
        String veiculoLimpo = veiculo == null ? "" : veiculo.trim();
        if (veiculoLimpo.isEmpty()) {
            throw new ValidacaoException(Campo.VEICULO, "Informe o veículo.");
        }
        if (veiculoLimpo.length() < VEICULO_MIN) {
            throw new ValidacaoException(Campo.VEICULO,
                    "O veículo deve ter pelo menos " + VEICULO_MIN + " caracteres.");
        }
        if (veiculoLimpo.length() > VEICULO_MAX) {
            throw new ValidacaoException(Campo.VEICULO,
                    "O veículo deve ter no máximo " + VEICULO_MAX + " caracteres.");
        }

        // Tipo: obrigatório
        if (tipo == null) {
            throw new ValidacaoException(Campo.TIPO, "Selecione o tipo de manutenção.");
        }

        // Data: obrigatória, válida (dd/mm/aaaa) e não futura
        String dataLimpa = dataTexto == null ? "" : dataTexto.trim();
        if (dataLimpa.isEmpty()) {
            throw new ValidacaoException(Campo.DATA, "Informe a data do serviço.");
        }
        LocalDate data = DataUtil.converter(dataLimpa);
        if (data == null) {
            throw new ValidacaoException(Campo.DATA, "Data inválida. Use o formato dd/mm/aaaa.");
        }
        if (data.isAfter(hoje)) {
            throw new ValidacaoException(Campo.DATA, "A data do serviço não pode ser futura.");
        }

        // Quilometragem: obrigatória, número inteiro, não negativa e dentro do limite
        String kmLimpo = kmTexto == null ? "" : kmTexto.trim();
        if (kmLimpo.isEmpty()) {
            throw new ValidacaoException(Campo.KM, "Informe a quilometragem do veículo.");
        }
        Integer km = NumeroUtil.converterInteiro(kmLimpo);
        if (km == null) {
            throw new ValidacaoException(Campo.KM, "Quilometragem inválida. Digite apenas números.");
        }
        if (km < 0) {
            throw new ValidacaoException(Campo.KM, "A quilometragem não pode ser negativa.");
        }
        if (km > KM_MAX) {
            throw new ValidacaoException(Campo.KM, "A quilometragem informada é maior que o limite permitido.");
        }

        // Custo: obrigatório, número, não negativo e dentro do limite
        String custoLimpo = custoTexto == null ? "" : custoTexto.trim();
        if (custoLimpo.isEmpty()) {
            throw new ValidacaoException(Campo.CUSTO, "Informe o custo do serviço.");
        }
        Double custo = NumeroUtil.converterDecimal(custoLimpo);
        if (custo == null) {
            throw new ValidacaoException(Campo.CUSTO, "Custo inválido. Use números, por exemplo 150,00.");
        }
        if (custo < 0) {
            throw new ValidacaoException(Campo.CUSTO, "O custo não pode ser negativo.");
        }
        if (custo > CUSTO_MAX) {
            throw new ValidacaoException(Campo.CUSTO, "O custo informado é maior que o limite permitido.");
        }

        // Observações: opcional, até 200 caracteres
        String obsLimpa = observacoes == null ? "" : observacoes.trim();
        if (obsLimpa.length() > OBSERVACOES_MAX) {
            throw new ValidacaoException(Campo.OBSERVACOES,
                    "As observações devem ter no máximo " + OBSERVACOES_MAX + " caracteres.");
        }

        Manutencao manutencao = new Manutencao();
        manutencao.setId(id == null ? 0 : id);
        manutencao.setVeiculo(veiculoLimpo);
        manutencao.setTipo(tipo);
        manutencao.setData(data);
        manutencao.setKm(km);
        manutencao.setCusto(custo);
        manutencao.setObservacoes(obsLimpa);
        return manutencao;
    }
}
