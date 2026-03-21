package com.custodyreport.backend.neutralization.debug;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import java.io.FileInputStream;
import java.io.InputStream;

public class TagDebugger {
    public static void main(String[] args) throws Exception {
        String modelPath = "./data/models/opennlp-pt-ud-gsd-pos-1.3-2.5.4.bin";
        try (InputStream modelIn = new FileInputStream(modelPath)) {
            POSModel model = new POSModel(modelIn);
            POSTaggerME tagger = new POSTaggerME(model);
            
            String sentence = "Ele frequentemente esquece de trazer o casaco da criança";
            String[] tokens = sentence.split(" ");
            String[] tags = tagger.tag(tokens);
            
            for (int i = 0; i < tokens.length; i++) {
                System.out.println(tokens[i] + " -> " + tags[i]);
            }
        }
    }
}
