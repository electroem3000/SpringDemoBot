package io.proj3ct.SpringDemoBot.service;

import com.vdurmont.emoji.EmojiParser;
import io.proj3ct.SpringDemoBot.entity.BotUser;
import io.proj3ct.SpringDemoBot.repository.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MessageService {
    private static final String SERIAL_BUTTON = "SERIAL_BUTTON";
    private static final String FILM_BUTTON = "FILM_BUTTON";
    private static final String NONE_BUTTON = "NONE_BUTTON";

    @Autowired
    private IUserRepository userRepository;

    public SendMessage messageReceiver(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String receivedText = update.getMessage().getText();
            String name = update.getMessage().getFrom().getFirstName();
            Long chatId = update.getMessage().getChatId();

            SendMessage answer;
            switch (receivedText) {
                case "/start": {
                    String answerText = EmojiParser.parseToUnicode("Привет, " + name + "! " + ":blush:");
                    answer = createMessage(chatId, answerText);
                    registerUser(update.getMessage());
                    break;
                }
                case "/settings": {
                    answer = createMessage(chatId, "Выберите тип аниме.");
                    chooseType(answer);
                    break;
                }
                default: {
                    String answerText = EmojiParser.parseToUnicode("Я не знаю такую команду " + ":confused:");
                    answer = createMessage(chatId, answerText);
                    break;
                }
            }
            return answer;
        }
        return null;
    }

    public EditMessageText messageEditor(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        String answerText = "";
        switch (callbackData) {
            case SERIAL_BUTTON:
                answerText = "Вы выбрали сериалы.";
                break;
            case FILM_BUTTON:
                answerText = "Вы выбрали фильмы";
                break;
            case NONE_BUTTON:
                answerText = "Вы выбрали любой тип";
                break;
            default:
                break;
        }
        EditMessageText answer = new EditMessageText();
        answer.setChatId(String.valueOf(chatId));
        answer.setText(answerText);
        answer.setMessageId(messageId);
        return answer;
    }

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

    private SendMessage createMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        return message;
    }

    private void chooseType(SendMessage message) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        var serialButton = new InlineKeyboardButton();
        serialButton.setText("Сериал");
        serialButton.setCallbackData(SERIAL_BUTTON);

        var filmButton = new InlineKeyboardButton();
        filmButton.setText("Фильм");
        filmButton.setCallbackData(FILM_BUTTON);

        var noneButton = new InlineKeyboardButton();
        noneButton.setText("Любой");
        noneButton.setCallbackData(NONE_BUTTON);

        row.add(serialButton);
        row.add(filmButton);
        row.add(noneButton);
        rows.add(row);
        markupInline.setKeyboard(rows);
        message.setReplyMarkup(markupInline);
    }
}
