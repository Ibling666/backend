package com.usei.usei.util;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs REST
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Usar nuestra config de CORS
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                        "/auth/**",
                        "/configuracion-seguridad/**",
                        "/configuracion-seguridad/ping",
                        "/ping",
                        "/",
                        "/usuario/**",
                        "/rol/**",
                        "/noticia/**",
                        "/log-usuario/**",
                        "/certificado/**",
                        "/encuesta/**",
                        "/encuesta-gestion/**",
                        "/estado-certificado/**",
                        "/estado-encuesta/**",
                        "/estudiante/**",
                        "/indicador-riesgo/**",
                        "/notificacion/**",
                        "/opciones-pregunta/**",
                        "/parametros-aviso/**",
                        "/plazo/**",
                        "/pregunta/**",
                        "/reporte/**",
                        "/respuesta/**",
                        "/riesgo-evento/**",
                        "/soporte/**",
                        "/tipo-notificacion/**",
                        "/tipo-problema/**",
                        "/api/**",
                        "/imagenes/**",
                        "/documents/**")
                    .permitAll() // Permite acceso público a estas rutas
                    .anyRequest().authenticated() // Todo lo demás requiere autenticación
                )
                .httpBasic(basic -> basic.disable()); // Desactivar Basic Auth para evitar pop-ups del navegador

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:5173", // Desarrollo local
                "https://usei-seguridad-back-production.up.railway.app", // Tu propio backend
                // 👇 Agrega AQUÍ la URL principal que te dio Vercel (la corta)
                "https://usei-seguridad-front.vercel.app",
                // Esta es la específica que tenías, puedes dejarla por si acaso:
                "https://usei-seguridad-front-8yvz7pc4r-ignacios-projects-1b5e3921.vercel.app"));

        // Métodos permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Headers permitidos: Usa "*" para evitar problemas con headers que envíe Axios
        // automáticamente
        config.setAllowedHeaders(List.of("*"));

        // Exponer headers si es necesario (opcional, pero ayuda a veces con el token)
        config.setExposedHeaders(List.of("Authorization"));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
