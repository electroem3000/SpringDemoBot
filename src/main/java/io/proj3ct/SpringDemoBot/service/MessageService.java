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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessageService {
    private static final String TV_BUTTON = "TV_BUTTON";
    private static final String MOVIE_BUTTON = "MOVIE_BUTTON";
    private static final String RELEASED_BUTTON = "RELEASED_BUTTON";
    private static final String ONGOING_BUTTON = "ONGOING_BUTTON";
    private static final String ANONS_BUTTON = "ANONS_BUTTON";

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
                answerText = "Вы выбрали TV (многосерийные аниме).";
                user.setKind("tv");
                break;
            case MOVIE_BUTTON:
                answerText = "Вы выбрали movie (полнометражные аниме).";
                user.setKind("movie");
                break;
            case RELEASED_BUTTON:
                answerText = "Вы выбрали released (полностью вышедшие аниме)";
                user.setStatus("released");
                break;
            case ONGOING_BUTTON:
                answerText = "Вы выбрали ongoing (аниме, которые выходят прямо сейчас)";
                user.setStatus("ongoing");
                break;
            case ANONS_BUTTON:
                answerText = "Вы выбрали  anons (аниме, которые только планируются к выпуску)";
                user.setStatus("anons");
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
                response = createMessage(chatId, BotMessages.START_MESSAGE);
                registerUser(new BotUser(chatId, DEFAULT_STATUS, DEFAULT_KIND));
                break;
            }
            case "/kind": {
                response = createMessage(chatId, BotMessages.CHOOSE_ANIME_KIND);
                addKindButtons(response);
                break;
            }
            case "/status": {
                response = createMessage(chatId, BotMessages.CHOOSE_ANIME_STATUS);
                addStatusButtons(response);
                break;
            }
            case "/anime": {
                BotUser user = getOrRegisterUser(chatId);
                String kind = user.getKind();
                String status = user.getStatus();

                Optional<Anime> animeOptional = animeRepository.findByRandom(kind, status);
                if (animeOptional.isPresent()) {
                    Anime anime = animeOptional.get();
                    response = createMessage(chatId, BotMessages.getAnimeInformationText(anime));
                } else {
                    response = createMessage(chatId, "Аниме не найдено 😢");
                }
                break;
            }
            case "/mypreferences": {
                BotUser user = getOrRegisterUser(chatId);
                response = createMessage(chatId, BotMessages.getUserPreferencesText(user));
                break;
            }
            case "/help": {
                response = createMessage(chatId, BotMessages.HELP_MESSAGE);
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


    private void addStatusButtons(SendMessage message) {
        message.setReplyMarkup(createInlineKeyboard(
                List.of(
                        createButton("Released", RELEASED_BUTTON),
                        createButton("Ongoing", ONGOING_BUTTON),
                        createButton("Anons", ANONS_BUTTON)
                )
        ));
    }

    private void addKindButtons(SendMessage message) {
        message.setReplyMarkup(createInlineKeyboard(
                List.of(
                        createButton("TV", TV_BUTTON),
                        createButton("Movie", MOVIE_BUTTON)
                )
        ));
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private InlineKeyboardMarkup createInlineKeyboard(List<InlineKeyboardButton> buttons) {
        List<List<InlineKeyboardButton>> rows = buttons.stream()
                .map(List::of)
                .collect(Collectors.toList());

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
