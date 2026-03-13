package com.custodyreport.backend.neutralization.availability;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.postag.POSModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

/**
 * Carregamento automático do modelo OpenNLP POS Tagger (PT-BR).
 *
 * Fluxo:
 * 1. Tenta carregar do classpath (modelo já empacotado no JAR)
 * 2. Tenta carregar do diretório local (data/models/)
 * 3. Se não encontrou, baixa automaticamente do Apache e salva localmente
 * 4. Se tudo falhar, o Deep Mode delega para FastMode sem erro
 */
@Slf4j
@Component
public class NlpModelLoader {

    private static final String MODEL_FILENAME = "opennlp-pt-ud-gsd-pos-1.3-2.5.4.bin";
    private static final String DOWNLOAD_URL =
        "https://downloads.apache.org/opennlp/models/ud-models-1.3/" + MODEL_FILENAME;

    @Value("${carepulse.nlp.models-dir:./data/models}")
    private String modelsDir;

    @Getter
    private POSModel posModel;

    @Getter
    private boolean available = false;

    @PostConstruct
    public void load() {
        // 1. Tentar do classpath (modelo empacotado)
        if (loadFromClasspath()) {
            return;
        }

        // 2. Tentar do diretório local
        Path localPath = Path.of(modelsDir, MODEL_FILENAME);
        if (loadFromFile(localPath)) {
            return;
        }

        // 3. Auto-download do Apache
        log.info("Modelo NLP não encontrado. Iniciando download automático...");
        log.info("Fonte: {}", DOWNLOAD_URL);

        if (downloadModel(localPath) && loadFromFile(localPath)) {
            log.info("✓ Modelo baixado e carregado com sucesso — Deep Mode habilitado");
            return;
        }

        log.warn("Deep Mode NLP indisponível — operando apenas com Fast Mode");
        log.warn("Verifique sua conexão com a internet e reinicie para tentar novamente");
    }

    private boolean loadFromClasspath() {
        try (InputStream is = getClass().getResourceAsStream("/models/" + MODEL_FILENAME)) {
            if (is != null) {
                posModel = new POSModel(is);
                available = true;
                log.info("✓ OpenNLP POS model carregado do classpath — Deep Mode habilitado");
                return true;
            }
        } catch (Exception e) {
            log.debug("Modelo não encontrado no classpath: {}", e.getMessage());
        }
        return false;
    }

    private boolean loadFromFile(Path path) {
        if (!Files.exists(path)) {
            return false;
        }

        try (InputStream is = Files.newInputStream(path)) {
            posModel = new POSModel(is);
            available = true;
            log.info("✓ OpenNLP POS model carregado de {} — Deep Mode habilitado", path);
            return true;
        } catch (Exception e) {
            log.warn("Falha ao carregar modelo de {}: {}", path, e.getMessage());
            return false;
        }
    }

    private boolean downloadModel(Path targetPath) {
        try {
            Files.createDirectories(targetPath.getParent());

            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DOWNLOAD_URL))
                .timeout(Duration.ofSeconds(120))
                .GET()
                .build();

            log.info("Baixando modelo POS Tagger PT-BR (~5 MB)...");

            HttpResponse<InputStream> response = client.send(request,
                HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                try (InputStream bodyStream = response.body()) {
                    Files.copy(bodyStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                long sizeMb = Files.size(targetPath) / (1024 * 1024);
                log.info("✓ Download concluído: {} ({} MB)", MODEL_FILENAME, sizeMb);
                return true;
            } else {
                log.warn("Download falhou — HTTP {}", response.statusCode());
                // The body stream from an error response should also be closed to release resources
                try (InputStream bodyStream = response.body()) {
                     // Just close it, we don't need to read the error body
                }
                return false;
            }
        } catch (IOException | InterruptedException e) {
            log.warn("Falha no download do modelo: {}", e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
}
