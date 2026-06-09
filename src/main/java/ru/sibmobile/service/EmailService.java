package ru.sibmobile.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String username,
                        @Value("${spring.mail.from:${spring.mail.username}}") String from) {
        this.mailSender = mailSender;
        this.fromAddress = (from != null && !from.isBlank()) ? from : username;
    }

    public boolean sendVerificationCode(String to, String code) {
        return send(to, "СибМобиль - Код подтверждения регистрации",
                "Ваш код подтверждения: " + code + "\n\nКод действителен 10 минут.\n\nСибМобиль",
                "verification", code);
    }

    public boolean sendPasswordResetCode(String to, String code) {
        return send(to, "СибМобиль - Восстановление пароля",
                "Вы запросили восстановление пароля.\n\n" +
                        "Ваш одноразовый код: " + code + "\n\n" +
                        "Если вы не запрашивали восстановление, проигнорируйте это письмо.\n\nСибМобиль",
                "password-reset", code);
    }

    public void sendOrderConfirmation(String to,
                                      ru.sibmobile.dto.OrderForm form,
                                      ru.sibmobile.service.TariffService.CarPrice price,
                                      ru.sibmobile.model.TariffType tariff) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ваш заказ принят в обработку.\n\n");
        sb.append("Авто: ").append(form.getCarType().getDisplayName()).append("\n");
        sb.append("Тариф: ").append(tariff.name()).append("\n\n");
        sb.append("Предоплата: ").append((int) price.getPrepayment()).append(" ₽").append("\n");
        sb.append("Стоимость за км: ").append(String.format(java.util.Locale.US, "%.2f", price.getPerKm())).append(" ₽/км\n\n");
        sb.append("Имя: ").append(form.getFirstName()).append("\n");
        sb.append("Фамилия: ").append(form.getLastName()).append("\n");
        sb.append("Телефон: ").append(form.getPhone()).append("\n");
        sb.append("Город: ").append(form.getCity()).append("\n");
        sb.append("Адрес: ").append(form.getAddress()).append("\n\n");
        sb.append("Начало проката: ").append(form.getStartDateTime()).append("\n");
        sb.append("Окончание проката: ").append(form.getEndDateTime()).append("\n\n");
        sb.append("Спасибо, что выбираете СибМобиль!");
        send(to, "СибМобиль - Заказ принят", sb.toString(), "order", null);
    }

    public void sendBlockNotification(String to, String reason) {
        send(to, "СибМобиль - Учётная запись заблокирована",
                "Ваша учётная запись в СибМобиль заблокирована.\n\nПричина: " +
                        (reason != null && !reason.isBlank() ? reason : "не указана") +
                        "\n\nПо вопросам разблокировки обратитесь в поддержку.",
                "block", null);
    }

    public void sendRestrictNotification(String to, String reason, java.time.LocalDateTime restrictedUntil) {
        send(to, "СибМобиль - Временное ограничение доступа",
                "Доступ к вашему аккаунту СибМобиль временно ограничен до: " + restrictedUntil + "\n\nПричина: " +
                        (reason != null && !reason.isBlank() ? reason : "не указана") +
                        "\n\nПосле указанной даты доступ будет восстановлен.",
                "restrict", null);
    }

    private boolean send(String to, String subject, String text, String kind, String codeForLog) {
        if (to == null || to.isBlank()) {
            log.warn("Skip {} email: empty recipient", kind);
            return false;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to.trim());
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email [{}] sent to {}", kind, to);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email [{}] to {}: {}", kind, to, e.getMessage());
            if (codeForLog != null) {
                log.warn("Код {} для {} (если SMTP недоступен — проверьте настройки почты в application.properties)", kind, to);
            }
            return false;
        }
    }
}
