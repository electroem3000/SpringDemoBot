package io.proj3ct.SpringDemoBot.service;

import io.proj3ct.SpringDemoBot.entity.BotUser;
import io.proj3ct.SpringDemoBot.repository.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.sql.Timestamp;

@Service
@Slf4j
public class MessageService {
    @Autowired
    private IUserRepository userRepository;

    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            BotUser botUser = new BotUser();
            botUser.setChatId(message.getChatId());
            botUser.setUsername(message.getFrom().getUserName());
            botUser.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(botUser);
            log.info("user saved: " + botUser);
        }
    }

    public SendMessage messageReceiver(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String receivedText = update.getMessage().getText();
            String name = update.getMessage().getFrom().getUserName();
            Long chatId = update.getMessage().getChatId();

            String answerText;
            switch (receivedText) {
                case "/start":
                    answerText = String.format("Привет, %s!", name);
                    registerUser(update.getMessage());
                    break;
                case "/stop":
                    answerText = String.format("Пока, %s!", name);
                    break;
                default:
                    answerText = "Я не знаю такую команду :(";
                    break;
            }
            SendMessage answer = new SendMessage();
            answer.setChatId(String.valueOf(chatId));
            answer.setText(answerText);
            return answer;
        }
        return null;
    }
}
