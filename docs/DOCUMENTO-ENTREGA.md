# Documento de Entrega – App 02 (Persistência Local com SQLite)

## 1. Integrantes do grupo

| Nome | GRR |
|------|-----|
| *(preencher)* | *(preencher)* |
| *(preencher)* | *(preencher)* |
| *(preencher)* | *(preencher)* |

## 2. Repositório no GitHub

https://github.com/joaozanini/mobile-app02

## 3. Tema e explicação do aplicativo

O **AutoLog** é um aplicativo de controle de manutenção veicular. O usuário registra cada serviço feito no
carro ou na moto (troca de óleo, pneus, freios, revisão, bateria etc.) informando o veículo, o tipo de
serviço, a data, a quilometragem, o custo e observações. Todos os registros ficam gravados em um banco
SQLite local. A partir deles, o app calcula automaticamente a data prevista da próxima manutenção de cada
serviço, classifica cada registro como *Em dia*, *Vence em breve* ou *Vencida*, e mostra o total gasto
com o veículo, ajudando o dono a não perder os prazos de manutenção e a acompanhar seus gastos.

## 4. Regras de negócio e ordenação

**Regra 1 – Data prevista da próxima manutenção.** Cada tipo de serviço tem um intervalo recomendado
(enum `TipoManutencao`). Ao salvar, o Controller calcula `proxima_data = data do serviço + intervalo` e
grava no banco:

| Tipo de serviço              | Intervalo |
|------------------------------|-----------|
| Troca de óleo                | 6 meses   |
| Troca de filtros             | 6 meses   |
| Alinhamento e balanceamento  | 6 meses   |
| Freios (pastilhas/discos)    | 12 meses  |
| Pneus                        | 12 meses  |
| Revisão geral                | 12 meses  |
| Bateria                      | 24 meses  |
| Outro serviço                | 12 meses  |

**Regra 2 – Status automático** (enum `StatusManutencao`), calculado comparando a data prevista com a
data de hoje:

- **Vencida**: a data prevista já passou;
- **Vence em breve**: faltam até 30 dias para a data prevista;
- **Em dia**: faltam mais de 30 dias.

**Regra 3 – Totalização.** O cartão no topo da tela principal mostra o **total gasto** (soma dos custos
dos registros exibidos), a **quantidade de registros** e quantas manutenções estão **vencidas**.

**Ordenação.** A lista é sempre exibida por **data do serviço decrescente** (a manutenção mais recente
aparece primeiro); em caso de empate, pelo id decrescente. A ordenação é feita no SQL
(`ORDER BY data DESC, id DESC`), o que é possível porque as datas são gravadas no formato ISO
(`yyyy-MM-dd`).

## 5. Filtros de dados disponíveis

1. **Listar todos** – estado inicial da tela (busca vazia e status "Todas"); o item de menu
   *Limpar filtros* restaura esse estado.
2. **Buscar por item específico** – campo de busca que filtra, enquanto o usuário digita, pelo nome do
   veículo, pelas observações ou pelo tipo de serviço (ex.: "oleo" encontra "Troca de óleo", sem exigir
   acento). No SQL: `veiculo LIKE ? OR observacoes LIKE ? OR tipo IN (...)`.
3. **Filtrar por status** (filtro escolhido pelo grupo) – Spinner com *Todas*, *Vencidas*, *Vence em breve*
   e *Em dia*. O filtro compara a coluna `proxima_data` com a data de hoje diretamente no SQL
   (ex.: vencidas = `proxima_data < hoje`).

A busca e o filtro de status podem ser combinados.

## 6. Estrutura do projeto no Android Studio

Projeto Android (Java), módulo `app`, `minSdk 29`, `compileSdk/targetSdk 34`, pacote `br.com.autolog`.

| Camada | Classe(s) | Papel |
|--------|-----------|-------|
| Model | `model/Manutencao`, `model/TipoManutencao`, `model/StatusManutencao`, `model/Resumo` | Estrutura dos dados e regras de cálculo (intervalo e status) |
| DBHelper | `database/DBHelper` | `SQLiteOpenHelper` que cria a tabela `manutencao` no banco `autolog.db` e controla a versão |
| DAO | `dao/ManutencaoDAO` | Todas as operações SQL: `inserir`, `atualizar`, `excluir`, `buscarPorId`, `listar` (com filtros e ordenação) |
| Controller | `controller/ManutencaoController`, `controller/ValidadorManutencao`, `controller/ValidacaoException` | Validação dos dados antes do DAO, aplicação das regras de negócio e coordenação entre telas e dados |
| Views | `MainActivity`, `CadastroActivity`, `adapter/ManutencaoAdapter` + layouts XML | Telas, ListView e interação com o usuário |
| Util | `util/DataUtil`, `util/NumeroUtil` | Conversão de datas (dd/mm/aaaa ⇄ ISO) e de números/moeda |

Tabela `manutencao`: `id` (PK autoincremento), `veiculo`, `tipo`, `data`, `km`, `custo`, `proxima_data`,
`observacoes`.

## 7. Telas desenvolvidas

**Tela principal (`MainActivity`, `activity_main.xml`)**

- Barra superior com o nome do app e menu (*Limpar filtros* e *Regras de manutenção*, que exibe as regras ao usuário).
- Cartão de resumo: total gasto, quantidade de registros e manutenções vencidas.
- Campo de busca e Spinner de status (filtros).
- `ListView` com um cartão por manutenção: tipo de serviço, veículo, data e quilometragem, custo,
  etiqueta colorida com o status e a data prevista da próxima manutenção.
- Mensagem de lista vazia quando não há registros.
- Botão flutuante **+** para cadastrar uma nova manutenção.

**Tela de cadastro / edição / exclusão (`CadastroActivity`, `activity_cadastro.xml`)**

- Formulário: veículo, tipo (Spinner), data do serviço (digitação ou calendário), quilometragem, custo e observações.
- Caixa informativa que mostra, em tempo real, a data prevista da próxima manutenção conforme o tipo e a data escolhidos.
- Botões **Salvar**, **Excluir** (apenas no modo edição, com diálogo de confirmação) e **Cancelar**.
- Validações: campos obrigatórios, veículo entre 2 e 40 caracteres, observações até 200 caracteres,
  quilometragem e custo não negativos e dentro dos limites, data válida no formato dd/mm/aaaa e não futura.
  Erros são informados por `Toast`, pela mensagem junto ao campo e o campo inválido recebe o foco.

## 8. Fluxo de navegação e ações

1. O app abre na **tela principal** e carrega do SQLite a lista de manutenções (ordenada por data).
2. Tocar em **+** abre a **tela de cadastro** em branco (`Intent` sem extras).
3. Tocar em um **item da lista** abre a mesma tela em **modo edição**: a `MainActivity` envia o `id` do
   registro pela `Intent` (`EXTRA_ID`), a `CadastroActivity` busca o registro pelo Controller/DAO e preenche
   o formulário; o botão **Excluir** fica visível.
4. **Salvar**: a Activity envia os textos ao Controller, que valida (Requisito 7), calcula a próxima
   manutenção e chama o DAO (`insert` ou `update`). Em sucesso, mostra um `Toast` e volta para a tela
   principal, que recarrega a lista no `onResume`. Em erro, mostra a mensagem e foca o campo inválido.
5. **Excluir**: pede confirmação, chama o DAO (`delete`), mostra `Toast` e volta para a lista.
6. **Cancelar** ou a seta de voltar retornam à tela principal sem alterar nada.
7. Na tela principal, digitar na busca ou trocar o status no Spinner refaz a consulta no banco e
   atualiza a lista e o resumo.

## 9. Referências e ferramentas utilizadas

- Documentação oficial do Android (SQLiteOpenHelper, ListView, Intents) e biblioteca Material Components.
- Ícones vetoriais do conjunto Material Icons (licença Apache 2.0).
- O código deste projeto foi escrito com o auxílio de uma ferramenta de IA generativa (Claude), revisado e
  testado pelo grupo. Os testes automatizados (JUnit e Robolectric) estão em `app/src/test`.
