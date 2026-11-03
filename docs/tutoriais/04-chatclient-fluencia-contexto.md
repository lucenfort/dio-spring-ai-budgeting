# 04 — ChatClient: API Fluente e Contexto

- **Escopo**: Uso de `ChatClient`, definição de `defaultSystem` e criação do `OpenAiChatClientIT`.

---

## 1. Objetivo

Adotar o `ChatClient` como camada de abstração fluente sobre o `ChatModel`, desacoplando a aplicação do provedor específico de LLM e centralizando prompts de sistema e ferramentas.

---

## 2. Implementação

### 2.1. Teste de Integração (`OpenAiChatClientIT.java`)
Arquivo [`src/test/java/dio/budgeting/OpenAiChatClientIT.java`](../../src/test/java/dio/budgeting/OpenAiChatClientIT.java):

```java
package dio.budgeting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
public class OpenAiChatClientIT {
    @Autowired
    OpenAiChatModel openAiChatModel;

    @Test
    void should_executeSum_when_prompted() {
        var chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem("Você é um matemático")
                .build();

        var response = chatClient.prompt("Some 10 mais 20. Depois subtraia 30 do resultado anterior. Exiba apenas o resultado final sem explicações")
                .call()
                .content();

        assertThat(response).contains("0");
    }
}
```

### 2.2. Injeção via `ChatClient.Builder`
Em componentes gerenciados pelo Spring, injete `ChatClient.Builder` (auto-configurado pelo Spring AI) em vez da implementação concreta do modelo:

```java
public TransactionController(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder
            .defaultSystem(...)
            .build();
}
```

---

## 3. Execução

```bash
./gradlew test --tests "dio.budgeting.OpenAiChatClientIT"
```
