package io.proj3ct.SpringDemoBot.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.proj3ct.SpringDemoBot.entity.Anime;
import io.proj3ct.SpringDemoBot.repository.IAnimeRepository;
import io.proj3ct.SpringDemoBot.service.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BotConfig {
    private final TelegramBot telegramBot;
    @Autowired
    private IAnimeRepository animeRepository;

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        loadAnime(); //todo

        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
            log.error("Не удалось создать бота");
        }
    }

    private void loadAnime() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            List<Anime> jokeList = objectMapper.readValue(new File("anime.json"),
                    typeFactory.constructCollectionType(List.class, Anime.class));
            animeRepository.saveAll(jokeList);
        } catch (Exception e) {
            log.error("Ошибка при загрузке данных");
        }
    }

}
