package com.custodyreport.backend.neutralization.dictionary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.custodyreport.backend.neutralization.Severity;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SentimentDictionary {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Analyzer luceneAnalyzer = new BrazilianAnalyzer();

    private final Map<String, DictionaryEntry> stemIndex = new HashMap<>(); // For single-word stems
    @Getter
    private final Map<String, DictionaryEntry> phraseIndex = new HashMap<>(); // For multi-word stems
    private final Map<java.util.regex.Pattern, DictionaryEntry> regexIndex = new LinkedHashMap<>();
    @Getter
    private final Map<String, DictionaryEntry> allOriginals = new HashMap<>();
    @Getter
    private CnvVocabulary cnvVocabulary;

    @PostConstruct
    public void load() {
        try (InputStream is = new ClassPathResource("neutralization/dictionary-pt-BR.json").getInputStream()) {
            JsonNode root = objectMapper.readTree(is);
            JsonNode contexts = root.path("contexts");
            int loadedEntries = 0;

            Iterator<String> contextNames = contexts.fieldNames();
            while (contextNames.hasNext()) {
                String ctxName = contextNames.next();
                JsonNode categories = contexts.path(ctxName).path("categories");

                Iterator<String> catNames = categories.fieldNames();
                while (catNames.hasNext()) {
                    String catName = catNames.next();
                    JsonNode category = categories.path(catName);
                    Severity catSeverity = Severity.valueOf(category.path("severity").asText("MEDIUM"));
                    JsonNode entries = category.path("entries");

                    for (JsonNode entryNode : entries) {
                        DictionaryEntry entry = parseEntry(entryNode, catSeverity);
                        indexEntry(entry);
                        loadedEntries++;
                    }
                }
            }
            
            // Parse CNV Vocabulary (CR-06)
            JsonNode vocabNode = root.path("cnvVocabulary");
            if (!vocabNode.isMissingNode() && !vocabNode.isNull()) {
                this.cnvVocabulary = new CnvVocabulary(
                    parseList(vocabNode.path("feelingsWhenNeedsMet")),
                    parseList(vocabNode.path("feelingsWhenNeedsNotMet")),
                    parseList(vocabNode.path("universalNeeds"))
                );
            }

            log.info("SentimentDictionary loaded successfully. Total entries: {}. CNV Vocabulary loaded.", loadedEntries);
        } catch (Exception e) {
            log.error("Failed to load sentiment dictionary", e);
            throw new IllegalStateException("Falha crítica: Dicionário de neutralização não encontrado ou inválido.", e);
        }
    }

    private DictionaryEntry parseEntry(JsonNode node, Severity defaultSeverity) {
        List<String> stems = parseList(node.path("stems"));
        List<String> original = parseList(node.path("original"));
        List<String> suggestions = parseList(node.path("suggestions"));
        
        return new DictionaryEntry(
            node.path("ruleId").asText(),
            stems,
            original,
            suggestions,
            node.path("reason").asText(),
            node.path("pattern").asText(null),
            node.path("type").asText(null),
            defaultSeverity,
            node.path("cnvNote").asText(null)
        );
    }

    private List<String> parseList(JsonNode arrayNode) {
        List<String> list = new ArrayList<>();
        if (arrayNode.isArray()) {
            for (JsonNode node : arrayNode) {
                list.add(node.asText());
            }
        }
        return list;
    }

    private void indexEntry(DictionaryEntry entry) {
        if ("regex".equals(entry.type()) && entry.pattern() != null) {
            regexIndex.put(java.util.regex.Pattern.compile(entry.pattern()), entry);
        } else {
            if (entry.stems() != null) {
                for (String stem : entry.stems()) {
                    // CR-01: Separate single-word stems from multi-word stems
                    if (stem.trim().contains(" ")) {
                        phraseIndex.put(stem.trim(), entry);
                    } else {
                        stemIndex.put(stem.trim(), entry);
                    }
                }
            }
            if (entry.original() != null) {
                for (String orig : entry.original()) {
                    allOriginals.put(orig, entry);
                }
            }
        }
    }

    public Optional<DictionaryEntry> findByStem(String stem) {
        return Optional.ofNullable(stemIndex.get(stem));
    }

    public Map<java.util.regex.Pattern, DictionaryEntry> getRegexIndex() {
        return regexIndex;
    }

    public String stemToken(String token) throws IOException {
        TokenStream ts = luceneAnalyzer.tokenStream("f", token);
        CharTermAttribute attr = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        String stem = ts.incrementToken() ? attr.toString() : token;
        ts.end();
        ts.close();
        return stem;
    }
}
