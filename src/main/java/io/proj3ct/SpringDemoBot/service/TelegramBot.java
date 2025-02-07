package io.proj3ct.SpringDemoBot.service;

import io.proj3ct.SpringDemoBot.configuration.properties.BotProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotProperties botProperties;
    private final MessageService messageService;

    @Autowired
    public TelegramBot(BotProperties botProperties, MessageService messageService) {

        this.botProperties = botProperties;
        this.messageService = messageService;

        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "зарегистрироваться"));
        commands.add(new BotCommand("/help", "вынести окно с командами"));
        commands.add(new BotCommand("/mypreferences", "показать сохраненные предпочтения"));
        commands.add(new BotCommand("/anime", "посоветовать аниме"));
        commands.add(new BotCommand("/settings", "установить свои настройки"));
        try {
            execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка при задании списка команд: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            executeMessage(update);
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

    private void executeMessage(Update update) throws TelegramApiException {
        if (update.hasMessage()) {
            execute(messageService.messageReceiver(update));
        } else if (update.hasCallbackQuery()) {
            execute(messageService.messageEditor(update));
        }
    }
}
