package com.usei.usei.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Service
public class CaptchaService {

    private static final String SECRET_KEY = "6LfqqPMrAAAAAO2W80T-sgn-W22Sw050AgNusVRh"; // 🔑 Tu clave secreta
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public boolean verifyCaptcha(String token) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", SECRET_KEY);
            params.add("response", token);

            Map<String, Object> response = restTemplate.postForObject(VERIFY_URL, params, Map.class);
            return (Boolean) response.get("success");
        } catch (Exception e) {
            System.err.println(" Error verificando reCAPTCHA: " + e.getMessage());
            return false;
        }
    }
}
