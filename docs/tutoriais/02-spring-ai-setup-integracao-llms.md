# Aula 02 — Spring AI Setup e Integração com LLMs

- **Vídeo correspondente**: `Spring AI Setup e Integração com LLMs.mp4` (~14m15s)
- **Etapa do Projeto**: Configuração do Gradle (`build.gradle`), dependências do Spring AI e chave da OpenAI.

---

## 🎯 1. Objetivo da Aula

Explorar a documentação oficial do **Spring AI**, entender seus módulos principais (*Chat Models*, *Transcription API*, *Text-To-Speech API*, *Tool Calling* e *Advisors*) e configurar o projeto Spring Boot com o starter da OpenAI.

---

## 🛠️ 2. Passo a Passo de Implementação

### Passo 2.1 — Configurar o `build.gradle`
No arquivo [`build.gradle`](../../build.gradle), configure o Java 25, o BOM (*Bill of Materials*) do Spring AI e a dependência `spring-ai-starter-model-openai`:

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.0.5'
    id 'io.spring.dependency-management' version '1.1.7'
    id("io.freefair.lombok") version "9.2.0"
}

group = 'dio'
version = '0.0.1-SNAPSHOT'
description = 'budgeting'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'

    // BOM e Starter do Spring AI para OpenAI
    implementation platform("org.springframework.ai:spring-ai-bom:2.0.0-M4")
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'

    implementation 'org.springframework.boot:spring-boot-starter-web'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

### Passo 2.2 — Configurar a API Key no `application.properties`
No arquivo [`src/main/resources/application.properties`](../../src/main/resources/application.properties), defina o nome da aplicação e referencie a variável de ambiente `OPENAI_API_KEY`:

```properties
spring.application.name=budgeting
spring.ai.openai.api-key=${OPENAI_API_KEY}
```

> ⚠️ **Atenção (demonstrado no vídeo)**: Se você iniciar a aplicação sem definir a variável `OPENAI_API_KEY`, o autoconfigure do Spring AI lançará a exceção:
> `java.lang.IllegalArgumentException: OpenAI API key must be set. Use the connection property: spring.ai.openai.api-key`

---

## ▶️ 3. Como Executar e Validar

1. Exporte sua chave de API da OpenAI no terminal (ou nas configurações de *Run/Debug* da IDE):
   ```bash
   export OPENAI_API_KEY="sk-..."
   ```
2. Suba a aplicação para validar que o contexto do Spring Boot inicia sem erros:
   ```bash
   ./gradlew bootRun
   ```

---

## ✨ 4. Como Refinar no Repositório

- **Nunca faça commit da sua chave real** no `application.properties`. Mantenha sempre `${OPENAI_API_KEY}` e disponibilize um arquivo [`.env.example`](../../.env.example) na raiz do repositório.
- Certifique-se de que `.env` está listado no [`.gitignore`](../../.gitignore).
