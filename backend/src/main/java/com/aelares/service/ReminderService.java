package com.aelares.service;

import com.aelares.dto.response.WeeklyPlanResponse;
import com.aelares.entity.Home;
import com.aelares.entity.User;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ReminderService {

    @Inject
    EmailService emailService;

    @Inject
    TaskCompletionService taskCompletionService;

    /**
     * Dispara toda segunda-feira às 07:00 (horário do servidor).
     * Envia o planejamento da semana para todos os usuários com lares cadastrados.
     */
    @Scheduled(cron = "0 0 7 ? * MON")
    public void sendWeeklyReminders() {
        Log.info("Iniciando envio de lembretes semanais...");

        List<User> users = User.listAll();
        int sent = 0;
        int skipped = 0;

        for (User user : users) {
            try {
                List<Home> homes = Home.findByUserId(user.id);
                if (homes.isEmpty()) {
                    skipped++;
                    continue;
                }

                sendReminderToUser(user, homes);
                sent++;
            } catch (Exception e) {
                Log.errorf("Falha ao processar lembrete para usuário %s: %s", user.email, e.getMessage());
            }
        }

        Log.infof("Lembretes semanais: %d enviados, %d ignorados (sem lares).", sent, skipped);
    }

    /**
     * Envia o lembrete para um usuário específico — chamável manualmente ou via API.
     */
    public void sendReminderToUser(User user, List<Home> homes) {
        LocalDate today = LocalDate.now();
        List<WeeklyPlanResponse> plans = new ArrayList<>();

        for (Home home : homes) {
            try {
                WeeklyPlanResponse plan = taskCompletionService.getWeeklyPlan(home.id, user.id, today);
                plans.add(plan);
            } catch (Exception e) {
                Log.warnf("Erro ao montar plano do lar %s para %s: %s", home.name, user.email, e.getMessage());
            }
        }

        emailService.sendWeeklyReminderEmail(user.email, user.name, plans);
    }
}

