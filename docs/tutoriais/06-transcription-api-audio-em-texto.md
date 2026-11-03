# 06 — Transcription API: Conversão de Áudio em Texto (STT)

- **Escopo**: Configuração do modelo `whisper-1`, prompt de contexto fonético e `OpenAiTranscriptionModelIT`.

---

## 1. Objetivo

Integrar o `TranscriptionModel` (`OpenAiAudioTranscriptionModel`) para transcrever arquivos de áudio (`.m4a`) contendo registros de despesas em português brasileiro.

---

## 2. Implementação

### 2.1. Configuração de Transcrição (`application.properties`)
Em [`src/main/resources/application.properties`](../../src/main/resources/application.properties):

```properties
spring.ai.model.audio.transcription=openai
spring.ai.openai.audio.transcription.options.model=whisper-1
spring.ai.openai.audio.transcription.options.language=pt
spring.ai.openai.audio.transcription.options.temperature=0
spring.ai.openai.audio.transcription.options.response-format=text
spring.ai.openai.audio.transcription.options.prompt=Áudio em português brasileiro.\
  Áudio contém descrição de gastos financeiros. \
  As frases geralmente contêm: \
   - um valor em reais (número + "reais"); \
   - uma ação (gastei, paguei, comprei); \
   - um local ou estabelecimento (mercado, farmácia, restaurante, loja, etc.).
```

> O uso de `temperature=0` combinado com `options.prompt` reduz ambiguidades léxicas e força a representação numérica dos valores monetários.

### 2.2. Teste Parametrizado (`OpenAiTranscriptionModelIT.java`)
Arquivo [`src/test/java/dio/budgeting/OpenAiTranscriptionModelIT.java`](../../src/test/java/dio/budgeting/OpenAiTranscriptionModelIT.java), validando os arquivos em `src/test/resources/audio/recording-[1..6].m4a`:

```java
package dio.budgeting;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
public class OpenAiTranscriptionModelIT {
    @Autowired
    OpenAiAudioTranscriptionModel openAiTranscriptionModel;

    @ParameterizedTest
    @CsvSource({
            "recording-1.m4a, 80 reais",
            "recording-2.m4a, 40 reais",
            "recording-3.m4a, 120 reais",
            "recording-4.m4a, 90 reais",
            "recording-5.m4a, 200 reais",
            "recording-6.m4a, 60 reais",
    })
    public void should_containExpectedKeywords_when_audioFilesAreProcessed(String fileName, String expectedKeyword) {
        var recording = new ClassPathResource("audio/" + fileName);
        var response = openAiTranscriptionModel.call(recording);

        assertThat(response).contains(expectedKeyword);
    }
}
```

---

## 3. Execução

```bash
./gradlew test --tests "dio.budgeting.OpenAiTranscriptionModelIT" --info
```
