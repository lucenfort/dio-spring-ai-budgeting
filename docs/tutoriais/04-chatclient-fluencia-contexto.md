# Aula 04 — ChatClient: Fluência e Contexto no Spring AI

- **Vídeo correspondente**: `ChatClient Fluência e Contexto no Spring AI.mp4` (~13m17s)
- **Etapa do Projeto**: Uso da API fluente `ChatClient`, definição de *System Prompt* padrão (`defaultSystem`) e criação do `OpenAiChatClientIT`.

---

## 🎯 1. Objetivo da Aula

Evoluir do uso direto de `ChatModel` para o **`ChatClient`**, uma abstração fluente e idiomática do Spring (semelhante ao `WebClient` e `RestClient`) que desacopla o código do provedor específico (OpenAI, Anthropic, Ollama, etc.) e facilita a configuração de mensagens de sistema (*System Message*), templates de prompt e ferramentas (*Tools*).

---

## 🛠️ 2. Passo a Passo de Implementação

### Passo 2.1 — Criar o Teste de Integração `OpenAiChatClientIT.java`
Crie o arquivo [`src/test/java/dio/budgeting/OpenAiChatClientIT.java`](../../src/test/java/dio/budgeting/OpenAiChatClientIT.java):

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
        System.out.println(response);
    }
}
```

### Passo 2.2 — (Exploratório na Aula) Injetar `ChatClient.Builder` em Beans do Spring
Em componentes gerenciados pelo Spring (como Controllers ou Services), o Spring AI disponibiliza automaticamente um bean `ChatClient.Builder` pré-configurado com as opções do `application.properties`:

```java
@RestController
@RequestMapping("/api")
public class ChatClientController {
    private final ChatClient chatClient;

    public ChatClientController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/chat")
    String chat(String prompt) {
        return this.chatClient.prompt().user(prompt).call().content();
    }
}
```

---

## ▶️ 3. Como Executar e Testar

Execute o teste de integração do `ChatClient`:
```bash
./gradlew test --tests "dio.budgeting.OpenAiChatClientIT"
```

---

## ✨ 4. Como Refinar no Repositório

- Prefira sempre injetar `ChatClient.Builder` nos seus Controllers/Services em vez de acoplar diretamente a `OpenAiChatModel`. Isso mantém sua camada de orquestração agnóstica de provedor.
