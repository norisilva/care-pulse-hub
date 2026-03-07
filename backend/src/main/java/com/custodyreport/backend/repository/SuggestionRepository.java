package com.custodyreport.backend.repository;

import com.custodyreport.backend.domain.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    List<Suggestion> findByRoleId(String roleId);
}
