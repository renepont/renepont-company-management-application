# 📘 Guia de Boas Práticas: Controle Financeiro

**Stack:** Spring Boot 3+ (Java 17/21), Angular 17+, MySQL 8+, Maven

## 1. Arquitetura de Software (Backend)

### 🏗️ Camadas do Sistema

Mantenha a separação rigorosa de responsabilidades para facilitar testes e manutenção:

- **Controller:** Apenas roteamento, tratamento de HTTP e validação inicial.
- **Service:** Onde reside o "coração" da aplicação. Toda regra de negócio (cálculos de lucro, validações de saldo) deve estar aqui.
- **Repository:** Interface de comunicação com o MySQL via Spring Data JPA.
- **DTO (Data Transfer Object):** Não exponha suas Entidades JPA diretamente para o Angular. Use DTOs para trafegar apenas o necessário, evitando ataques de Mass Assignment.

### 💰 Manipulação de Valores Monetários

- **Java:** Utilize sempre BigDecimal. Nunca use Float ou Double, pois eles causam erros de arredondamento em cálculos financeiros.
- **MySQL:** Utilize o tipo DECIMAL(19,2).
- **Cálculos:** Sempre realize cálculos de previsão e lucro no Backend (Java). O Frontend (Angular) deve ser apenas para exibição.

## 2. Banco de Dados (MySQL)

### ⚙️ Configuração e Design

- **Nomenclatura:** Tabelas e colunas devem usar snake_case (ex: data_vencimento).
- **Auditoria:** Adicione colunas de controle em tabelas financeiras: data_criacao, data_atualizacao e usuario_id.
- **Migrations:** Utilize Flyway ou Liquibase. Evite usar spring.jpa.hibernate.ddl-auto=update em produção; prefira scripts de migração versionados.
- **Soft Delete:** Considere adicionar uma coluna deletado (boolean) em vez de apagar registros financeiros permanentemente, para manter a integridade histórica.

## 3. Frontend (Angular)

### 🧩 Estrutura de Componentes

- **Organização:** Separe componentes por módulos (Ex: FinanceiroModule, SharedModule, CoreModule).
- **TypeScript:** Ative o modo estrito (strict: true) no tsconfig.json. Aproveite a tipagem forte para evitar erros de undefined em tempo de execução.
- **Services:** Toda chamada HTTP deve estar dentro de um Service. Nunca injete o HttpClient diretamente em um Component.

### 🎨 UX e Formulários

- **Reactive Forms:** Utilize ReactiveFormsModule para formulários complexos. Ele oferece melhor controle sobre validações e estados dos campos.
- **Pipes:** Utilize os pipes nativos do Angular para formatar moedas e datas na tela (ex: {{ valor | currency:'BRL' }}).

## 4. Segurança e IAM (Sua especialidade)

### 🔐 Autenticação e Autorização

- **JWT (JSON Web Token):** Utilize para manter a comunicação stateless.
- **HttpInterceptor:** Implemente um interceptor no Angular para anexar o cabeçalho Authorization: Bearer <token> automaticamente em cada requisição.
- **CORS:** Configure o CorsRegistry no Spring Boot permitindo apenas o domínio específico onde o Angular está rodando.

## 5. Checklist de Qualidade

- Os erros da API retornam um JSON padronizado com código HTTP correto (Ex: 400 para erro de validação).
- O sistema possui logs (Slf4j) para operações críticas (ex: exclusão de lançamentos).
- Credenciais de banco de dados e chaves JWT estão em variáveis de ambiente, nunca "hardcoded" no código.
- O BigDecimal está sendo instanciado via BigDecimal.valueOf(valor) ou new BigDecimal("string") para evitar perda de precisão.
