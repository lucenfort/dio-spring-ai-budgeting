# Aula 07 — Speech API: Sintetizando Voz com Text-to-Speech

- **Vídeo correspondente**: `Speech API Sintetizando Voz com Text to Speech.mp4` (~16m45s)
- **Etapa do Projeto**: Configuração do `gpt-4o-mini-tts` (voz `nova`, velocidade `1.2`, formato `mp3`) no `application.properties` e criação do `OpenAiSpeechModelIT`.

---

## 🎯 1. Objetivo da Aula

Utilizar a **Audio Speech API (Text-to-Speech / TTS)** do Spring AI (`OpenAiAudioSpeechModel` / `TextToSpeechModel`) para transformar textos de resposta gerados pela aplicação em arquivos de áudio MP3 naturais em português.

---

## 🛠️ 2. Passo a Passo de Implementação

### Passo 2.1 — Configurar o Modelo de Síntese de Voz no `application.properties`
Adicione no [`src/main/resources/application.properties`](../../src/main/resources/application.properties):

```properties
spring.ai.model.audio.speech=openai
spring.ai.openai.audio.speech.options.model=gpt-4o-mini-tts
spring.ai.openai.audio.speech.options.voice=nova
spring.ai.openai.audio.speech.options.speed=1.2
spring.ai.openai.audio.speech.options.response-format=mp3
```

### Passo 2.2 — Criar o Teste de Integração `OpenAiSpeechModelIT.java`
Crie o arquivo [`src/test/java/dio/budgeting/OpenAiSpeechModelIT.java`](../../src/test/java/dio/budgeting/OpenAiSpeechModelIT.java):

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
        System.out.println(tempFile.toAbsolutePath());
    }
}
```

### Passo 2.3 — Como Retornar Áudio MP3 via HTTP no Spring Web
Para devolver o array `byte[]` produzido pelo `TextToSpeechModel` como download de arquivo MP3 em um Controller REST, encapsule-o em um `ByteArrayResource` com o cabeçalho `Content-Disposition`:

```java
byte[] audio = textToSpeechModel.call(texto);
var resource = new ByteArrayResource(audio);

return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                        .filename("audio.mp3")
                        .build()
                        .toString())
        .body(resource);
```

---

## ▶️ 3. Como Executar e Testar

```bash
./gradlew test --tests "dio.budgeting.OpenAiSpeechModelIT" --info
```
O caminho absoluto do arquivo `.mp3` gerado será exibido no console para que você possa ouvi-lo no reprodutor de mídia.
