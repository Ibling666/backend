package com.usei.usei;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.usei.usei.util.JwtAuthInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/documents/**") // Mapeo unificado para documentos
                .addResourceLocations("classpath:/static/documents/");
        
        // Mantener compatibilidad con tu frontend actual si pide /imagenes/
        registry.addResourceHandler("/imagenes/**")
                .addResourceLocations("classpath:/static/documents/imagenes/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**") // Aplica a todo por defecto
                .excludePathPatterns(
                        // Rutas de autenticación
                        "/usuario/login",
                        "/usuario/register",
                        "/auth/**",

                        // Rutas públicas específicas (Aquí solucionamos el 401)
                        "/rol/**",
                        "/usuario/**",

                        // APIs públicas (se permiten todas las rutas de la carpeta api)
                        "/certificado/**",
                        "/configuracion-seguridad/**",
                        "/encuesta/**",
                        "/encuesta-gestion/**",
                        "/estado-certificado/**",
                        "/estado-encuesta/**",
                        "/estudiante/**",
                        "/indicador-riesgo/**",
                        "/noticia/**",
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

                        // Utilidades y health check
                        "/configuracion-seguridad/ping",
                        "/ping",
                        "/",
                        "/error",

                        // Recursos estáticos
                        "/documents/**",
                        "/imagenes/**",
                        "/favicon.ico"
                );
    }
}
