package io.proj3ct.SpringDemoBot.service;

import io.proj3ct.SpringDemoBot.entity.Anime;
import io.proj3ct.SpringDemoBot.entity.BotUser;
import io.proj3ct.SpringDemoBot.repository.IAnimeRepository;
import io.proj3ct.SpringDemoBot.repository.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MessageService {
    private static final String TV_BUTTON = "TV_BUTTON";
    private static final String MOVIE_BUTTON = "MOVIE_BUTTON";

    private static final String DEFAULT_KIND = "tv";
    private static final String DEFAULT_STATUS = "released";

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IAnimeRepository animeRepository;

    public BotApiMethod<?> processUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            return processCallbackQuery(update.getCallbackQuery());
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            return processMessage(update.getMessage());
        }
        return null;
    }

    private BotApiMethod<?> processCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        Long chatId = callbackQuery.getMessage().getChatId();

        BotUser user = getOrRegisterUser(chatId);

        String answerText = "";
        switch (callbackData) {
            case TV_BUTTON:
                answerText = "Вы выбрали сериалы.";
                user.setKind("tv");
                break;
            case MOVIE_BUTTON:
                answerText = "Вы выбрали фильмы.";
                user.setKind("movie");
                break;
            default:
                answerText = "Неверное действие";
                break;
        }
        userRepository.save(user);

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(String.valueOf(chatId));
        editMessage.setMessageId(messageId);
        editMessage.setText(answerText);

        return editMessage;
    }

    private BotApiMethod<?> processMessage(Message message) {
        String receivedText = message.getText();
        Long chatId = message.getChatId();
        SendMessage response;

        switch (receivedText) {
            case "/start": {
                String name = message.getFrom().getFirstName();
                response = createMessage(chatId, "Привет, " + name + "! 😊");
                registerUser(new BotUser(chatId, DEFAULT_STATUS, DEFAULT_KIND));
                break;
            }
            case "/settings": {
                response = createMessage(chatId, "Выберите тип аниме.");
                addKindButtons(response);
                break;
            }
            case "/anime": {
                BotUser user = getOrRegisterUser(chatId);
                String kind = user.getKind();
                String status = user.getStatus();

                Optional<Anime> animeOptional = animeRepository.findByRandom(kind, status);
                if (animeOptional.isPresent()) {
                    Anime anime = animeOptional.get();
                    response = createMessage(chatId, formatAnimeInformation(anime));
                } else {
                    response = createMessage(chatId, "Аниме не найдено 😢");
                }
                break;
            }
            default: {
                response = createMessage(chatId, "Я не знаю такую команду 😕");
                break;
            }
        }
        return response;
    }

    private SendMessage createMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        return message;
    }

    private void registerUser(BotUser botUser) {
        if (userRepository.findById(botUser.getChatId()).isEmpty()) {
            userRepository.save(botUser);
            log.info("User saved: " + botUser);
        }
    }

    private BotUser getOrRegisterUser(Long chatId) {
        return userRepository.findById(chatId)
                .orElseGet(() -> {
                    BotUser botUser = new BotUser(chatId, DEFAULT_STATUS, DEFAULT_KIND);
                    registerUser(botUser);
                    return botUser;
                });
    }

    private void addKindButtons(SendMessage message) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        var serialButton = new InlineKeyboardButton();
        serialButton.setText("Сериал");
        serialButton.setCallbackData(TV_BUTTON);

        var filmButton = new InlineKeyboardButton();
        filmButton.setText("Фильм");
        filmButton.setCallbackData(MOVIE_BUTTON);

        row.add(serialButton);
        row.add(filmButton);
        rows.add(row);
        markupInline.setKeyboard(rows);
        message.setReplyMarkup(markupInline);
    }

    private String formatAnimeInformation(Anime anime) {
        return String.format(
                "🎥 *%s* (%s)\n\n" +
                        "⭐ Рейтинг: %.2f\n" +
                        "📺 Тип: %s\n" +
                        "📅 Статус: %s\n" +
                        "🎬 Эпизоды: %d\n\n" +
                        "[Подробнее] (https://shikimori.one%s)",
                anime.getName(),
                anime.getRussian().isEmpty() ? "нет перевода" : anime.getRussian(),
                anime.getScore(),
                anime.getKind(),
                anime.getStatus(),
                anime.getEpisodes(),
                anime.getUrl()
        );
    }
}
