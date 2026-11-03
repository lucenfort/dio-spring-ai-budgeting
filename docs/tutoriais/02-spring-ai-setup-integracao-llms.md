# 02 — Configuração do Spring AI e Integração com LLMs

- **Escopo**: Configuração de dependências no `build.gradle` e credenciais no `application.properties`.

---

## 1. Objetivo

Configurar o BOM (*Bill of Materials*) do Spring AI e o starter da OpenAI em uma aplicação Spring Boot 4 com Java 25.

---

## 2. Implementação

### 2.1. Dependências (`build.gradle`)
No arquivo [`build.gradle`](../../build.gradle), declare o toolchain do Java 25, o BOM do Spring AI e o starter `spring-ai-starter-model-openai`:

```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-web'

    implementation platform("org.springframework.ai:spring-ai-bom:2.0.0-M4")
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

### 2.2. Credenciais (`application.properties`)
No arquivo [`src/main/resources/application.properties`](../../src/main/resources/application.properties), configure a chave de API via variável de ambiente:

```properties
spring.application.name=budgeting
spring.ai.openai.api-key=${OPENAI_API_KEY}
```

> **Nota**: A ausência da variável `OPENAI_API_KEY` durante a inicialização do contexto lançará `IllegalArgumentException: OpenAI API key must be set`.

---

## 3. Validação

```bash
export OPENAI_API_KEY="sk-..."
./gradlew bootRun
```
