package com.custodyreport.backend.neutralization.debug;

import com.custodyreport.backend.neutralization.NeutralizationRequest;
import com.custodyreport.backend.neutralization.NeutralizationResult;
import com.custodyreport.backend.neutralization.ModeType;
import com.custodyreport.backend.neutralization.NeutralizationFacade;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.custodyreport.backend")
public class DeepTester {
    public static void main(String[] args) {
        SpringApplication.run(DeepTester.class, args);
    }

    @Bean
    public CommandLineRunner run(NeutralizationFacade facade) {
        return args -> {
            String text = "Ele frequentemente esquece de trazer o casaco da criança.";
            NeutralizationRequest req = new NeutralizationRequest(text, "custody", "pt-BR", ModeType.DEEP);
            
            NeutralizationResult result = facade.analyze(req);
            
            System.out.println("Text: " + text);
            System.out.println("Mode: " + result.modeUsed());
            System.out.println("Suggestions found: " + result.suggestions().size());
            result.suggestions().forEach(s -> {
                System.out.println(" - [" + s.ruleId() + "] " + s.originalSpan() + " -> " + s.suggestedReplacement());
            });
            
        };
    }
}
