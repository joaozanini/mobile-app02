# Documento de Entrega – App 02 (Persistência Local com SQLite)

## 1. Integrantes do grupo

| Nome | GRR |
|------|-----|
| Felipe Matheus Laskos | GRR20241326 |
| Lucas Sarnacki Guiraud | GRR20241773 |
| João Vitor Zanini Pedro | GRR20242373 |

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
