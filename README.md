# Precify 2.0 Java

Precify 2.0 Java e uma reescrita do projeto original de precificacao em Python, agora com foco em programacao orientada a objetos, separacao entre camadas do sistema e uma interface grafica simples baseada em `JOptionPane`.

O objetivo do projeto e ajudar no calculo do preco sugerido de venda de um produto final a partir dos custos do lote e de parametros comerciais. Embora o MVP esteja voltado principalmente para alimentos, a modelagem foi feita com nomes mais genericos para permitir expansao futura para outros tipos de produto.

## Objetivo

O sistema calcula o preco sugerido por unidade de um produto considerando:

- custo dos insumos usados no lote
- custo de embalagem por unidade
- percentual de desperdicio
- percentual de gastos indiretos
- percentual de taxa de venda ou delivery
- percentual de lucro
- arredondamento comercial do preco final

O foco nao e uma interface sofisticada. A proposta desta versao e ter um sistema simples, funcional e didatico, mas com melhor organizacao de codigo do que a versao inicial.

## Funcionalidades atuais

- cadastrar produtos
- cadastrar insumos de cada produto
- listar produtos cadastrados
- consultar os detalhes de um produto
- editar nome, categoria, rendimento, percentuais, embalagem e arredondamento
- adicionar e remover insumos de um produto
- remover produtos
- gerar relatorio em arquivo texto
- salvar e carregar dados automaticamente em arquivo local

## Como o calculo funciona

O sistema trabalha com a ideia de lote. Primeiro ele soma o custo dos insumos usados e o custo de embalagem do lote. Depois aplica os percentuais configurados e, no fim, divide pelo rendimento total.

Fluxo do calculo:

1. Soma o custo de todos os insumos usados no lote.
2. Soma o custo de embalagem do lote.
3. Aplica desperdicio.
4. Aplica gastos indiretos.
5. Aplica taxa de venda ou delivery.
6. Aplica lucro.
7. Divide pelo rendimento do lote para obter o preco sugerido por unidade.
8. Aplica o tipo de arredondamento comercial escolhido.

Em termos simples:

```text
subtotal base do lote
= custo dos insumos + custo de embalagem do lote

custo total sem arredondamento
= subtotal base
  + desperdicio
  + gastos indiretos
  + taxa de venda/delivery
  + lucro

preco sugerido por unidade
= custo total sem arredondamento / rendimento

preco final apresentado
= preco sugerido por unidade com arredondamento comercial
```

## Estrutura do projeto

O projeto esta organizado em pacotes simples:

```text
src/br/com/precify/
  app/
  model/
  repository/
  service/
  ui/
```

Resumo de cada pacote:

- `app`: contem o ponto de entrada e o fluxo principal da aplicacao
- `model`: contem as entidades e enums do dominio
- `repository`: responsavel por salvar e carregar os dados em arquivo
- `service`: contem a regra de negocio de precificacao e geracao de relatorio
- `ui`: utilitarios para interacao com `JOptionPane`

## Principais classes

### Camada de modelo

- `Produto`: entidade principal do sistema. Guarda dados comerciais, rendimento, embalagem, arredondamento e lista de insumos usados no lote.
- `Insumo`: representa um item comprado, com nome, unidade, quantidade da embalagem e preco da embalagem.
- `ItemCusto`: representa quanto de um insumo foi usado no lote e calcula seu custo proporcional.
- `CategoriaProduto`: enum para classificacao simples do produto.
- `TipoArredondamento`: enum com as estrategias de arredondamento comercial disponiveis.

### Camada de servico

- `CalculadoraDePreco`: interface com os metodos principais de calculo.
- `CalculadoraPrecificacao`: implementa a regra de negocio da precificacao.
- `RelatorioService`: gera um relatorio textual com os dados dos produtos.

### Persistencia

- `ProdutoRepository`: interface para operacoes de salvar e carregar.
- `ArquivoProdutoRepository`: implementacao baseada em arquivo texto local.

### Interface

- `JOptionPaneUtils`: centraliza chamadas comuns da interface.
- `PrecifyApp`: tela principal, menu e fluxo de cadastro, consulta e edicao.

## Conceitos de orientacao a objetos aplicados

Mesmo sendo um projeto simples, esta versao foi pensada para explorar conceitos basicos de POO:

- encapsulamento: cada classe controla seus proprios dados e validacoes
- responsabilidade unica: modelo, persistencia, interface e servico estao separados
- abstracao: a regra de precificacao foi isolada em uma interface e em uma classe de servico
- composicao: `Produto` possui varios `ItemCusto`, e cada `ItemCusto` referencia um `Insumo`
- extensibilidade: enums e interfaces deixam o projeto mais facil de evoluir

## Tipos de arredondamento disponiveis

O sistema ja suporta algumas estrategias simples de arredondamento:

- sem arredondamento
- para cima em R$ 0,10
- para cima em R$ 0,50
- para cima em R$ 1,00
- preco final com final `,99`

Isso permite adaptar o valor sugerido para uma apresentacao comercial mais realista.

## Persistencia de dados

Os dados sao salvos automaticamente no diretorio `data/`:

- `data/produtos.txt`: armazenamento dos produtos cadastrados
- `data/relatorio_precify.txt`: relatorio textual gerado pelo sistema

O formato do arquivo foi mantido simples e controlado pela aplicacao. O usuario nao precisa editar esse arquivo manualmente.

## Requisitos

Para executar o projeto localmente, voce precisa ter:

- Java JDK 21 ou superior
- `javac` disponivel no terminal

Se quiser conferir:

```bash
java -version
javac -version
```

## Como executar na sua maquina

### 1. Clonar o repositório

```bash
git clone git@github.com:ju-caju/precify-v2-java.git
cd precify-v2-java
```

### 2. Compilar o projeto

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
```

### 3. Executar a aplicacao

```bash
java -cp out br.com.precify.app.PrecifyApp
```

## Fluxo basico de uso

Ao abrir a aplicacao, o usuario escolhe uma acao no menu principal. O fluxo mais comum e:

1. cadastrar um produto
2. informar nome, categoria, rendimento e unidade
3. informar percentuais comerciais
4. informar custo de embalagem por unidade
5. escolher o tipo de arredondamento
6. cadastrar os insumos usados no lote
7. visualizar o preco sugerido por unidade

Depois disso, o produto pode ser consultado, editado, removido e incluido em relatorio.

## Exemplo conceitual de uso

Imagine um produto com:

- rendimento de 20 unidades
- custo total de insumos de R$ 30,00
- custo de embalagem de R$ 0,50 por unidade
- desperdicio de 5%
- gastos indiretos de 10%
- taxa de venda de 12%
- lucro de 25%

O sistema parte do custo do lote, aplica os percentuais e gera um preco sugerido por unidade, com arredondamento comercial ao final.

## Limitacoes atuais do MVP

Esta versao ainda e propositalmente simples. Alguns pontos que podem evoluir nas proximas iteracoes:

- editar um insumo existente sem precisar remover e cadastrar novamente
- duplicar produtos como base para novas receitas
- separar custo fixo e custo variavel
- gerar ficha tecnica mais detalhada
- suportar outros tipos de produto alem do recorte inicial
- melhorar o formato de persistencia para JSON ou banco de dados no futuro

## Estado atual do projeto

O projeto esta funcional, compilavel e pronto para evolucao incremental. A intencao da versao 2.0 nao e apenas refazer a interface, mas reorganizar a base do sistema para facilitar manutencao, estudo e expansao.
