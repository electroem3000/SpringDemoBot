package io.proj3ct.SpringDemoBot.service;

import io.proj3ct.SpringDemoBot.configuration.properties.BotProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotProperties botProperties;
    private final MessageService messageService;

    @Autowired
    public TelegramBot(BotProperties botProperties, MessageService messageService) {

        this.botProperties = botProperties;
        this.messageService = messageService;

    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            SendMessage message = messageService.messageReceiver(update);
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при ответе пользователю!");
        }
    }

    @Override
    public String getBotUsername() {
        return botProperties.name();
    }

    @Override
    public String getBotToken() {
        return botProperties.token();
    }
}
