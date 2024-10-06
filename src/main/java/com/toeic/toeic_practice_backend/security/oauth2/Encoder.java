package com.toeic.toeic_practice_backend.security.oauth2;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Encoder {
    public String encode(String value) {
        try {
            log.info("Encode value: " + value);
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding value: " + value);
            log.error(e.getMessage());
            return value;
        }
    }
}