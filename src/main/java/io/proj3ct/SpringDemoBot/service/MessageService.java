package io.proj3ct.SpringDemoBot.service;

import com.vdurmont.emoji.EmojiParser;
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
    private static final String NONE_BUTTON = "NONE_BUTTON";

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
                    String answerText = EmojiParser.parseToUnicode("Привет, " + name + "! " + ":blush:");
                    answer = createMessage(chatId, answerText);
                    registerUser(update.getMessage());
                    break;
                }
                case "/settings": {
                    answer = createMessage(chatId, "Выберите тип аниме.");
                    chooseKind(answer);
                    break;
                }
                case "/anime": {
                    long count = animeRepository.count();
                    if (count == 0) {
                        answer = createMessage(chatId, "В базе данных пока нет аниме 😢");
                        break;
                    }

                    Random random = new Random(); //todo
                    long id = random.nextLong(count) + 1;

                    Optional<Anime> animeOptional = animeRepository.findById(String.valueOf(id));
                    if (animeOptional.isPresent()) {
                        Anime anime = animeOptional.get();
                        answer = createMessage(chatId, formatAnimeInformation(anime));
                    } else {
                        answer = createMessage(chatId, "Аниме не найдено 😢");
                    }
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
            case TV_BUTTON:
                answerText = "Вы выбрали сериалы.";
                break;
            case MOVIE_BUTTON:
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
            botUser.setKind("None");
            botUser.setStatus("None");

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

    private void chooseKind(SendMessage message) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        var serialButton = new InlineKeyboardButton();
        serialButton.setText("Сериал");
        serialButton.setCallbackData(TV_BUTTON);

        var filmButton = new InlineKeyboardButton();
        filmButton.setText("Фильм");
        filmButton.setCallbackData(MOVIE_BUTTON);

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
