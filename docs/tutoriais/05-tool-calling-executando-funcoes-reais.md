# 05 — Tool Calling: Execução de Métodos Java pelo LLM

- **Escopo**: Anotação `@Tool`, registro via `.defaultTools(...)`, diagnóstico em nível `DEBUG` e `ToolCallingIT`.

---

## 1. Objetivo

Configurar o mecanismo de **Tool Calling** do Spring AI para que o modelo de linguagem delegue operações determinísticas e regras de negócio a métodos Java anotados com `@Tool`.

---

## 2. Implementação

### 2.1. Log de Diagnóstico (`application.properties`)
Em [`src/main/resources/application.properties`](../../src/main/resources/application.properties):

```properties
logging.level.org.springframework.ai=DEBUG
```
Habilita o rastreamento de chamadas gerenciadas por `DefaultToolCallingManager` e `MethodToolCallback`.

### 2.2. Teste de Integração (`ToolCallingIT.java`)
Arquivo [`src/test/java/dio/budgeting/ToolCallingIT.java`](../../src/test/java/dio/budgeting/ToolCallingIT.java):

```java
package dio.budgeting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
public class ToolCallingIT {
    @Autowired
    OpenAiChatModel openAiChatModel;

    @Test
    void should_executeSum_when_prompted() {
        var chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem("Você é um matemático")
                .defaultTools(new MathTools())
                .build();

        var response = chatClient.prompt("Some 10 mais 20. Depois subtraia 30 do resultado anterior. Exiba apenas o resultado final sem explicações")
                .call().content();

        assertThat(response).contains("0");
    }

    static class MathTools {
        @Tool(description = "soma dois números inteiros, a e b")
        public int sum(int a, int b) {
            return a + b;
        }

        @Tool(description = "subtrai dois números inteiros, a e b")
        public int diff(int a, int b) {
            return a - b;
        }
    }
}
```

---

## 3. Execução

```bash
./gradlew test --tests "dio.budgeting.ToolCallingIT" --info
```

> A seleção da tool pelo LLM depende diretamente dos metadados declarados em `@Tool(name, description)` e `@ToolParam(description)`.
