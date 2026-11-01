# Aula 03 — Explorando o ChatModel e Modelos de Linguagem

- **Vídeo correspondente**: `Explorando o ChatModel e Modelos de Linguagem.mp4` (~22m27s)
- **Etapa do Projeto**: Criação do teste de integração `OpenAiChatModelIT`, configuração de `OpenAiChatOptions` e primeiro teste de comunicação com o modelo `gpt-4o-mini`.

---

## 🎯 1. Objetivo da Aula

Entender como funciona a interface `ChatModel` (e sua implementação `OpenAiChatModel`), como customizar opções de chamada (`model`, `temperature`, `responseFormat`) via código ou via `application.properties`, e criar um teste de integração seguro que só executa quando a variável `OPENAI_API_KEY` está presente.

---

## 🛠️ 2. Passo a Passo de Implementação

### Passo 2.1 — Criar o Teste de Integração `OpenAiChatModelIT.java`
Crie o arquivo [`src/test/java/dio/budgeting/OpenAiChatModelIT.java`](../../src/test/java/dio/budgeting/OpenAiChatModelIT.java):

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
        System.out.println(response);
    }
}
```

### Passo 2.2 — Externalizar Opções do ChatModel no `application.properties`
Em vez de construir o `OpenAiChatOptions` manualmente toda vez, adicione no [`src/main/resources/application.properties`](../../src/main/resources/application.properties):

```properties
spring.ai.openai.chat.options.model=gpt-4o-mini
spring.ai.openai.chat.options.response-format.type=TEXT
```

### Passo 2.3 — (Exploratório na Aula) Testar via Controller REST
Durante a aula, o instrutor demonstra um controller simples (`ChatModelController`) para testar no cliente HTTP:
```java
@RestController
@RequestMapping("/api")
public class ChatModelController {
    private final OpenAiChatModel openAiChatModel;

    public ChatModelController(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    @GetMapping("/chat-model")
    String chat(String prompt) {
        return this.openAiChatModel.call(prompt);
    }
}
```
*(Nota: este controller exploratório será substituído na Aula 11 pelo `TransactionController` definitivo).*

---

## ▶️ 3. Como Executar e Testar

Execute o teste de integração com a chave da OpenAI configurada:
```bash
export OPENAI_API_KEY="sua_chave_aqui"
./gradlew test --tests "dio.budgeting.OpenAiChatModelIT"
```

---

## ✨ 4. Como Refinar no Repositório

- O uso da anotação `@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")` é fundamental para que pipelines de CI (como o GitHub Actions) não quebrem quando rodarem sem segredos de API externa.
