package com.custodyreport.backend.neutralization.steps;

import com.custodyreport.backend.neutralization.availability.NlpModelLoader;
import com.custodyreport.backend.neutralization.pipeline.PipelineContext;
import com.custodyreport.backend.neutralization.pipeline.PipelineStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.postag.POSTaggerME;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Step de POS Tagging usando Apache OpenNLP.
 * Só executa se o modelo estiver disponível (carregado pelo NlpModelLoader).
 * Caso contrário, é silenciosamente ignorado.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PosTaggingStep implements PipelineStep {

    private final NlpModelLoader modelLoader;

    @Override
    public boolean shouldExecute(PipelineContext context) {
        return modelLoader.isAvailable();
    }

    @Override
    public void execute(PipelineContext ctx) {
        if (!shouldExecute(ctx)) {
            return;
        }

        POSTaggerME tagger = new POSTaggerME(modelLoader.getPosModel());
        List<String> allTags = new ArrayList<>();

        for (String sentence : ctx.getSentences()) {
            String[] tokens = sentence.split("\\s+");
            String[] tags = tagger.tag(tokens);
            allTags.addAll(Arrays.asList(tags));
        }

        ctx.setPosTags(allTags);
        ctx.markStepApplied("PosTagger");

        log.debug("POS Tagging concluído: {} tags geradas", allTags.size());
    }
}
