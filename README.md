# Cadastro Cliente / Produto / Venda

Projeto de estudo (curso EBAC, módulo 30) para praticar acesso a banco de dados com **JDBC puro** em Java, aplicando o padrão **DAO** (Data Access Object) e **annotations** de mapeamento sobre um schema PostgreSQL com relacionamento N:N entre vendas e produtos.

## Sumário

- [Visão geral](#visão-geral)
- [Tecnologias](#tecnologias)
- [Modelo de dados](#modelo-de-dados)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Arquitetura (camadas)](#arquitetura-camadas)
- [Configuração do banco de dados](#configuração-do-banco-de-dados)
- [Como rodar](#como-rodar)
- [Testes](#testes)
- [Observações e limitações conhecidas](#observações-e-limitações-conhecidas)

## Visão geral

O projeto trabalha com quatro entidades:

- **Cliente** — cadastro de clientes (nome, CPF, telefone, endereço, e-mail, data de nascimento e quantidade de compras).
- **Produto** — cadastro de produtos (código, nome, descrição, marca, valor de venda, valor de compra e se está disponível).
- **Venda** — uma venda para um cliente, com código, data, status (`INICIADA`, `CONCLUIDA`, `CANCELADA`) e valor total.
- **ProdutoQuantidade** — cada item da venda: o produto, a quantidade e o valor total do item. Resolve o relacionamento N:N entre `Venda` e `Produto`.

A própria `Venda` concentra a regra de negócio: `adicionarProduto` soma no item existente quando o produto já está na venda, `removerProduto` diminui ou retira o item, e o valor total é recalculado a cada mudança. Uma venda `CONCLUIDA` não pode mais ser alterada.

Cada entidade é uma classe em `model`. `Cliente`, `Produto` e `Venda` têm uma interface DAO e uma implementação JDBC em `dao`; os itens (`ProdutoQuantidade`) são gravados e lidos pelo `VendaDaoImpl`, junto com a venda.

## Tecnologias

- **Java 25** (JRE_CONTAINER do `.classpath`)
- **JDBC** (sem ORM/framework) via driver `postgresql-42.7.13`
- **PostgreSQL**
- **JUnit 5** (Jupiter) para os testes
- **Mockito 5** (mock maker inline) para os testes unitários dos DAOs
- Projeto **Eclipse** (`.project` / `.classpath`), sem Maven/Gradle — as dependências são referenciadas diretamente como `.jar` no classpath

## Modelo de dados

```sql
-- Cliente
CREATE TABLE tb_cliente (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf BIGINT NOT NULL UNIQUE,
    tel BIGINT,
    endereco VARCHAR(100),
    numero INT,
    cidade VARCHAR(50),
    estado VARCHAR(50),
    email VARCHAR(150),
    qtd_compras INT NOT NULL DEFAULT 0,
    data_nascimento DATE
);

-- Produto
CREATE TABLE tb_produto (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(255),
    valor NUMERIC(10,2) NOT NULL,
    valor_compra NUMERIC(10,2),
    disponivel BOOLEAN NOT NULL DEFAULT TRUE,
    marca VARCHAR(50)
);

-- Venda
CREATE TABLE tb_venda (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    id_cliente_fk BIGINT NOT NULL REFERENCES tb_cliente(id) ON DELETE RESTRICT,
    valor_total NUMERIC(10,2) NOT NULL DEFAULT 0,
    data_venda TIMESTAMPTZ NOT NULL,
    status_venda VARCHAR(20) NOT NULL
);

-- Itens da venda: resolve o N:N entre venda e produto
CREATE TABLE tb_produto_quantidade (
    id BIGSERIAL PRIMARY KEY,
    id_venda_fk BIGINT NOT NULL REFERENCES tb_venda(id) ON DELETE CASCADE,
    id_produto_fk BIGINT NOT NULL REFERENCES tb_produto(id) ON DELETE RESTRICT,
    quantidade INT NOT NULL CHECK (quantidade > 0),
    valor_total NUMERIC(10,2) NOT NULL
);
```

Se o banco ainda tem as tabelas da versão anterior (módulo 29), apague-as antes de rodar o script acima:

```sql
DROP TABLE IF EXISTS item_pedido, pedido, cliente, produto;
```

Se as tabelas `tb_cliente`/`tb_produto` já existiam antes das colunas mais novas, basta adicioná-las (o `IF NOT EXISTS` pula as que já estiverem lá):

```sql
ALTER TABLE tb_cliente ADD COLUMN IF NOT EXISTS qtd_compras INT NOT NULL DEFAULT 0;
ALTER TABLE tb_cliente ADD COLUMN IF NOT EXISTS data_nascimento DATE;
ALTER TABLE tb_produto ADD COLUMN IF NOT EXISTS valor_compra NUMERIC(10,2);
ALTER TABLE tb_produto ADD COLUMN IF NOT EXISTS marca VARCHAR(50);
```

Pontos importantes do schema:

- `tb_cliente.cpf` é a chave de negócio do cliente (`@TipoChave("getCpf")`) e é usada na busca; `tel`, endereço, `numero`, `cidade`, `estado` e `email` são opcionais.
- `tb_produto.codigo` e `tb_venda.codigo` são as chaves de negócio de produto e venda (`@TipoChave("getCodigo")`).
- `tb_cliente.qtd_compras` guarda quantas compras o cliente já fez; começa em `0`, tanto no banco quanto no model `Cliente`.
- `tb_cliente.data_nascimento` e `tb_produto.marca` são opcionais.
- `tb_produto.valor_compra` é quanto a loja pagou pelo produto (o custo); `valor` é o preço de venda. É opcional.
- `tb_produto.disponivel` indica se o produto está ativo para venda; o padrão é `TRUE`, tanto no banco quanto no model `Produto`.
- `tb_venda.status_venda` guarda o nome do enum `Venda.Status`. Se a venda for cadastrada sem data ou sem status, o DAO usa a data atual e `INICIADA`.
- Excluir uma `Venda` remove seus itens em cascata (`ON DELETE CASCADE`).
- Excluir um `Produto` que está em alguma venda, ou um `Cliente` que tem venda, é bloqueado pelo banco (`ON DELETE RESTRICT`).
- `tb_produto_quantidade.valor_total` guarda o valor do item no momento da venda, já que `tb_produto.valor` pode mudar depois.

## Estrutura do projeto

```text
src/main/java/
├── anotacao/
│   ├── Tabela.java            (@Tabela: nome da tabela)
│   ├── ColunaTabela.java      (@ColunaTabela: coluna + setter)
│   └── TipoChave.java         (@TipoChave: getter da chave)
├── model/
│   ├── Persistente.java       (interface: getId/setId)
│   ├── Cliente.java
│   ├── Produto.java
│   ├── Venda.java             (venda + regras de adicionar/remover produtos)
│   └── ProdutoQuantidade.java (item da venda: produto + quantidade + valor total)
├── dao/
│   ├── ClienteDAO.java        (interface)
│   ├── ClienteDaoImpl.java
│   ├── ProdutoDAO.java        (interface)
│   ├── ProdutoDaoImpl.java
│   ├── VendaDAO.java          (interface)
│   └── VendaDaoImpl.java      (grava venda + itens na mesma transação)
└── factory/
    └── ConnectionFactory.java

src/test/java/anotacao/
└── AnotacoesTest.java               (annotations disponíveis em RUNTIME e no alvo certo)

src/test/java/factory/
└── ConnectionFactoryTest.java       (conexão e tratamento de erro, com DriverManager mockado)

src/test/java/model/
├── AnotacaoTest.java                (confere o mapeamento das entidades)
├── ClienteTest.java                 (valores iniciais e getters/setters)
├── ProdutoTest.java                 (valores iniciais e getters/setters)
├── ProdutoQuantidadeTest.java       (cálculo de quantidade e valor total do item)
└── VendaTest.java                   (regras de negócio da venda)

src/test/java/dao/
├── ClienteDaoImplTest.java          (integração)
├── ClienteDaoImplUnitTest.java      (unitário, com mocks)
├── ProdutoDaoImplTest.java          (integração)
├── ProdutoDaoImplUnitTest.java      (unitário, com mocks)
├── VendaDaoImplTest.java            (integração)
└── VendaDaoImplUnitTest.java        (unitário, com mocks)

src/test/resources/
└── mockito-extensions/
    └── org.mockito.plugins.MockMaker   (ativa o inline mock maker do Mockito)
```

## Arquitetura (camadas)

- **model** — as entidades, mapeadas com annotations: `@Tabela` (nome da tabela), `@ColunaTabela` (coluna do banco e setter que a preenche) e `@TipoChave` (getter da chave usada na busca — `getCpf` em `Cliente`, `getCodigo` em `Produto` e `Venda`). `Cliente`, `Produto` e `Venda` implementam `Persistente`. Os atributos `Venda.produtos` e `ProdutoQuantidade.produto` não são colunas: são preenchidos pelo DAO a partir das chaves estrangeiras.
- **anotacao** — definição das annotations de mapeamento (retenção `RUNTIME`, lidas por reflection).
- **dao** — uma interface por entidade + uma implementação JDBC (`*DaoImpl`) que abre conexão, monta o `PreparedStatement`, executa e fecha os recursos.
- **factory.ConnectionFactory** — fábrica estática de conexões (`DriverManager.getConnection`), sem pool: cada operação de DAO abre e fecha sua própria conexão.

Métodos expostos por cada DAO:

| DAO | Métodos |
| --- | --- |
| `ClienteDAO` | `cadastrar`, `atualizar`, `buscaCliente(cpf)`, `buscatodos`, `excluir` |
| `ProdutoDAO` | `cadastrar`, `atualizar`, `buscaProduto(codigo)`, `buscatodos`, `excluir` |
| `VendaDAO` | `cadastrar`, `atualizar`, `buscaVenda(codigo)`, `buscatodos`, `excluir` |

Sobre o `VendaDaoImpl`:

- `cadastrar` grava a venda e todos os itens numa única transação (`setAutoCommit(false)` + `commit`/`rollback`), e preenche os ids gerados na venda e nos itens.
- `atualizar` atualiza a venda e substitui os itens no banco pelos que estão no objeto, também numa transação.
- `buscaVenda` e `buscatodos` trazem a venda já com o `Cliente` completo e os itens com seus `Produto`.

## Configuração do banco de dados

A conexão é resolvida em [`ConnectionFactory`](src/main/java/factory/ConnectionFactory.java) a partir de variáveis de ambiente, com valores padrão para desenvolvimento local:

| Variável | Padrão |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:15432/postgres` |
| `DB_USER` | `postgres` |
| `DB_PASSWORD` | `Adm1234` |

Para usar outro banco, defina as variáveis de ambiente antes de rodar a aplicação/testes, por exemplo:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/meubanco"
$env:DB_USER = "meu_usuario"
$env:DB_PASSWORD = "minha_senha"
```

Antes de usar, crie as tabelas no banco executando o SQL da seção [Modelo de dados](#modelo-de-dados).

## Como rodar

O projeto foi criado no Eclipse (sem Maven/Gradle). O classpath (`.classpath`) referencia os `.jar` do driver PostgreSQL e do JUnit 5 diretamente pelo caminho local da máquina onde foi criado — ajuste esses caminhos (ou importe o projeto e adicione as bibliotecas pela IDE) caso rode em outra máquina:

- Driver JDBC: `postgresql-42.7.13.jar` (o `.classpath` aponta para a cópia do repositório local do Maven, `~/.m2/repository/org/postgresql/postgresql/42.7.13/`)
- JUnit 5: `junit-jupiter-api`, `junit-jupiter-engine`, `junit-platform-commons`, `junit-platform-engine`, `junit-platform-launcher`, `org.apiguardian.api`, `org.opentest4j`
- Mockito 5 (só para os testes unitários): `mockito-core`, `mockito-junit-jupiter`, `byte-buddy`, `byte-buddy-agent`, `objenesis`

Para importar:

1. Abra o Eclipse → `File > Open Projects from File System...` → selecione a pasta do projeto.
2. Garanta que um JDK 25 está configurado (é o que o `.classpath` declara; ajuste o `JRE_CONTAINER` se sua máquina só tiver outra versão instalada).
3. Ajuste as bibliotecas externas do `.classpath` se os `.jar` não estiverem nos mesmos caminhos.

## Testes

Todos os testes usam **JUnit 5**. São 73 testes em 13 classes:

| Grupo | Classes | Precisa de banco? |
| --- | --- | --- |
| Entidades | `AnotacaoTest`, `ClienteTest`, `ProdutoTest`, `ProdutoQuantidadeTest`, `VendaTest` | Não |
| Annotations | `AnotacoesTest` | Não |
| Conexão | `ConnectionFactoryTest` | Não (mocka o `DriverManager`) |
| DAOs — unitários | `ClienteDaoImplUnitTest`, `ProdutoDaoImplUnitTest`, `VendaDaoImplUnitTest` | Não (mocka o JDBC) |
| DAOs — integração | `ClienteDaoImplTest`, `ProdutoDaoImplTest`, `VendaDaoImplTest` | **Sim** |

- **Testes das entidades e annotations** — `AnotacaoTest` confere tabelas, colunas, chaves e se cada `setJavaName` existe; `ClienteTest` e `ProdutoTest` conferem os valores iniciais (`qtdCompras = 0`, `disponivel = true`) e os getters/setters; `VendaTest` e `ProdutoQuantidadeTest` conferem os cálculos de quantidade e valor total e o bloqueio de alteração em venda concluída; `AnotacoesTest` garante que as annotations ficam disponíveis em tempo de execução.
- **Teste da conexão** (`ConnectionFactoryTest`) — mocka o `DriverManager` para conferir que a conexão é devolvida e que uma `SQLException` vira `RuntimeException` com a causa original.
- **Testes de integração dos DAOs** (`ClienteDaoImplTest`, `ProdutoDaoImplTest`, `VendaDaoImplTest`) — se conectam a um banco PostgreSQL real (via `ConnectionFactory`), cadastram registros de teste (com códigos via `UUID` e CPFs aleatórios) e os removem no `@AfterEach`. **Exigem** um banco PostgreSQL acessível com as tabelas já criadas (seção [Modelo de dados](#modelo-de-dados)). `VendaDaoImplTest` também cria o cliente e os produtos da venda, e comprova que o banco bloqueia a exclusão de um produto que está numa venda.
- **Testes unitários dos DAOs** (`ClienteDaoImplUnitTest`, `ProdutoDaoImplUnitTest`, `VendaDaoImplUnitTest`) — **não tocam em banco nenhum**. Usam Mockito para mockar `Connection`/`PreparedStatement`/`ResultSet` e o método estático `ConnectionFactory.getConnection()` (via `Mockito.mockStatic`), verificando isoladamente: o SQL exato gerado, a ordem/valor dos parâmetros do `PreparedStatement`, o mapeamento do `ResultSet` para o model, o `commit`/`rollback` da venda (no cadastro e na atualização), e que `PreparedStatement`/`Connection` são fechados mesmo quando há exceção.

Para rodar os testes de integração sem mexer no banco do dia a dia, aponte `DB_URL`/`DB_USER`/`DB_PASSWORD` para um banco de testes com as tabelas criadas.

O `mockStatic` exige o *inline mock maker* do Mockito, habilitado pelo arquivo [`src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`](src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker) (conteúdo: `mock-maker-inline`). Sem essa pasta de resources no classpath de teste, os testes unitários falham com `MockitoException`.

Rode pelos utilitários de teste do Eclipse (clique direito na classe/pacote → `Run As > JUnit Test`) ou via linha de comando com `javac`/`java` apontando para os `.jar` listados acima e para o JUnit Platform Launcher.

## Observações e limitações conhecidas

- `ConnectionFactory` não usa pool de conexões — cada chamada de DAO abre e fecha sua própria `Connection`, o que é aceitável para fins didáticos mas não é recomendado em produção.
- A annotation de `Venda.cliente` aponta para `setIdClienteFk`, que não existe na classe (igual ao modelo do professor): quem grava e lê o id do cliente é o `VendaDaoImpl`. O `AnotacaoTest` trata essa coluna como exceção.
- As credenciais padrão (`postgres` / `Adm1234`) servem apenas para desenvolvimento local; sempre sobrescreva via variáveis de ambiente em qualquer outro ambiente.
- Os testes unitários usam mocking de método estático (`mockStatic`), o que acopla o teste à assinatura de `ConnectionFactory.getConnection()`; é um trade-off aceito aqui para testar os DAOs sem alterar o design de produção (não há injeção de dependência no projeto).
