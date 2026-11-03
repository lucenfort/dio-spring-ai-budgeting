# 07 — Speech API: Síntese de Voz (Text-to-Speech)

- **Escopo**: Configuração do modelo `gpt-4o-mini-tts` e implementação do `OpenAiSpeechModelIT`.

---

## 1. Objetivo

Utilizar a interface `TextToSpeechModel` (`OpenAiAudioSpeechModel`) para converter respostas textuais em fluxos de áudio MP3.

---

## 2. Implementação

### 2.1. Configuração de Síntese (`application.properties`)
Em [`src/main/resources/application.properties`](../../src/main/resources/application.properties):

```properties
spring.ai.model.audio.speech=openai
spring.ai.openai.audio.speech.options.model=gpt-4o-mini-tts
spring.ai.openai.audio.speech.options.voice=nova
spring.ai.openai.audio.speech.options.speed=1.2
spring.ai.openai.audio.speech.options.response-format=mp3
```

### 2.2. Teste de Integração (`OpenAiSpeechModelIT.java`)
Arquivo [`src/test/java/dio/budgeting/OpenAiSpeechModelIT.java`](../../src/test/java/dio/budgeting/OpenAiSpeechModelIT.java):

```java
package dio.budgeting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
public class OpenAiSpeechModelIT {
    @Autowired
    OpenAiAudioSpeechModel openAiSpeechModel;

    @Test
    public void should_produceAudio_when_textIsProvided() throws IOException {
        var response = openAiSpeechModel.call("O valor total do serviço ficou em 80 reais. Posso confirmar o pagamento?");

        assertThat(response).hasSizeGreaterThan(1024);

        var tempFile = Files.createTempFile("AUDIO_", ".mp3");
        Files.write(tempFile, response);
    }
}
```

### 2.3. Retorno Binário via HTTP
Para retornar o array `byte[]` em um endpoint Spring MVC:

```java
byte[] audio = textToSpeechModel.call(texto);
var resource = new ByteArrayResource(audio);

return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("audio.mp3").build().toString())
        .body(resource);
```

---

## 3. Execução

```bash
./gradlew test --tests "dio.budgeting.OpenAiSpeechModelIT" --info
```
