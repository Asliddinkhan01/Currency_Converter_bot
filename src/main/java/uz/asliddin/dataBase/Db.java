package uz.asliddin.dataBase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import uz.asliddin.model.Currency;
import uz.asliddin.model.Transactions;
import uz.asliddin.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



//Asliddin Kenjaev 12/18/2021 4:49 PM

public class Db {
    public static Map<Long, User> userMap = new HashMap<>();

    public static List<Currency> currencyList;

    public static List<Transactions> transactionsList = new ArrayList<>();

    public static void getCurrencyFromBank() {

        Gson gson = new Gson();

        try {
            URL url = new URL("https://cbu.uz/uz/arkhiv-kursov-valyut/json/");
            URLConnection connection = url.openConnection();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            currencyList = gson.fromJson(bufferedReader, new TypeToken<List<Currency>>() {
            }.getType());

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Db is ready !!! ");
    }
}
