# Precify 2.0 Java

Versao 2.0 do Precify, reescrita em Java com foco em programacao orientada a objetos, interface grafica simples via `JOptionPane` e persistencia em arquivo texto.

## Escopo do MVP

- Cadastro de produtos e insumos
- Calculo do preco sugerido por unidade
- Percentual de gastos indiretos
- Percentual de desperdicio
- Percentual de taxa de venda/delivery
- Percentual de lucro
- Custo de embalagem por unidade
- Arredondamento comercial do preco final
- Edicao e remocao de produtos
- Persistencia local em arquivo
- Geracao de relatorio `.txt`

## Estrutura

```text
src/br/com/precify/
  app/
  model/
  repository/
  service/
  ui/
```

## Compilar

```bash
cd /home/ju/precify-v2-java
mkdir -p out
javac -d out $(find src -name "*.java")
```

## Executar

```bash
cd /home/ju/precify-v2-java
java -cp out br.com.precify.app.PrecifyApp
```

Os dados ficam em `data/produtos.txt` e o relatorio em `data/relatorio_precify.txt`.
