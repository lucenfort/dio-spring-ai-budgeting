# Aula 06 — Transcription API: Transformando Áudio em Texto

- **Vídeo correspondente**: `Transcription API Transformando Áudio em Texto.mp4` (~23m29s)
- **Etapa do Projeto**: Configuração do modelo `whisper-1` no `application.properties`, prompt de contexto fonético e criação do teste parametrizado `OpenAiTranscriptionModelIT`.

---

## 🎯 1. Objetivo da Aula

Integrar a **Audio Transcription API** do Spring AI (`OpenAiAudioTranscriptionModel` / `TranscriptionModel`) para converter arquivos de áudio (`.m4a`) com comandos de voz de despesas financeiras em texto em português brasileiro com alta precisão.

---

## 🛠️ 2. Passo a Passo de Implementação

### Passo 2.1 — Configurar o Whisper e o Prompt de Contexto no `application.properties`
Adicione as propriedades de transcrição no [`src/main/resources/application.properties`](../../src/main/resources/application.properties):

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

> 💡 **Por que configurar `options.prompt` e `temperature=0` na transcrição?**
> Como explicado em aula, definir `temperature=0` e um prompt descrevendo o domínio financeiro evita que o Whisper transcreva números por extenso ou interprete incorretamente palavras como *"reais"*, *"farmácia"* ou *"mercado"*.

### Passo 2.2 — Verificar os Arquivos de Áudio de Teste
Os 6 áudios de exemplo estão em `src/test/resources/audio/`:
- `recording-1.m4a` (*80 reais*)
- `recording-2.m4a` (*40 reais*)
- `recording-3.m4a` (*120 reais*)
- `recording-4.m4a` (*90 reais*)
- `recording-5.m4a` (*200 reais*)
- `recording-6.m4a` (*60 reais*)

### Passo 2.3 — Criar o Teste Parametrizado `OpenAiTranscriptionModelIT.java`
Crie o arquivo [`src/test/java/dio/budgeting/OpenAiTranscriptionModelIT.java`](../../src/test/java/dio/budgeting/OpenAiTranscriptionModelIT.java):

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
        System.out.println(response);
    }
}
```

---

## ▶️ 3. Como Executar e Testar

```bash
./gradlew test --tests "dio.budgeting.OpenAiTranscriptionModelIT" --info
```
Você verá no console as frases transcritas de cada um dos 6 arquivos `.m4a` (por exemplo: *"Gastei na farmácia rapidinho e deixei 80 reais em três itens."*).
