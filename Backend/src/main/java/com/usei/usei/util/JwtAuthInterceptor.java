package com.usei.usei.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenGenerator tokenGenerator;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // ---------------------------------------------------------
        // 0) PRE-FLIGHT CORS
        // ---------------------------------------------------------
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        final String path = request.getRequestURI();
        final String method = request.getMethod();

        // ---------------------------------------------------------
        // 1) AUDITORÍA (solo mutaciones)
        // ---------------------------------------------------------
        boolean isMutation =
                "POST".equalsIgnoreCase(method)
                        || "PUT".equalsIgnoreCase(method)
                        || "PATCH".equalsIgnoreCase(method)
                        || "DELETE".equalsIgnoreCase(method);

        request.setAttribute("auditEnabled", isMutation);

        // ---------------------------------------------------------
        // 2) RUTAS PÚBLICAS
        // ---------------------------------------------------------

        // Auth / login
        if (path.startsWith("/auth/")
                || path.startsWith("/usuario/enviarCodigoVerificacion")
                || path.startsWith("/estudiante/enviarCodigoVerificacion")) {
            return true;
        }

        // Auditoría pública
        if (path.startsWith("/log-usuario/")) {
            return true;
        }

        // Noticias públicas
        if (path.startsWith("/noticia/")) {
            return true;
        }

        // Recursos estáticos
        if (path.startsWith("/documents/")
                || path.startsWith("/imagenes/")
                || path.equals("/favicon.ico")) {
            return true;
        }

        // ---------------------------------------------------------
        // 3) VALIDACIÓN JWT
        // ---------------------------------------------------------
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"UNAUTHORIZED\",\"message\":\"Token no proporcionado\"}"
            );
            return false;
        }

        Jws<Claims> claims = tokenGenerator.validateAndParseToken(authHeader);
        if (claims == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"UNAUTHORIZED\",\"message\":\"Token inválido o expirado\"}"
            );
            return false;
        }

        request.setAttribute("userId", claims.getBody().get("id"));
        request.setAttribute("userType", claims.getBody().get("type"));
        request.setAttribute("username", claims.getBody().get("username"));

        return true;
    }
}
