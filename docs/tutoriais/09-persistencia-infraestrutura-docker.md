# 09 — Persistência e Infraestrutura com Docker e Spring Data JPA

- **Escopo**: Definição do MySQL via `compose.yml` e implementação do pacote `infrastructure.persistence` (`TransactionEntity`, `TransactionEntityRepository`, `JpaTransactionRepository`).

---

## 1. Objetivo

Implementar o adaptador de persistência em MySQL utilizando Spring Data JPA e integração automática via `spring-boot-docker-compose`, mantendo o modelo relacional (`TransactionEntity`) desacoplado do modelo de domínio (`Transaction`).

---

## 2. Implementação

### 2.1. Dependências (`build.gradle`)
Em [`build.gradle`](../../build.gradle):

```groovy
developmentOnly 'org.springframework.boot:spring-boot-docker-compose'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
runtimeOnly 'com.mysql:mysql-connector-j'
```

### 2.2. Serviço MySQL (`compose.yml`)
Arquivo [`compose.yml`](../../compose.yml):

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

### 2.3. Propriedades JPA (`application.properties`)
Em [`src/main/resources/application.properties`](../../src/main/resources/application.properties):

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 2.4. Adaptador de Persistência (`infrastructure.persistence`)

1. **[`TransactionEntity.java`](../../src/main/java/dio/budgeting/infrastructure/persistence/entity/TransactionEntity.java)**:
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
                   this.category);
       }
   }
   ```

2. **[`TransactionEntityRepository.java`](../../src/main/java/dio/budgeting/infrastructure/persistence/repository/TransactionEntityRepository.java)**:
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

3. **[`JpaTransactionRepository.java`](../../src/main/java/dio/budgeting/infrastructure/persistence/repository/JpaTransactionRepository.java)**:
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
