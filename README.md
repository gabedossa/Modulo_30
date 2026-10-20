# 🛒 Módulo 30 — Cadastro de Clientes, Produtos e Vendas com JDBC + PostgreSQL

Projeto Java do curso de Back-end da **EBAC** que implementa um pequeno sistema de vendas em camadas
(**domínio → DAO → service**), persistindo os dados no **PostgreSQL** via **JDBC puro** e coberto por
**109 testes JUnit**.

![Java](https://img.shields.io/badge/Java-8-ED8B00?logo=openjdk&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-JDBC%2042.7-4169E1?logo=postgresql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-build-C71A36?logo=apachemaven&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit-4.13-25A162?logo=junit5&logoColor=white)
![Testes](https://img.shields.io/badge/testes-109%20passando-brightgreen)

---

## 📌 Funcionalidades

| Entidade | O que dá pra fazer |
|---|---|
| **Cliente** | Cadastrar, buscar por CPF, listar, atualizar e excluir |
| **Produto** | Cadastrar, buscar por código, listar, atualizar e excluir |
| **Venda** | Abrir uma venda, adicionar/remover produtos, finalizar, cancelar, listar e excluir |

### Regras de negócio (camada de service)

**Cliente**
- Nome obrigatório
- CPF com 11 dígitos — a máscara é removida (`111.111.111-11` → `11111111111`)
- Estado como sigla de 2 letras (convertido para maiúsculas)
- Número da casa positivo
- CPF não pode se repetir

**Produto**
- Código obrigatório, normalizado em maiúsculas e sem espaços nas pontas
- Nome obrigatório
- Valor maior que zero
- Código não pode se repetir

**Venda**
- Cliente e produtos precisam estar cadastrados — o preço sempre vem do produto salvo no banco
- O valor total é recalculado a cada produto adicionado ou removido
- Só é possível alterar produtos de uma venda **INICIADA**
- Só finaliza se tiver pelo menos um produto

```mermaid
stateDiagram-v2
    [*] --> INICIADA: cadastrar
    INICIADA --> INICIADA: adicionar / remover produto
    INICIADA --> CONCLUIDA: finalizar
    INICIADA --> CANCELADA: cancelar
    CONCLUIDA --> CANCELADA: cancelar
```

---

## 🗂️ Estrutura do projeto

```
src
├── main
│   ├── java/br/com/ebac
│   │   ├── App.java                   # testa a conexão com o banco
│   │   ├── anotacao/                  # @Tabela, @ColunaTabela, @TipoChave e AnotacaoUtil
│   │   ├── domain/                    # Persistente, Cliente, Produto, Venda, ProdutoQuantidade
│   │   ├── dao/                       # DAOs de cliente, produto e venda
│   │   │   ├── generic/               # IGenericDAO e GenericDAO (CRUD JDBC reaproveitado)
│   │   │   └── jdbc/ConnectionFactory # abre a conexão com o PostgreSQL
│   │   └── service/                   # validações e regras de negócio
│   │       └── generic/               # IGenericService e GenericService
│   └── resources
│       └── script.sql                 # criação das tabelas
└── test/java/br/com/ebac
    ├── *Test.java                     # testes de domínio, DAO e service
    └── *DAOMock.java                  # DAOs em memória (GenericDAOMock) usados nos testes de service
```

### Arquitetura com generics

O CRUD é escrito uma única vez em classes genéricas, e cada entidade só informa o que tem de específico.

```mermaid
classDiagram
    direction LR
    class Persistente {
        <<interface>>
        getId() Long
        setId(Long)
    }
    class IGenericDAO~T, E~ {
        <<interface>>
        cadastrar(T) Integer
        consultar(E) T
        buscarTodos() List~T~
        atualizar(T) Integer
        excluir(T) Integer
    }
    class GenericDAO~T, E~ {
        <<abstract>>
        getTipoClasse()* Class~T~
        converter(Connection, ResultSet) T
    }
    class GenericService~T, E~ {
        <<abstract>>
        validar(T)*
        normalizarChave(E)*
    }
    IGenericDAO <|.. GenericDAO
    GenericDAO <|-- ClienteDAO
    GenericDAO <|-- ProdutoDAO
    GenericDAO <|-- VendaDAO
    GenericService --> IGenericDAO : usa
    GenericService <|-- ClienteService
    GenericService <|-- ProdutoService
    GenericService <|-- VendaService
```

- **`T extends Persistente`**: a entidade (`Cliente`, `Produto`, `Venda`), que sempre tem um `id`.
- **`E extends Serializable`**: a chave de negócio usada em `consultar` — o **CPF** do cliente e o **código** do produto e da venda.
- **`GenericDAO`** lê as annotations da entidade e monta sozinho o SQL de insert, select, update e delete; cadastro e atualização rodam em transação.
- **`GenericService`** aplica as regras comuns: registro não nulo, chave sem duplicidade e existência antes de atualizar ou excluir.

### Mapeamento com annotations

As entidades dizem como são gravadas no banco por meio de três annotations próprias (pacote `br.com.ebac.anotacao`):

| Annotation | Onde | Para quê |
|---|---|---|
| `@Tabela("tb_cliente")` | classe | nome da tabela |
| `@ColunaTabela(dbName = "casa_numero", setJavaName = "setCasaNumero")` | atributo | coluna no banco e setter usado ao ler o registro |
| `@TipoChave` | atributo | chave de negócio usada em `consultar` e na checagem de duplicidade |

```java
@Tabela("tb_cliente")
public class Cliente implements Persistente {

    @ColunaTabela(dbName = "id", setJavaName = "setId")
    private Long id;

    @TipoChave
    @ColunaTabela(dbName = "cpf", setJavaName = "setCpf")
    private String cpf;

    @ColunaTabela(dbName = "casa_numero", setJavaName = "setCasaNumero")
    private Integer casaNumero;
    // ...
}
```

Com isso, um DAO só precisa dizer qual é a sua entidade:

```java
public class ClienteDAO extends GenericDAO<Cliente, String> implements IClienteDAO {

    @Override
    public Class<Cliente> getTipoClasse() {
        return Cliente.class;
    }
}
```

Atributos que apontam para outra entidade (como o `cliente` da `Venda`, na coluna `cliente_id`) são gravados pelo `id`.
O `VendaDAO` completa a leitura buscando o cliente e os itens da venda.

### Modelo de dados

```mermaid
erDiagram
    tb_cliente ||--o{ tb_venda : "faz"
    tb_venda ||--|{ tb_produto_quantidade : "contém"
    tb_produto ||--o{ tb_produto_quantidade : "aparece em"

    tb_cliente {
        bigserial id PK
        varchar nome
        varchar cpf UK
        varchar cidade
        varchar endereco
        varchar estado
        integer casa_numero
        varchar tel
    }
    tb_produto {
        bigserial id PK
        varchar codigo UK
        varchar nome
        varchar descricao
        numeric valor
    }
    tb_venda {
        bigserial id PK
        varchar codigo UK
        bigint cliente_id FK
        numeric valor_total
        timestamp data_venda
        varchar status
    }
    tb_produto_quantidade {
        bigserial id PK
        bigint venda_id FK
        bigint produto_id FK
        integer quantidade
        numeric valor_total
    }
```

A venda e seus itens são gravados **na mesma transação**: se algo falhar no meio, nada é salvo.

---

## 🚀 Como rodar

### Pré-requisitos
- Java 8 ou superior
- Maven
- PostgreSQL

### 1. Crie o banco e as tabelas

```bash
psql -U postgres -c "CREATE DATABASE ebac;"
psql -U postgres -d ebac -f src/main/resources/script.sql
```

### 2. Configure a conexão

Ajuste URL, usuário e senha em
[`ConnectionFactory.java`](src/main/java/br/com/ebac/dao/jdbc/ConnectionFactory.java):

```java
private static final String URL = "jdbc:postgresql://localhost:5432/ebac";
private static final String USER = "postgres";
private static final String PASSWORD = "admin";
```

### 3. Rode os testes

```bash
mvn test
```

---

## 🧪 Testes

| Camada | Arquivos | Usa o banco? |
|---|---|---|
| Domínio | `ClienteTest`, `ProdutoTest`, `ProdutoQuantidadeTest`, `VendaTest` | Não |
| Annotations | `AnotacaoTest` (tabelas, chaves e se todo `setJavaName` existe) | Não |
| DAO | `ClienteDAOTest`, `ProdutoDAOTest`, `VendaDAOTest`, `ConnectionFactoryTest` | Sim |
| Service | `ClienteServiceTest`, `ProdutoServiceTest`, `VendaServiceTest` | Não (usa mocks em memória) |

Os testes de DAO limpam os registros que criam, então o banco fica do jeito que estava.

---

Feito por **[Gabriel Vinces](https://github.com/gabedossa)** durante o curso de Back-end Java da EBAC.
