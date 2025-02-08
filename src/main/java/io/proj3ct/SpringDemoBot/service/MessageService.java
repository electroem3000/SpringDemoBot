package io.proj3ct.SpringDemoBot.service;

import io.proj3ct.SpringDemoBot.entity.Anime;
import io.proj3ct.SpringDemoBot.entity.BotUser;
import io.proj3ct.SpringDemoBot.repository.IAnimeRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

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

    public SendMessage messageReceiver(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String receivedText = update.getMessage().getText();
            String name = update.getMessage().getFrom().getFirstName();
            Long chatId = update.getMessage().getChatId();

            SendMessage answer;
            switch (receivedText) {
                case "/start": {
                    answer = createMessage(chatId, "Привет, " + name + "! 😊");
                    registerUser(new BotUser(chatId, DEFAULT_STATUS, DEFAULT_KIND));
                    break;
                }
                case "/settings": {
                    answer = createMessage(chatId, "Выберите тип аниме.");
                    addKindButtons(answer);
                    break;
                }
                case "/anime": {
                    BotUser user = userRepository.findById(chatId).orElseGet(() -> {
                        BotUser botUser = new BotUser(chatId, DEFAULT_STATUS, DEFAULT_KIND);
                        registerUser(botUser);
                        return botUser;
                    });

                    String kind = user.getKind();
                    String status = user.getStatus();

                    Optional<Anime> animeOptional = animeRepository.findByRandom(kind, status);
                    if (animeOptional.isPresent()) {
                        Anime anime = animeOptional.get();
                        answer = createMessage(chatId, formatAnimeInformation(anime));
                    } else {
                        answer = createMessage(chatId, "Аниме не найдено 😢");
                    }
                    break;
                }
                default: {
                    answer = createMessage(chatId, "Я не знаю такую команду 😕");
                    break;
                }
            }
            return answer;
        }
        return null;
    }


    // todo
    public EditMessageText messageEditor(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        BotUser user = userRepository.findById(chatId).orElseGet(() -> {
            BotUser botUser = new BotUser(chatId, DEFAULT_STATUS, DEFAULT_KIND);
            registerUser(botUser);
            return botUser;
        });

        String answerText = "";
        switch (callbackData) {
            case TV_BUTTON:
                answerText = "Вы выбрали сериалы.";
                user.setKind("tv");
                break;
            case MOVIE_BUTTON:
                answerText = "Вы выбрали фильмы";
                user.setKind("movie");
                break;
            default:
                break;
        }
        userRepository.save(user);
        EditMessageText answer = new EditMessageText();
        answer.setChatId(String.valueOf(chatId));
        answer.setText(answerText);
        answer.setMessageId(messageId);
        return answer;
    }

    private void registerUser(BotUser botUser) {
        if (userRepository.findById(botUser.getChatId()).isEmpty()) {
            userRepository.save(botUser);
            log.info("User saved: " + botUser);
        }
    }

    private SendMessage createMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        return message;
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
