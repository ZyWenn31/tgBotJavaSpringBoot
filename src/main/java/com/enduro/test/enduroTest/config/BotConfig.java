package com.enduro.test.enduroTest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("application.yml")
public class BotConfig {
    @Value("${bot.name}")
    String BotName;

    @Value("${bot.token}")
    String BotToken;

    public BotConfig() {
    }

    public String getBotName() {
        return BotName;
    }

    public String getBotToken() {
        return BotToken;
    }
}
