package ru.sibmobile.service;



import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.scheduling.annotation.Async;

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



    @Async("mailExecutor")

    public void sendVerificationCodeAsync(String to, String code) {

        sendVerificationCode(to, code);

    }



    @Async("mailExecutor")

    public void sendPasswordResetCodeAsync(String to, String code) {

        sendPasswordResetCode(to, code);

    }



    public boolean sendVerificationCode(String to, String code) {

        return send(to, "СибМобиль — код: " + code,

                "Код подтверждения: " + code + "\n\nДействует 10 минут.\n\nСибМобиль",

                "verification", code, true);

    }



    public boolean sendPasswordResetCode(String to, String code) {

        return send(to, "СибМобиль — код: " + code,

                "Код восстановления пароля: " + code + "\n\nДействует 10 минут.\n" +

                        "Если вы не запрашивали сброс, проигнорируйте письмо.\n\nСибМобиль",

                "password-reset", code, true);

    }



    @Async("mailExecutor")
    public void sendOrderConfirmationAsync(String to,
                                           ru.sibmobile.dto.OrderForm form,
                                           ru.sibmobile.service.TariffService.CarPrice price,
                                           ru.sibmobile.model.TariffType tariff,
                                           ru.sibmobile.model.Car assignedCar) {
        sendOrderConfirmation(to, form, price, tariff, assignedCar);
    }

    @Async("mailExecutor")
    public void sendOrderReceiptAsync(String to,
                                      ru.sibmobile.dto.OrderForm form,
                                      ru.sibmobile.service.TariffService.CarPrice price,
                                      ru.sibmobile.model.TariffType tariff,
                                      ru.sibmobile.model.Car assignedCar) {
        sendOrderReceipt(to, form, price, tariff, assignedCar);
    }

    public void sendOrderConfirmation(String to,
                                      ru.sibmobile.dto.OrderForm form,
                                      ru.sibmobile.service.TariffService.CarPrice price,
                                      ru.sibmobile.model.TariffType tariff,
                                      ru.sibmobile.model.Car assignedCar) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ваш заказ принят в обработку.\n\n");
        sb.append("Авто: ").append(form.getCarType().getDisplayName()).append("\n");
        if (assignedCar != null) {
            sb.append("Госномер: ").append(assignedCar.getPlateNumber()).append("\n");
            sb.append("Город выдачи: ").append(assignedCar.getCity()).append("\n");
        }
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

        send(to, "СибМобиль - Заказ принят", sb.toString(), "order", null, false);

    }



    public void sendOrderReceipt(String to,
                                 ru.sibmobile.dto.OrderForm form,
                                 ru.sibmobile.service.TariffService.CarPrice price,
                                 ru.sibmobile.model.TariffType tariff,
                                 ru.sibmobile.model.Car assignedCar) {
        StringBuilder sb = new StringBuilder();
        sb.append("ЧЕК\n");
        sb.append("══════════════════════════════════\n");
        sb.append("СибМобиль — каршеринг\n");
        sb.append("Дата: ").append(java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).append("\n");
        sb.append("──────────────────────────────────\n");
        sb.append("Клиент: ").append(form.getFirstName()).append(" ").append(form.getLastName()).append("\n");
        sb.append("Телефон: ").append(form.getPhone()).append("\n");
        sb.append("Авто: ").append(form.getCarType().getDisplayName()).append("\n");
        if (assignedCar != null) {
            sb.append("Госномер: ").append(assignedCar.getPlateNumber()).append("\n");
        }
        sb.append("Тариф: ").append(tariff.name()).append("\n");

        sb.append("Город: ").append(form.getCity()).append("\n");

        sb.append("Адрес выдачи: ").append(form.getAddress()).append("\n");

        sb.append("Начало: ").append(form.getStartDateTime()).append("\n");

        sb.append("Окончание: ").append(form.getEndDateTime()).append("\n");

        sb.append("──────────────────────────────────\n");

        sb.append("Предоплата: ").append((int) price.getPrepayment()).append(" ₽\n");

        sb.append("Стоимость за км: ").append(String.format(java.util.Locale.US, "%.2f", price.getPerKm())).append(" ₽/км\n");

        sb.append("══════════════════════════════════\n");

        sb.append("Спасибо за заказ!\n");

        send(to, "СибМобиль - Чек по заказу", sb.toString(), "receipt", null, false);

    }



    public void sendBlockNotification(String to, String reason) {

        send(to, "СибМобиль - Учётная запись заблокирована",

                "Ваша учётная запись в СибМобиль заблокирована.\n\nПричина: " +

                        (reason != null && !reason.isBlank() ? reason : "не указана") +

                        "\n\nПо вопросам разблокировки обратитесь в поддержку.",

                "block", null, false);

    }



    public void sendRestrictNotification(String to, String reason, java.time.LocalDateTime restrictedUntil) {

        send(to, "СибМобиль - Временное ограничение доступа",

                "Доступ к вашему аккаунту СибМобиль временно ограничен до: " + restrictedUntil + "\n\nПричина: " +

                        (reason != null && !reason.isBlank() ? reason : "не указана") +

                        "\n\nПосле указанной даты доступ будет восстановлен.",

                "restrict", null, false);

    }



    private boolean send(String to, String subject, String text, String kind, String codeForLog, boolean highPriority) {

        if (to == null || to.isBlank()) {

            log.warn("Skip {} email: empty recipient", kind);

            return false;

        }

        long start = System.currentTimeMillis();

        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromAddress);

            helper.setTo(to.trim());

            helper.setSubject(subject);

            helper.setText(text, false);

            if (highPriority) {

                message.setHeader("X-Priority", "1");

                message.setHeader("Importance", "high");

            }

            mailSender.send(message);

            long smtpMs = System.currentTimeMillis() - start;

            log.info("Email [{}] accepted by SMTP for {} in {} ms (inbox delivery may take 1-2 min via Gmail)",

                    kind, to, smtpMs);

            return true;

        } catch (Exception e) {

            long smtpMs = System.currentTimeMillis() - start;

            log.error("Failed to send email [{}] to {} after {} ms: {}", kind, to, smtpMs, e.getMessage());

            if (codeForLog != null) {

                log.warn("Код {} для {}: {} (проверьте SMTP в application.properties)", kind, to, codeForLog);

            }

            return false;

        }

    }

}


