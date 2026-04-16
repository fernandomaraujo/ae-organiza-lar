package com.aelares.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    /**
     * Envia email com código OTP
     */
    public void sendOtpEmail(String to, String code) {
        try {
            String html = String.format("""
                <html>
                    <body>
                        <h2>Seu código de acesso</h2>
                        <p>Use este código para acessar sua conta AE Organiza Lar:</p>
                        <h1 style="color: #007bff; font-size: 32px; letter-spacing: 5px;">%s</h1>
                        <p>Este código expira em 15 minutos.</p>
                        <p>Se não solicitou este código, ignore este email.</p>
                        <hr>
                        <p style="color: #666; font-size: 12px;">AE Organiza Lar - Gerenciador de Afazeres Domésticos</p>
                    </body>
                </html>
                """, code);

            Mail mail = new Mail()
                .setFrom("noreply@aelares.com")
                .addTo(to)
                .setSubject("Seu código de acesso - AE Organiza Lar")
                .setHtml(html);

            mailer.send(mail);
        } catch (Exception e) {
            System.err.println("Erro ao enviar email para " + to + ": " + e.getMessage());
            // Log error but don't throw to not interrupt flow
        }
    }

    /**
     * Envia email com resumo semanal (Fase 2)
     */
    public void sendWeeklyReminderEmail(String to, String userName, Object tasksData) {
        try {
            String html = String.format("""
                <html>
                    <body>
                        <h2>Seu planejamento semanal</h2>
                        <p>Olá %s,</p>
                        <p>Aqui está seu planejamento para a próxima semana:</p>
                        <!-- Tasks will be rendered here in Phase 2 -->
                        <p>Acesse sua conta para gerenciar suas tarefas.</p>
                        <hr>
                        <p style="color: #666; font-size: 12px;">AE Organiza Lar - Gerenciador de Afazeres Domésticos</p>
                    </body>
                </html>
                """, userName);

            Mail mail = new Mail()
                .setFrom("noreply@aelares.com")
                .addTo(to)
                .setSubject("Seu planejamento semanal - AE Organiza Lar")
                .setHtml(html);

            mailer.send(mail);
        } catch (Exception e) {
            System.err.println("Erro ao enviar email semanal para " + to + ": " + e.getMessage());
        }
    }
}

