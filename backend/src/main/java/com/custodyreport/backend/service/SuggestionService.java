package com.custodyreport.backend.service;

import com.custodyreport.backend.domain.Suggestion;
import com.custodyreport.backend.repository.SuggestionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuggestionService {
    private final SuggestionRepository repository;

    @PostConstruct
    public void initData() {
        if (repository.count() == 0) {
            // Role 1: Guarda Compartilhada (Pai/Mãe)
            repository.save(Suggestion.builder()
                    .roleId("1")
                    .scenario("Semana de Provas")
                    .field1Suggestion("Foco total nos estudos. Realizamos revisões diárias de matemática e história.")
                    .field2Suggestion("Horário de dormir antecipado para garantir descanso. Alimentação leve.")
                    .field3Suggestion("Ansioso(a) com a prova, mas confiante após os estudos.")
                    .field4Suggestion("Necessário conferir a nota do trabalho de artes na próxima semana.")
                    .build());

            repository.save(Suggestion.builder()
                    .roleId("1")
                    .scenario("Férias / Lazer")
                    .field1Suggestion("Dia de parque e atividades ao ar livre. Sem intercorrências de saúde.")
                    .field2Suggestion("Rotina mais flexível. Dormiu um pouco mais tarde.")
                    .field3Suggestion("Muito animado(a) e sociável com os primos.")
                    .field4Suggestion("Lembrar de passar o protetor solar amanhã.")
                    .build());
                    
            // Role 2: Cuidador de Idosos
            repository.save(Suggestion.builder()
                    .roleId("2")
                    .scenario("Dia Estável")
                    .field1Suggestion("Alimentação aceita sem restrições. Hidratação adequada.")
                    .field2Suggestion("Sono tranquilo. Acordou bem disposto(a).")
                    .field3Suggestion("Fisioterapia realizada pela manhã. Caminhada de 15 min no jardim.")
                    .field4Suggestion("Aferição de pressão normal: 12/8.")
                    .build());
        }
    }

    public List<Suggestion> getSuggestionsByRole(String roleId) {
        return repository.findByRoleId(roleId);
    }
}
