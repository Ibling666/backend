package com.usei.usei.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Provee un bean "fallback" de JavaMailSender en caso de que no exista uno
 * en el entorno (por ejemplo cuando se usa la API de Resend en lugar de SMTP).
 * Esto evita que Spring falle al arrancar cuando otras clases todavía dependen
 * de la interfaz `JavaMailSender`.
 */
@Configuration
public class MailConfig {

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl impl = new JavaMailSenderImpl();
        // Valores mínimos: no se intentará enviar realmente si las clases usan Resend.
        impl.setHost("localhost");
        impl.setPort(25);
        return impl;
    }
}
