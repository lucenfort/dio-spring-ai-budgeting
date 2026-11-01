# Aula 05 — Tool Calling: Executando Funções Reais com IA

- **Vídeo correspondente**: `Tool Calling Executando Funções Reais com IA.mp4` (~10m25s)
- **Etapa do Projeto**: Uso da anotação `@Tool`, registro em `.defaultTools(...)`, ativação de logs `DEBUG` e criação do `ToolCallingIT`.

---

## 🎯 1. Objetivo da Aula

Aprender como funciona o recurso de **Tool Calling (Function Calling)** no Spring AI. Em vez de o modelo de linguagem apenas gerar texto (ou tentar adivinhar cálculos e dados do sistema), ele identifica quando precisa chamar um método Java anotado com `@Tool`, solicita sua execução ao Spring AI (`DefaultToolCallingManager`) e usa o retorno real da função para formular a resposta final.

---

## 🛠️ 2. Passo a Passo de Implementação

### Passo 2.1 — Ativar Logs de Debug do Spring AI
No arquivo [`src/main/resources/application.properties`](../../src/main/resources/application.properties), adicione:

```properties
logging.level.org.springframework.ai=DEBUG
```
Com essa propriedade ativa, o console exibirá os logs do `DefaultToolCallingManager` e `MethodToolCallback` sempre que a IA invocar uma ferramenta Java.

### Passo 2.2 — Criar o Teste de Integração `ToolCallingIT.java`
Crie o arquivo [`src/test/java/dio/budgeting/ToolCallingIT.java`](../../src/test/java/dio/budgeting/ToolCallingIT.java):

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
        System.out.println(response);
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

## ▶️ 3. Como Executar e Testar

Execute o teste e observe no console as linhas de `DEBUG` mostrando a chamada das funções `sum` e `diff`:
```bash
./gradlew test --tests "dio.budgeting.ToolCallingIT" --info
```

---

## ✨ 4. Como Refinar no Repositório

- **Descrições claras em `@Tool` e `@ToolParam`**: O LLM decide qual método chamar e como preencher seus parâmetros lendo exclusivamente o nome e a `description` das anotações `@Tool` e `@ToolParam`. Escreva descrições precisas e sem ambiguidade.
