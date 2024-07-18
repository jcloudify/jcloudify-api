package api.jcloudify.app.service;

import com.stripe.Stripe;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class StripeConf {
    private String secretKey;

    @Value("${stripe.secret.key}")
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        Stripe.apiKey = secretKey;
    }
}
