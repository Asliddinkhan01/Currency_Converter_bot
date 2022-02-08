package uz.asliddin;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.asliddin.bot.Bot;
import uz.asliddin.dataBase.Db;

import static uz.asliddin.util.MyUtil.GREEN_BOLD;
import static uz.asliddin.util.MyUtil.print;

//Asliddin Kenjaev 12/16/2021 1:03 PM

public class Main {
    public static void main(String[] args) {
        Db.getCurrencyFromBank();
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);

            api.registerBot(new Bot());

            print(GREEN_BOLD, "Bot started...");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
