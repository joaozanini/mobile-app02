# AutoLog – Controle de Manutenção Veicular

Aplicativo Android nativo (Java) desenvolvido para o **App 02 – Aplicativo com Persistência Local (SQLite)**.

O AutoLog registra as manutenções feitas em um ou mais veículos (troca de óleo, pneus, freios, revisão etc.),
guarda tudo em um banco **SQLite** local e, a partir dessas informações, calcula automaticamente **quando a
próxima manutenção deve acontecer**, mostrando o status de cada registro (*Em dia*, *Vence em breve* ou
*Vencida*) e o **total gasto** com o veículo.

## Funcionalidades

- **CRUD completo em SQLite**: inserir, listar, buscar, editar e excluir manutenções.
- **Duas telas** com navegação por `Intent` e passagem de dados entre Activities.
- **ListView** com adapter próprio; ao tocar em um item, abre a tela de edição com o registro carregado e opção de exclusão.
- **Regras de negócio**:
  - *Próxima manutenção*: calculada somando o intervalo recomendado do tipo de serviço à data do serviço
    (óleo/filtros/alinhamento: 6 meses; freios/pneus/revisão/outros: 12 meses; bateria: 24 meses).
  - *Status automático*: **Vencida** (data prevista já passou), **Vence em breve** (faltam até 30 dias) ou **Em dia**.
  - *Totalização*: a tela principal mostra o total gasto (soma dos custos), o número de registros e quantas manutenções estão vencidas.
- **Ordenação**: lista por data do serviço decrescente (mais recente primeiro), desempate pelo id.
- **Três filtros**: listar todas, buscar por item específico (veículo, tipo de serviço ou observação) e filtrar por status.
- **Validação antes de gravar**: campos obrigatórios, tamanho dos textos, números negativos, data inválida/mal formatada ou futura.
  Mensagens via `Toast`, erro junto ao campo e foco automático no campo inválido.
- **Persistência real**: os dados ficam no arquivo `autolog.db` do app e sobrevivem ao fechamento do app e ao reinício do aparelho.

## Estrutura do projeto

```
app/src/main/java/br/com/autolog/
├── MainActivity.java              # Tela principal (lista, resumo, filtros, botão +)
├── CadastroActivity.java          # Tela de cadastro / edição / exclusão
├── model/
│   ├── Manutencao.java            # Entidade (linha da tabela "manutencao")
│   ├── TipoManutencao.java        # Enum com os tipos e o intervalo em meses de cada um
│   ├── StatusManutencao.java      # Enum com a regra de cálculo do status
│   └── Resumo.java                # Totais exibidos na tela principal
├── database/
│   └── DBHelper.java              # SQLiteOpenHelper: cria o banco e controla a versão
├── dao/
│   └── ManutencaoDAO.java         # Todas as operações SQL (insert, update, delete, query)
├── controller/
│   ├── ManutencaoController.java  # Liga as telas ao DAO e aplica as regras de negócio
│   ├── ValidadorManutencao.java   # Validação dos dados do formulário (antes do DAO)
│   └── ValidacaoException.java    # Erro de validação com o campo a ser focado
├── adapter/
│   └── ManutencaoAdapter.java     # Adapter da ListView (padrão ViewHolder)
└── util/
    ├── DataUtil.java              # dd/mm/aaaa <-> LocalDate <-> formato ISO do banco
    └── NumeroUtil.java            # Moeda e números no padrão brasileiro
```

Recursos em `app/src/main/res`: `layout/activity_main.xml`, `layout/activity_cadastro.xml`,
`layout/item_manutencao.xml`, `menu/menu_main.xml`, ícones vetoriais em `drawable/` e o ícone adaptativo
do app em `mipmap-anydpi/`.

## Banco de dados

Tabela `manutencao` (banco `autolog.db`, versão 1):

| Coluna         | Tipo    | Descrição                                               |
|----------------|---------|---------------------------------------------------------|
| id             | INTEGER | chave primária (autoincremento)                         |
| veiculo        | TEXT    | nome/modelo do veículo                                  |
| tipo           | TEXT    | nome do enum `TipoManutencao`                           |
| data           | TEXT    | data do serviço em ISO (`yyyy-MM-dd`) para ordenar/comparar |
| km             | INTEGER | quilometragem no dia do serviço                         |
| custo          | REAL    | valor pago (R$)                                         |
| proxima_data   | TEXT    | data prevista da próxima manutenção (regra de negócio)  |
| observacoes    | TEXT    | campo opcional                                          |

## Como executar

1. Abra a pasta do repositório no **Android Studio** (File > Open).
2. Aguarde a sincronização do Gradle (o projeto usa AGP 8.7.3, Gradle 8.9 e JDK 17+, já embutido no Android Studio).
3. Crie/escolha um emulador ou conecte um aparelho com **Android 10 (API 29)** ou superior e clique em **Run**.

Configuração do módulo: `minSdk = 29`, `targetSdk = 34`, `compileSdk = 34`.

## Testes automatizados

Os testes rodam na JVM, sem emulador:

```bash
./gradlew testDebugUnitTest
```

- `ManutencaoDAOTest` (Robolectric): CRUD completo no SQLite real, ordenação, busca, filtros por status e persistência após reabrir o banco.
- `ValidadorManutencaoTest`: todas as regras de validação (Requisito 7).
- `RegrasDeNegocioTest`: cálculo da próxima manutenção e do status.
- `DataUtilTest` e `NumeroUtilTest`: conversões de data e de números.

## Documento de entrega

O documento com os itens solicitados no enunciado está em [docs/DOCUMENTO-ENTREGA.md](docs/DOCUMENTO-ENTREGA.md).
