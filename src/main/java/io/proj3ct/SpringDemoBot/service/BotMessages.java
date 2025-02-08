package io.proj3ct.SpringDemoBot.service;

import io.proj3ct.SpringDemoBot.entity.Anime;
import io.proj3ct.SpringDemoBot.entity.BotUser;
import org.springframework.stereotype.Component;

@Component
public class BotMessages {
    public static final String START_MESSAGE = """
            🎉 Добро пожаловать в Аниме-Бота! 🤖🍿
            
            Этот бот поможет вам найти аниме по вашим предпочтениям. 🎬📺  
            
            🔹 Как это работает?  
            1️⃣ Выберите тип аниме с помощью команды /kind (фильм, сериал или любой).  
            2️⃣ Укажите статус выхода с командой /status (онгоинг, завершённое или анонс).  
            3️⃣ Используйте команду /anime, и бот порекомендует случайное аниме по вашим настройкам.  
            
            📌 Ваши настройки можно посмотреть с помощью /mypreferences.  
            ℹ Если забудете команды – просто используйте /help.  
            
            Приятного просмотра! 🍿✨  
            """;

    public static final String HELP_MESSAGE = """
            🤖 Список доступных команд:
            
            🔹 /start – зарегистрироваться в боте. Бот сохранит ваши предпочтения и поможет подбирать аниме.
            
            🔹 /help – показать это сообщение с описанием всех команд.
            
            🔹 /mypreferences – показать ваши текущие предпочтения. Бот сообщит, какие параметры сейчас выбраны для подбора аниме.
            
            🔹 /anime – бот случайным образом порекомендует вам аниме, соответствующее вашим предпочтениям (тип и статус выхода).
            
            🔹 /kind – выбрать тип аниме, который вас интересует (сериал или фильм).
            
            🔹 /status – выбрать статус выхода аниме (онгоинг, завершённое или анонсированное).
            
            🔹 Как это работает?
            1️⃣ Сначала настройте свои предпочтения с помощью /kind и /status.
            2️⃣ Затем используйте команду /anime, и бот подберёт вам случайное аниме по этим параметрам.
            
            Приятного просмотра! 🍿🎥
            """;

    public static final String CHOOSE_ANIME_KIND = """
            📝 Выберите тип аниме, который вам нравится.
            После этого я буду предлагать случайные аниме только этого типа.
            
            🎬 Movie – полнометражные аниме.
            📺 TV – многосерийные аниме.
            """;

    public static final String CHOOSE_ANIME_STATUS = """
            📝 Выберите, какие аниме вас интересуют по статусу выхода.
            После этого я буду рекомендовать только такие аниме.
            
            🔴 Ongoing – аниме, которые выходят прямо сейчас.
            ✅ Released – полностью вышедшие аниме.
            ⏳ Anons – те, что только планируются к выпуску.
            """;

    public static String getAnimeInformationText(Anime anime) {
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

    public static String getUserPreferencesText(BotUser user) {
        return String.format("""
                🔹 Ваши текущие настройки:
                📺 Тип: %s
                📌 Статус: %s
                
                Вы можете изменить их с помощью команд /kind и /status.
                """, user.getKind(), user.getStatus());
    }

}
