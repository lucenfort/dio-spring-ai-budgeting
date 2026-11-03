# 03 — ChatModel e Opções de Inferência

- **Escopo**: Uso de `OpenAiChatModel`, `OpenAiChatOptions` e implementação do `OpenAiChatModelIT`.

---

## 1. Objetivo

Utilizar a interface `ChatModel` (`OpenAiChatModel`) para comunicação direta com o modelo `gpt-4o-mini` e parametrizar opções de inferência programaticamente e via configuração externa.

---

## 2. Implementação

### 2.1. Teste de Integração (`OpenAiChatModelIT.java`)
Arquivo [`src/test/java/dio/budgeting/OpenAiChatModelIT.java`](../../src/test/java/dio/budgeting/OpenAiChatModelIT.java):

```java
package dio.budgeting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
public class OpenAiChatModelIT {
    @Autowired
    OpenAiApi openAiApi;

    @Test
    void should_receiveResponse_when_chatModelIsCalled() {
        var options = OpenAiChatOptions.builder()
                .model("gpt-4o-mini")
                .temperature(0.8)
                .responseFormat(ResponseFormat.builder().type(ResponseFormat.Type.TEXT).build())
                .build();

        var chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();

        var response = chatModel.call("Gere um registro de budgeting, com descrição de gasto, valor em reais e local");

        assertThat(response).isNotEmpty();
    }
}
```

### 2.2. Parametrização no `application.properties`
Em [`src/main/resources/application.properties`](../../src/main/resources/application.properties):

```properties
spring.ai.openai.chat.options.model=gpt-4o-mini
spring.ai.openai.chat.options.response-format.type=TEXT
```

---

## 3. Execução

```bash
export OPENAI_API_KEY="sk-..."
./gradlew test --tests "dio.budgeting.OpenAiChatModelIT"
```

> A anotação `@EnabledIfEnvironmentVariable` garante que o teste seja ignorado em pipelines de CI sem credenciais externas.
