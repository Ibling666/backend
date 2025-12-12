package com.usei.usei.dto.response;

import java.util.List;
import java.util.Map;

public class LoginResponseDTO {
    private String status;
    private String message;
    private String token;
    private int expiresIn;
    private Map<String, Object> data;
    private List<String> accesos;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String status, String message, String token, 
                           int expiresIn, Map<String, Object> data, List<String> accesos) {
        this.status = status;
        this.message = message;
        this.token = token;
        this.expiresIn = expiresIn;
        this.data = data;
        this.accesos = accesos;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public List<String> getAccesos() {
        return accesos;
    }

    public void setAccesos(List<String> accesos) {
        this.accesos = accesos;
    }
}