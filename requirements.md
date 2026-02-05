# 📋 Documentação Técnica: Sistema de Controle Financeiro Multi-Empresa

## 1. Tabela de Requisitos

| ID        | Tipo      | Requisito                 | Descrição                                                   | Prioridade |
| :-------- | :-------- | :------------------------ | :---------------------------------------------------------- | :--------- |
| **RF01**  | Funcional | **Gestão de Lançamentos** | CRUD de entradas e saídas financeiras.                      | Alta       |
| **RF02**  | Funcional | **Vínculo de Data**       | Todo lançamento deve obrigatoriamente possuir uma data.     | Alta       |
| **RF03**  | Funcional | **Cálculo de Saldo**      | Exibição do saldo líquido (Entradas - Saídas) por empresa.  | Alta       |
| **RF04**  | Funcional | **Relatório de Previsão** | Projeção de lucro/prejuízo baseada em datas futuras.        | Alta       |
| **RF05**  | Funcional | **Isolamento de Dados**   | Garantir que dados de uma empresa nunca vazem para outra.   | Crítica    |
| **RF06**  | Funcional | **Gestão de Categorias**  | CRUD de categorias vinculado à empresa (Apenas ADMIN cria). | Alta       |
| **RF07**  | Funcional | **Hierarquia de Acesso**  | `ROLE_ADMIN` gerencia infra; `ROLE_USER` opera lançamentos. | Alta       |
| **RF08**  | Funcional | **Filtros de Período**    | Filtragem de dados por mês, ano ou range customizado.       | Média      |
| **RNF01** | Não Func. | **Precisão Monetária**    | Uso de `BigDecimal` (Java) e `DECIMAL(19,2)` (MySQL).       | Crítica    |
| **RNF02** | Não Func. | **Segurança (IAM)**       | Autenticação JWT e autorização via Spring Security.         | Crítica    |
| **RNF03** | Não Func. | **Arquitetura SPA**       | Frontend em Angular com navegação assíncrona.               | Média      |
| **RNF04** | Não Func. | **Máscaras de Input**     | Inputs com máscaras para moeda e datas (Frontend).          | Baixa      |

---

## 2. Histórias de Usuário (User Stories)

### US01 - Operação de Caixa

**Como** operador financeiro (`ROLE_USER`),  
**eu quero** registrar receitas e despesas com data e categoria,  
**para que** o fluxo de caixa da empresa seja mantido atualizado.

- **Critérios de Aceite:** Valor positivo, data obrigatória e seleção de empresa ativa.

### US02 - Análise de Saúde Financeira

**Como** gestor,  
**eu quero** visualizar o saldo consolidado e a previsão para os próximos meses,  
**para planejar** investimentos ou cortes com base no lucro esperado.

### US03 - Gestão Administrativa (IAM)

**Como** administrador do sistema (`ROLE_ADMIN`),  
**eu quero** criar empresas e vincular usuários a elas,  
**para garantir** que cada colaborador acesse apenas o que lhe é permitido.

---

## 3. Especificações de IAM e Multi-Tenancy

### Perfis de Acesso (RBAC)

- **ROLE_ADMIN:**
- Gerenciamento de Empresas e Usuários (CRUD).
- Gerenciamento de Categorias de lançamento.
- **ROLE_USER:**
- Lançamentos financeiros e relatórios nas empresas permitidas.
- Sem permissão para criar categorias ou novas empresas.

### Estratégia de Isolamento

- **Identificação:** O Frontend envia o ID da empresa ativa através do Header `X-Tenant-ID`.
- **Validação:** O Backend valida o Header contra as permissões do usuário logado (JWT).
- **Contexto:** Uso de `ThreadLocal` no Java para manter o `tenant_id` seguro durante a execução da requisição.

---

## 4. Arquitetura de Implementação (Maven)

### Estrutura de Pacotes

```text
src/main/java/com/empresa/financeiro/
├── config/       # SecurityConfig, WebConfig
├── controller/   # Endpoints REST
├── dto/          # Objetos de Transferência (Request/Response)
├── model/        # Entidades JPA (Empresa, Usuario, Lancamento)
├── repository/   # Interfaces JpaRepository
├── security/     # JWTFilter, TenantFilter, TenantContext
└── service/      # Lógica de Negócio e Previsões
```

## 5. Modelo de Dados (MySQL)

```sql
-- Empresas
CREATE TABLE empresas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_fantasia VARCHAR(100) NOT NULL,
    cnpj VARCHAR(18) UNIQUE NOT NULL
);

-- Usuários
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    perfil ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER'
);

-- Relacionamento Many-to-Many: Usuário atende N empresas
CREATE TABLE usuario_empresa (
    usuario_id BIGINT NOT NULL,
    empresa_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, empresa_id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (empresa_id) REFERENCES empresas(id)
);

-- Categorias (Criadas por ADMIN)
CREATE TABLE categorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    empresa_id BIGINT NOT NULL,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id)
);

-- Lançamentos
CREATE TABLE lancamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    descricao VARCHAR(255),
    valor DECIMAL(19,2) NOT NULL,
    data_lancamento DATE NOT NULL,
    tipo ENUM('ENTRADA', 'SAIDA') NOT NULL,
    usuario_id BIGINT NOT NULL,
    empresa_id BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);
```
