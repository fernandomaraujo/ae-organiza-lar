package com.aelares.service;

import com.aelares.dto.response.WeeklyPlanResponse;
import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class EmailService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Inject
    Mailer mailer;

    /**
     * Envia email com código OTP
     */
    public void sendOtpEmail(String to, String code) {
        try {
            String html = """
                <html><body style="font-family:sans-serif;max-width:480px;margin:0 auto;padding:24px">
                  <h2 style="color:#2c3e50">Seu código de acesso</h2>
                  <p>Use este código para acessar sua conta <strong>AE Organiza Lar</strong>:</p>
                  <div style="background:#f0f4ff;border-radius:8px;padding:20px 32px;text-align:center;margin:24px 0">
                    <span style="color:#3b82f6;font-size:36px;font-weight:bold;letter-spacing:8px">%s</span>
                  </div>
                  <p style="color:#666">Este código expira em <strong>15 minutos</strong>.</p>
                  <p style="color:#999;font-size:12px">Se você não solicitou este código, ignore este e-mail.</p>
                  <hr style="border:none;border-top:1px solid #eee;margin:24px 0">
                  <p style="color:#aaa;font-size:11px">AE Organiza Lar · Gerenciador de Afazeres Domésticos</p>
                </body></html>
                """.formatted(code);

            mailer.send(Mail.withHtml(to, "Seu código de acesso — AE Organiza Lar", html)
                .setFrom("noreply@aelares.com"));
        } catch (Exception e) {
            Log.errorf("Erro ao enviar OTP para %s: %s", to, e.getMessage());
        }
    }

    /**
     * Envia email com resumo semanal (Fase 2)
     */
    public void sendWeeklyReminderEmail(String to, String userName, List<WeeklyPlanResponse> weeklyPlans) {
        try {
            String html = buildWeeklyHtml(userName, weeklyPlans);
            String subject = "📋 Seu planejamento semanal — AE Organiza Lar";

            mailer.send(Mail.withHtml(to, subject, html)
                .setFrom("noreply@aelares.com"));

            Log.infof("E-mail semanal enviado para %s", to);
        } catch (Exception e) {
            Log.errorf("Erro ao enviar e-mail semanal para %s: %s", to, e.getMessage());
        }
    }

    private String buildWeeklyHtml(String userName, List<WeeklyPlanResponse> weeklyPlans) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            <html><body style="font-family:sans-serif;max-width:600px;margin:0 auto;padding:24px;color:#2c3e50">
              <h2 style="color:#2c3e50">📋 Planejamento da Semana</h2>
              <p>Olá, <strong>%s</strong>! Aqui está o que você planejou para esta semana:</p>
            """.formatted(userName));

        if (weeklyPlans == null || weeklyPlans.isEmpty()) {
            sb.append("<p style='color:#999'>Nenhum afazer cadastrado ainda. Acesse o app para começar a organizar!</p>");
        } else {
            for (WeeklyPlanResponse plan : weeklyPlans) {
                sb.append("""
                    <div style="margin:20px 0">
                      <h3 style="color:#3b82f6;margin-bottom:4px">🏠 %s</h3>
                      <p style="color:#999;font-size:12px;margin-top:0">%s a %s</p>
                    """.formatted(
                        plan.homeName,
                        plan.weekStart.format(DATE_FMT),
                        plan.weekEnd.format(DATE_FMT)
                    ));

                boolean hasTasks = plan.days.stream().anyMatch(d -> !d.tasks.isEmpty());
                if (!hasTasks) {
                    sb.append("<p style='color:#aaa;font-size:13px'>Sem tarefas cadastradas para esta semana.</p>");
                } else {
                    for (WeeklyPlanResponse.DayPlanResponse day : plan.days) {
                        if (day.tasks.isEmpty()) continue;

                        sb.append("""
                            <div style="margin:12px 0;border-left:4px solid #3b82f6;padding-left:12px">
                              <strong style="font-size:14px">%s · %s</strong>
                              <ul style="margin:6px 0;padding-left:16px">
                            """.formatted(day.dayName, day.date.format(DATE_FMT)));

                        for (WeeklyPlanResponse.TaskDayResponse task : day.tasks) {
                            String badge = priorityBadge(task.priority);
                            sb.append("<li style='margin:4px 0;font-size:13px'>%s %s %s</li>"
                                .formatted(badge, task.title,
                                    task.notes != null && !task.notes.isBlank()
                                        ? "<span style='color:#999'>— " + task.notes + "</span>" : ""));
                        }

                        sb.append("</ul></div>");
                    }
                }

                sb.append("</div><hr style='border:none;border-top:1px solid #eee'>");
            }
        }

        sb.append("""
              <p style="margin-top:24px;font-size:13px;color:#666">
                Bom trabalho esta semana! 💪
              </p>
              <hr style="border:none;border-top:1px solid #eee;margin:24px 0">
              <p style="color:#aaa;font-size:11px">AE Organiza Lar · Gerenciador de Afazeres Domésticos</p>
            </body></html>
            """);

        return sb.toString();
    }

    private String priorityBadge(String priority) {
        if (priority == null) return "";
        return switch (priority.toUpperCase(Locale.ROOT)) {
            case "HIGH"   -> "<span style='color:#ef4444;font-weight:bold'>[Alta]</span>";
            case "LOW"    -> "<span style='color:#10b981'>[Baixa]</span>";
            default       -> "<span style='color:#f59e0b'>[Média]</span>";
        };
    }
}
