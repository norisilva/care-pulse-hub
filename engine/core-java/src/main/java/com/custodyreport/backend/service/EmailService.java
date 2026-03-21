package com.custodyreport.backend.service;

import com.custodyreport.backend.domain.Report;
import com.custodyreport.backend.domain.SystemConfig;
import com.custodyreport.backend.domain.NotificationGroup;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final ConfigService configService;
    private final NotificationGroupService groupService;
    
    private JavaMailSenderImpl createMailSender(SystemConfig config) {
        if (config.getEmailUsername() == null || config.getEmailPassword() == null) {
            throw new IllegalStateException("Credenciais de email não configuradas no sistema.");
        }
        
        String plainUser = configService.decrypt(config.getEmailUsername(), config.getMasterKey());
        String plainPass = configService.decrypt(config.getEmailPassword(), config.getMasterKey());
        
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(plainUser);
        mailSender.setPassword(plainPass);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");
        
        return mailSender;
    }
    
    @Async
    public void sendStandardizedReport(Report report) {
        try {
            SystemConfig config = configService.getConfig();
            
            // Get recipient from Notification Group if available, else fallback
            String mailTo = "exemplo@email.com";
            if (report.getNotificationGroupId() != null) {
                mailTo = groupService.findById(report.getNotificationGroupId())
                            .map(NotificationGroup::getRecipientEmail)
                            .orElse(config.getRecipientEmail() != null ? config.getRecipientEmail() : "exemplo@email.com");
            } else if (config.getRecipientEmail() != null) {
                mailTo = config.getRecipientEmail();
            }
            
            JavaMailSenderImpl mailSender = createMailSender(config);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            String dateStr = report.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String assistedName = report.getAssistedName() != null ? report.getAssistedName() : "Assistido";
            
            helper.setSubject(String.format("[CarePulse Hub] Relatório de %s - %s", assistedName, dateStr));
            helper.setTo(mailTo); 
            
            String role = config.getUserRole() != null ? config.getUserRole() : "1";
            String[] labels = getLabelsForRole(role);
            
            String htmlContent = String.format(
                "<h2>Relatório de Cuidados</h2>" +
                "<p><b>Data:</b> %s</p>" +
                "<p><b>Nome do Assistido:</b> %s</p>" +
                "<hr>" +
                "<p><b>%s:</b><br>%s</p>" +
                "<p><b>%s:</b><br>%s</p>" +
                "<p><b>%s:</b><br>%s</p>" +
                "<p><b>%s:</b><br>%s</p>",
                HtmlUtils.htmlEscape(dateStr),
                HtmlUtils.htmlEscape(assistedName),
                HtmlUtils.htmlEscape(labels[0]), HtmlUtils.htmlEscape(report.getField1() != null ? report.getField1() : ""),
                HtmlUtils.htmlEscape(labels[1]), HtmlUtils.htmlEscape(report.getField2() != null ? report.getField2() : ""),
                HtmlUtils.htmlEscape(labels[2]), HtmlUtils.htmlEscape(report.getField3() != null ? report.getField3() : ""),
                HtmlUtils.htmlEscape(labels[3]), HtmlUtils.htmlEscape(report.getField4() != null ? report.getField4() : "")
            );
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Email sent successfully for report ID: {}", report.getId());
        } catch (Exception e) {
            log.warn("Email sending skipped or failed (credentials might not be configured): {}", e.getMessage());
        }
    }

    private String[] getLabelsForRole(String role) {
        switch (role) {
            case "2": return new String[]{"Alimentação", "Sono", "Atividades", "Observações"};
            case "3": return new String[]{"Sinais Vitais", "Medicação", "Evolução", "Intercorrências"};
            case "4": return new String[]{"Desempenho", "Foco", "Tarefas", "Sugestões"};
            case "5": return new String[]{"Passeio", "Alimentação", "Comportamento", "Saúde"};
            case "6": return new String[]{"Treino Realizado", "Condição Física", "Frequência Cardíaca", "Metas"};
            case "7": return new String[]{"Veículo/Aparelho", "Diagnóstico", "Peças", "Testes"};
            case "8": return new String[]{"Cômodo", "Feito", "Pendências", "Observações"};
            case "9": return new String[]{"Obra/Fase", "Materiais", "Avanço", "Problemas"};
            case "10": return new String[]{"Campo 1", "Campo 2", "Campo 3", "Campo 4"};
            case "1":
            default:
                return new String[]{"Saúde", "Rotina/Educação", "Estado Emocional", "Observações Gerais"};
        }
    }
}
