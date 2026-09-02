package com.thechoicecompany.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    private String frontendUrl;

    private Email email = new Email();
    private Whatsapp whatsapp = new Whatsapp();
    private Aws aws = new Aws();
    private Recaptcha recaptcha = new Recaptcha();
    private Razorpay razorpay = new Razorpay();

    @Data public static class Email {
        private String from;
        private String sales;
    }
    @Data public static class Whatsapp {
        private String token;
        private String phoneId;
        private String salesNumber;
    }
    @Data public static class Aws {
        private String accessKey;
        private String secretKey;
        private String region;
        private String bucket;
    }
    @Data public static class Recaptcha {
        private String secret;
        private String verifyUrl;
        private Double minScore;
    }
    @Data public static class Razorpay {
        private String keyId;
        private String keySecret;
    }
}
