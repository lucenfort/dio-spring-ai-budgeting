# Aula 09 — Persistência e Infraestrutura: Configurando o Banco com Docker

- **Vídeo correspondente**: `Persistência e Infraestrutura Configurando o Banco com Docker.mp4` (~24m12s)
- **Etapa do Projeto**: Adição do MySQL com `spring-boot-docker-compose` (`compose.yml`) e implementação da camada `infrastructure.persistence` (`TransactionEntity`, `TransactionEntityRepository`, `JpaTransactionRepository`).

---

## 🎯 1. Objetivo da Aula

Implementar a persistência real de transações utilizando **Spring Data JPA**, **MySQL** e o suporte nativo do **Spring Boot Docker Compose**, mantendo a entidade JPA (`TransactionEntity`) separada da entidade pura de domínio (`Transaction`).

---

## 🛠️ 2. Passo a Passo de Implementação

### Passo 2.1 — Adicionar Dependências JPA, MySQL e Docker Compose no `build.gradle`
No [`build.gradle`](../../build.gradle), adicione:

```groovy
    developmentOnly 'org.springframework.boot:spring-boot-docker-compose'

    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.mysql:mysql-connector-j'
```

### Passo 2.2 — Criar o `compose.yml` na Raiz do Projeto
O arquivo [`compose.yml`](../../compose.yml) define o container MySQL gerenciado automaticamente pelo Spring Boot em tempo de desenvolvimento:

```yaml
services:
  database:
    image: mysql:9.6
    environment:
      MYSQL_DATABASE: transaction
      MYSQL_ROOT_PASSWORD: root
      MYSQL_USER: app
      MYSQL_PASSWORD: app
    ports:
      - "3307:3306"
    volumes:
      - transaction_data:/var/lib/mysql
    healthcheck:
      test: [ "CMD", "mysqladmin", "ping", "-h", "localhost", "-uapp", "-papp" ]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  transaction_data:
```

### Passo 2.3 — Configurar o Hibernate no `application.properties`
No [`src/main/resources/application.properties`](../../src/main/resources/application.properties), adicione:

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```
*(Não é necessário configurar `spring.datasource.url/username/password` manualmente quando `spring-boot-docker-compose` está ativo, pois o Spring Boot injeta as credenciais do `compose.yml` automaticamente via Service Connection).*

### Passo 2.4 — Implementar a Camada `infrastructure.persistence`

1. **Entidade JPA [`TransactionEntity.java`](../../src/main/java/dio/budgeting/infrastructure/persistence/entity/TransactionEntity.java)**:
   ```java
   package dio.budgeting.infrastructure.persistence.entity;

   import dio.budgeting.domain.Category;
   import dio.budgeting.domain.Transaction;
   import dio.budgeting.domain.TransactionId;
   import jakarta.persistence.Entity;
   import jakarta.persistence.EnumType;
   import jakarta.persistence.Enumerated;
   import jakarta.persistence.Id;
   import lombok.AllArgsConstructor;
   import lombok.Data;
   import lombok.NoArgsConstructor;

   import java.util.UUID;

   @Entity
   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   public class TransactionEntity {
       @Id
       private UUID id;
       private String description;
       private long amount;

       @Enumerated(EnumType.STRING)
       private Category category;

       public static TransactionEntity from(Transaction transaction) {
           return new TransactionEntity(
                   transaction.getId().uuid(),
                   transaction.getDescription(),
                   transaction.getAmount(),
                   transaction.getCategory());
       }

       public Transaction toDomain() {
           return new Transaction(
                   new TransactionId(this.id),
                   this.description,
                   this.amount,
                   this.category
           );
       }
   }
   ```

2. **Repositório Spring Data [`TransactionEntityRepository.java`](../../src/main/java/dio/budgeting/infrastructure/persistence/repository/TransactionEntityRepository.java)**:
   ```java
   package dio.budgeting.infrastructure.persistence.repository;

   import dio.budgeting.domain.Category;
   import dio.budgeting.infrastructure.persistence.entity.TransactionEntity;
   import org.springframework.data.jpa.repository.JpaRepository;

   import java.util.List;
   import java.util.UUID;

   public interface TransactionEntityRepository extends JpaRepository<TransactionEntity, UUID> {
       List<TransactionEntity> findAllByCategory(Category category);
   }
   ```

3. **Adaptador [`JpaTransactionRepository.java`](../../src/main/java/dio/budgeting/infrastructure/persistence/repository/JpaTransactionRepository.java)**:
   ```java
   package dio.budgeting.infrastructure.persistence.repository;

   import dio.budgeting.domain.Category;
   import dio.budgeting.domain.Transaction;
   import dio.budgeting.domain.TransactionRepository;
   import dio.budgeting.infrastructure.persistence.entity.TransactionEntity;
   import org.springframework.stereotype.Repository;

   import java.util.List;

   @Repository
   public class JpaTransactionRepository implements TransactionRepository {
       private final TransactionEntityRepository transactionEntityRepository;

       public JpaTransactionRepository(TransactionEntityRepository transactionEntityRepository) {
           this.transactionEntityRepository = transactionEntityRepository;
       }

       @Override
       public Transaction save(Transaction transaction) {
           var entity = TransactionEntity.from(transaction);
           return transactionEntityRepository.save(entity).toDomain();
       }

       @Override
       public List<Transaction> findAllByCategory(Category category) {
           return transactionEntityRepository.findAllByCategory(category)
                   .stream()
                   .map(TransactionEntity::toDomain)
                   .toList();
       }

       @Override
       public List<Transaction> findAll() {
           return transactionEntityRepository.findAll()
                   .stream()
                   .map(TransactionEntity::toDomain)
                   .toList();
       }
   }
   ```

---

## ▶️ 3. Como Executar e Validar

Com o Docker em execução, rode `./gradlew bootRun` e verifique no log do Hibernate a criação automática da tabela:
```sql
Hibernate: create table transaction_entity (amount bigint not null, id binary(16) not null, description varchar(255), category enum ('AUTO','GROCERIES','PHARMA'), primary key (id))
```
