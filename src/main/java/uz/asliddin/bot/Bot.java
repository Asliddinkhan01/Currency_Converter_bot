package uz.asliddin.bot;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.asliddin.model.Currency;
import uz.asliddin.model.Role;
import uz.asliddin.model.Transactions;
import uz.asliddin.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uz.asliddin.bot.Constants.*;
import static uz.asliddin.dataBase.Db.*;

//Asliddin Kenjaev 12/17/2021 5:38 PM

public class Bot extends TelegramLongPollingBot {
    int randomSmsCode;
    boolean hasPhoto;
    PhotoSize photo;
    boolean hasVideo;
    Video video;
    boolean hasMessage;

    String selectedCurrency;

    boolean toUzb = false;

    @Override
    public String getBotUsername() {
        return Constants.botUserName;
    }

    @Override
    public String getBotToken() {
        return Constants.botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        User currentUser;
        if (update.hasMessage()) {

            Message msgFromUser = update.getMessage();
            Long chatId = msgFromUser.getChatId();
            SendPhoto sendPhoto = new SendPhoto();
            SendVideo sendVideo = new SendVideo();
            GetFile getFile = new GetFile();
            SendDocument sendDocument = new SendDocument();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId.toString());


            currentUser = getCurrentUser(msgFromUser, chatId);
            if (msgFromUser.hasText()) {
                String txtFromUser = msgFromUser.getText();
                if (txtFromUser.equals("/start")) {
                    currentUser.setRound(1.0);
                    userMap.put(chatId, currentUser);
                    sendMessage.setText("Assalomu alaykum " + currentUser.getFullName() + ", welcome to my converter bot \uD83D\uDE0A\uD83D\uDE0A");
                    sendMessage.setReplyMarkup(getReplyKeyboard(currentUser));
                } else if (currentUser.getRound() == 2.0) {
                    if (txtFromUser.equals(String.valueOf(randomSmsCode))) {
                        userMap.get(chatId).setRole(Role.ADMIN);
                        userMap.put(chatId, userMap.get(chatId));
                        currentUser.setRound(4.0);
                    } else {
                        userMap.get(chatId).setRole(Role.USER);
                        userMap.put(chatId, userMap.get(chatId));
                        currentUser.setRound(5.0);
                    }
                    sendMessage.setText("Main menu");
                    sendMessage.setReplyMarkup(getReplyKeyboard(currentUser));
                } else if (currentUser.getRound() == 4.0 && currentUser.getRole() == Role.ADMIN) {
                    switch (txtFromUser) {
                        case "List of users \uD83D\uDC65": {
                            listOfUsers();

                            File file = new File("src/main/resources/users.xlsx");
                            sendDocument.setChatId(String.valueOf(chatId));
                            sendDocument.setDocument(new InputFile(file));
                            try {
                                execute(sendDocument);
                                return;
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case "All info of convertations \uD83D\uDCDA": {
                            infoOfCurrency();
                            File file = new File("src/main/resources/infoCurrency.pdf");
                            sendDocument.setChatId(String.valueOf(chatId));
                            sendDocument.setDocument(new InputFile(file));
                            try {
                                execute(sendDocument);
                                return;
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case "Send advertising to all members \uD83D\uDCE4":
                        case "Send news to all members ✉️":
                            currentUser.setRound(4.1);
                            sendMessage.setText("Choose Type: ");
                            sendMessage.setReplyMarkup(getReplyKeyboard(currentUser));
                            break;
                        case "History of convertations \uD83D\uDCBD": {
                            historyOfConvertations();

                            File file = new File("src/main/resources/history.xlsx");
                            sendDocument.setChatId(String.valueOf(chatId));
                            sendDocument.setDocument(new InputFile(file));
                            try {
                                execute(sendDocument);
                                return;
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                } else if (currentUser.getRound() == 4.5) {
                    String text = msgFromUser.getText();
                    if (hasPhoto) {
                        currentUser.setRound(4.0);
                        sendPhoto.setPhoto(new InputFile(new File(
                                "src/main/resources/pictures/" + photo.getFileUniqueId() + ".png"
                        )));
                        sendPhoto.setCaption(text);
                        hasPhoto = false;
                        try {
                            for (Map.Entry<Long, User> longUserEntry : userMap.entrySet()) {
                                sendPhoto.setChatId(longUserEntry.getKey().toString());
                                execute(sendPhoto);
                            }
                            return;
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else if (hasVideo) {
                        currentUser.setRound(4.0);
                        sendVideo.setVideo(new InputFile(new File(
                                "src/main/resources/videos/" + video.getFileUniqueId() + ".mp4"
                        )));
                        sendVideo.setCaption(text);
                        hasVideo = false;
                        try {
                            for (Map.Entry<Long, User> longUserEntry : userMap.entrySet()) {
                                sendVideo.setChatId(longUserEntry.getKey().toString());
                                execute(sendVideo);
                            }
                            return;
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else if (hasMessage) {
                        currentUser.setRound(4.0);
                        hasMessage = false;
                        try {
                            sendMessage.setText(text);
                            for (Map.Entry<Long, User> longUserEntry : userMap.entrySet()) {
                                sendMessage.setChatId(longUserEntry.getKey().toString());
                                execute(sendMessage);
                            }
                            return;
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (currentUser.getRound() == 5.0 && currentUser.getRole() == Role.USER) {
                    switch (txtFromUser) {
                        case "All info of convertations \uD83D\uDCDA":
                            infoOfCurrency();
                            File file = new File("src/main/resources/infoCurrency.pdf");
                            sendDocument.setChatId(String.valueOf(chatId));
                            sendDocument.setDocument(new InputFile(file));
                            try {
                                execute(sendDocument);
                                return;
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        case "Convert currency \uD83D\uDCB8\uD83D\uDCB8":
                            currentUser.setRound(3.0);
                            sendMessage.setText("Choose type ");
                            sendMessage.setReplyMarkup(getReplyKeyboard(currentUser));
                    }
                } else if (currentUser.getRound() == 3.0 && currentUser.getRole() == Role.USER) {
                    switch (txtFromUser) {
                        case "\uD83C\uDDFA\uD83C\uDDFF UZB -> currency":
                            toUzb = true;
                            currentUser.setRound(5.1);
                            sendMessage.setText("Choose currency: ");
                            sendMessage.setReplyMarkup(getReplyKeyboard(currentUser));
                            break;
                        case "Currency -> UZB \uD83C\uDDFA\uD83C\uDDFF":
                            toUzb = false;
                            currentUser.setRound(5.1);
                            sendMessage.setText("Choose currency: ");
                            sendMessage.setReplyMarkup(getReplyKeyboard(currentUser));
                            break;
                    }
                } else if (currentUser.getRound() == 5.2) {
                    boolean isFound = false;
                    for (Currency currency : currencyList) {
                        if (currency.getCcy().equals(txtFromUser.toUpperCase())) {
                            isFound = true;
                            break;
                        }
                    }
                    if (isFound) {
                        selectedCurrency = txtFromUser;
                        currentUser.setRound(5.3);
                        sendMessage.setText("Enter amount ");
                    } else {
                        sendMessage.setText("Wrong currency. Enter again");
                    }
                } else if (currentUser.getRound() == 5.3) {
                    currentUser.setRound(5.4);
                    sendMessage.setText(converter(currentUser, selectedCurrency.toUpperCase(), txtFromUser));
                    sendMessage.setReplyMarkup(getReplyKeyboard(currentUser));
                } else if (currentUser.getRound() == 5.4) {
                    switch (txtFromUser) {
                        case "Main menu":
                            currentUser.setRound(5.0);
                            sendMessage.setText("Main menu");
                            sendMessage.setReplyMarkup(getReplyKeyboard(currentUser));
                            break;
                        case "Continue to convert":
                            sendMessage.setText("Enter amount");
                            currentUser.setRound(5.3);
                            break;
                        case "Choose another currency":
                            currentUser.setRound(5.1);
                            sendMessage.setText("Choose currency: ");
                            sendMessage.setReplyMarkup(getReplyKeyboard(currentUser));
                            break;
                    }
                }
            } else if (msgFromUser.hasContact()) {
                Contact userContact = msgFromUser.getContact();
                String userNumber = userContact.getPhoneNumber();
                userMap.get(chatId).setPhoneNumber(userNumber);
                userMap.put(chatId, userMap.get(chatId));
                PhoneNumber phoneNumber = new PhoneNumber(userNumber);
                Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
                int sendingRandom = (int) (Math.random() * 9001 + 1000);
                randomSmsCode = sendingRandom;
                System.out.println(randomSmsCode);
                com.twilio.rest.api.v2010.account.Message message = com.twilio.rest.api.v2010.account.Message.creator(
                        phoneNumber, MESSAGE_SID, String.valueOf(sendingRandom)
                ).create();
                System.out.println(message.getSid());
                currentUser.setRound(2.0);
                sendMessage.setText("If you are admin we have sent code to your phone, enter the code. If you are not, press any key ");
            } else if (msgFromUser.hasPhoto()) {
                List<PhotoSize> photos = msgFromUser.getPhoto();
                photo = photos.get(photos.size() - 1);
                getFile.setFileId(photo.getFileId());
                String filePath = null;
                try {
                    filePath = execute(getFile).getFilePath();
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                hasPhoto = true;

                File outputFile = new File("src/main/resources/pictures/" + photo.getFileUniqueId() +
                        ".png");


                try {
                    if (filePath != null) {
                        downloadFile(filePath, outputFile);
                    }
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                currentUser.setRound(4.5);
                sendMessage.setText("Send Text");
            } else if (msgFromUser.hasVideo()) {
                video = msgFromUser.getVideo();
                getFile.setFileId(video.getFileId());
                String filePath = null;
                try {
                    filePath = execute(getFile).getFilePath();
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                hasVideo = true;

                File outputFile = new File("src/main/resources/videos/" + video.getFileUniqueId() +
                        ".mp4");


                try {
                    if (filePath != null) {
                        downloadFile(filePath, outputFile);
                    }
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                currentUser.setRound(4.5);
                sendMessage.setText("Send Text");
            }


            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            Long chatId = callbackQuery.getMessage().getChatId();
            currentUser = getCurrentUser(callbackQuery.getMessage(), chatId);

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId.toString());
            String data = callbackQuery.getData();
            if (currentUser.getRound() == 4.1) {
                switch (data) {
                    case "Message with photo":
                        sendMessage.setText("Sent photo");
                        break;
                    case "Message with video":
                        sendMessage.setText("Sent video");
                        break;
                    case "Message":
                        currentUser.setRound(4.5);
                        hasMessage = true;
                        sendMessage.setText("Sent message");
                        break;
                }
            } else if (currentUser.getRound() == 5.1) {
                if (data.equals("Other")) {
                    sendMessage.setText("Enter Currency: ");
                    currentUser.setRound(5.2);
                } else {
                    selectedCurrency = data;
                    currentUser.setRound(5.3);
                    sendMessage.setText("Enter amount");
                }
            }


            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private User getCurrentUser(Message msgFromUser, Long chatId) {
        User currentUser;
        if (userMap.containsKey(chatId)) {
            currentUser = userMap.get(chatId);
        } else {
            currentUser = new User();
            currentUser.setFullName(msgFromUser.getFrom().getFirstName());
            currentUser.setUserName(msgFromUser.getFrom().getUserName());
            userMap.put(chatId, currentUser);
        }
        return currentUser;
    }

    private ReplyKeyboard getReplyKeyboard(User currentUser) {
        // INLINE
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();

        inlineKeyboardMarkup.setKeyboard(inlineButtons);

        // NOT INLINE
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> rowList = new ArrayList<>();
        keyboardMarkup.setKeyboard(rowList);
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        double currentRound = currentUser.getRound();

        switch (String.valueOf(currentRound)) {
            case "1.0":
                KeyboardButton shareButton = new KeyboardButton();
                shareButton.setText("Share contact \uD83D\uDCDE");
                shareButton.setRequestContact(true);
                row1.add(shareButton);
                rowList.add(row1);
                break;
            case "4.0":
                row1.add("List of users \uD83D\uDC65");
                row1.add("History of convertations \uD83D\uDCBD");
                row2.add("All info of convertations \uD83D\uDCDA");
                row3.add("Send news to all members ✉️");
                row3.add("Send advertising to all members \uD83D\uDCE4");
                rowList.add(row2);
                rowList.add(row3);
                rowList.add(row1);
                break;
            case "4.1":
                List<InlineKeyboardButton> inlineRow1 = new ArrayList<>();
                List<InlineKeyboardButton> inlineRow2 = new ArrayList<>();
                List<InlineKeyboardButton> inlineRow4 = new ArrayList<>();
                InlineKeyboardButton button1 = new InlineKeyboardButton();
                button1.setText("Message with photo");
                button1.setCallbackData("Message with photo");

                InlineKeyboardButton button2 = new InlineKeyboardButton();
                button2.setText("Message with video");
                button2.setCallbackData("Message with video");

                InlineKeyboardButton button4 = new InlineKeyboardButton();
                button4.setText("Message");
                button4.setCallbackData("Message");

                inlineRow1.add(button1);
                inlineRow2.add(button2);
                inlineRow4.add(button4);
                inlineButtons.add(inlineRow1);
                inlineButtons.add(inlineRow2);
                inlineButtons.add(inlineRow4);
                return inlineKeyboardMarkup;
            case "5.0":
                ReplyKeyboardMarkup keyboardMarkup5 = new ReplyKeyboardMarkup();
                keyboardMarkup5.setResizeKeyboard(true);
                List<KeyboardRow> rowList5 = new ArrayList<>();
                keyboardMarkup5.setKeyboard(rowList5);
                KeyboardRow row1_5 = new KeyboardRow();
                KeyboardRow row2_5 = new KeyboardRow();

                row1_5.add("All info of convertations \uD83D\uDCDA");
                row2_5.add("Convert currency \uD83D\uDCB8\uD83D\uDCB8");
                rowList.add(row1_5);
                rowList.add(row2_5);
                break;
            case "3.0":
                ReplyKeyboardMarkup keyboardMarkup3 = new ReplyKeyboardMarkup();
                keyboardMarkup3.setResizeKeyboard(true);
                List<KeyboardRow> rowList3 = new ArrayList<>();
                keyboardMarkup3.setKeyboard(rowList3);
                KeyboardRow row1_3 = new KeyboardRow();
                KeyboardRow row2_3 = new KeyboardRow();

                row1_3.add("\uD83C\uDDFA\uD83C\uDDFF UZB -> currency");
                row2_3.add("Currency -> UZB \uD83C\uDDFA\uD83C\uDDFF");
                rowList.add(row1_3);
                rowList.add(row2_3);
                break;
            case "5.1":
                List<InlineKeyboardButton> inlineRow1_5_1 = new ArrayList<>();
                List<InlineKeyboardButton> inlineRow2_5_1 = new ArrayList<>();
                List<InlineKeyboardButton> inlineRow3_5_1 = new ArrayList<>();
                InlineKeyboardButton button1_5_1 = new InlineKeyboardButton();
                button1_5_1.setText("USD");
                button1_5_1.setCallbackData("USD");
                InlineKeyboardButton button1_5_1_1 = new InlineKeyboardButton();
                button1_5_1_1.setText("RUB");
                button1_5_1_1.setCallbackData("RUB");

                InlineKeyboardButton button2_5_1 = new InlineKeyboardButton();
                button2_5_1.setText("EUR");
                button2_5_1.setCallbackData("EUR");
                InlineKeyboardButton button2_5_1_1 = new InlineKeyboardButton();
                button2_5_1_1.setText("KRW");
                button2_5_1_1.setCallbackData("KRW");

                InlineKeyboardButton button3_5_1 = new InlineKeyboardButton();
                button3_5_1.setText("Other");
                button3_5_1.setCallbackData("Other");

                inlineRow1_5_1.add(button1_5_1);
                inlineRow1_5_1.add(button1_5_1_1);
                inlineRow2_5_1.add(button2_5_1);
                inlineRow2_5_1.add(button2_5_1_1);
                inlineRow3_5_1.add(button3_5_1);

                inlineButtons.add(inlineRow1_5_1);
                inlineButtons.add(inlineRow2_5_1);
                inlineButtons.add(inlineRow3_5_1);
                return inlineKeyboardMarkup;
            case "5.4":
                KeyboardButton mainButton = new KeyboardButton();
                KeyboardButton continueButton = new KeyboardButton();
                KeyboardButton chooseAnother = new KeyboardButton();
                mainButton.setText("Main menu");
                continueButton.setText("Continue to convert");
                chooseAnother.setText("Choose another currency");
                row1.add(continueButton);
                row1.add(chooseAnother);
                row3.add(mainButton);
                rowList.add(row1);
                rowList.add(row3);
                break;

        }


        return keyboardMarkup;
    }

    private void infoOfCurrency() {
        try (PdfWriter pdfWriter = new PdfWriter("src/main/resources/infoCurrency.pdf")) {
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);

            pdfDocument.setDefaultPageSize(PageSize.A4);

            pdfDocument.addNewPage();
            Document document = new Document(pdfDocument);
            Paragraph paragraph = new Paragraph("Currency information");
            paragraph.setTextAlignment(TextAlignment.CENTER);
            document.add(paragraph);

            float[] colWidth = {40F, 150F, 150F};
            Table table = new Table(colWidth);


            table.addCell("T/R");
            table.addCell("Name");
            table.addCell("Rate");

            int count = 1;
            for (Currency currency : currencyList) {
                table.addCell(String.valueOf(count++));
                table.addCell("1 " + currency.getCcy());
                table.addCell(currency.getRate() + " sum");
            }


            table.setHorizontalAlignment(com.itextpdf.layout.property.HorizontalAlignment.CENTER);
            table.setTextAlignment(TextAlignment.CENTER);


            document.add(table);
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void historyOfConvertations() {
        int priceColNum;
        try (FileOutputStream fileOutputStream = new FileOutputStream("src/main/resources/history.xlsx")) {

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();
            sheet.setColumnWidth(0, 20);
            sheet.setDefaultColumnWidth(20);
            sheet.setDefaultRowHeightInPoints(50);

            XSSFRow row = sheet.createRow(0);

            CellStyle cellStyle = row.getSheet().getWorkbook().createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            priceColNum = 6;
            row.createCell(0).setCellValue("T/R");
            row.getCell(0).setCellStyle(cellStyle);
            row.createCell(1).setCellValue("Full name");
            row.getCell(1).setCellStyle(cellStyle);
            row.createCell(2).setCellValue("Telephone number");
            row.getCell(2).setCellStyle(cellStyle);
            row.createCell(3).setCellValue("Currency type");
            row.getCell(3).setCellStyle(cellStyle);
            row.createCell(4).setCellValue("Converted amount");
            row.getCell(4).setCellStyle(cellStyle);
            row.createCell(5).setCellValue("In sum");
            row.getCell(5).setCellStyle(cellStyle);
            row.createCell(6).setCellValue("Date");
            row.getCell(6).setCellStyle(cellStyle);


            for (int j = 0; j < transactionsList.size(); j++) {
                XSSFRow newRow = sheet.createRow(j + 1);
                newRow.createCell(0).setCellValue(j + 1);
                newRow.getCell(0).setCellStyle(cellStyle);
                newRow.createCell(1).setCellValue(transactionsList.get(j).getUser().getFullName());
                newRow.getCell(1).setCellStyle(cellStyle);
                newRow.createCell(2).setCellValue(transactionsList.get(j).getUser().getPhoneNumber());
                newRow.getCell(2).setCellStyle(cellStyle);
                newRow.createCell(3).setCellValue(transactionsList.get(j).getCurrencyType());
                newRow.getCell(3).setCellStyle(cellStyle);
                newRow.createCell(4).setCellValue(transactionsList.get(j).getAmount());
                newRow.getCell(4).setCellStyle(cellStyle);
                newRow.createCell(5).setCellValue(transactionsList.get(j).getInSum());
                newRow.getCell(5).setCellStyle(cellStyle);
                newRow.createCell(priceColNum).setCellValue(transactionsList.get(j).getLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy || hh:mm:ss")));
                newRow.getCell(priceColNum).setCellStyle(cellStyle);

            }

            workbook.write(fileOutputStream);
            System.out.println("Successfully created !!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listOfUsers() {
        int i = 0;
        int priceColNum;
        try (FileOutputStream fileOutputStream1 = new FileOutputStream("src/main/resources/users.xlsx")) {

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();
            sheet.setColumnWidth(0, 20);
            sheet.setDefaultColumnWidth(20);
            sheet.setDefaultRowHeightInPoints(50);

            XSSFRow row = sheet.createRow(0);

            CellStyle cellStyle = row.getSheet().getWorkbook().createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            priceColNum = 4;
            row.createCell(0).setCellValue("T/R");
            row.getCell(0).setCellStyle(cellStyle);
            row.createCell(1).setCellValue("Full name");
            row.getCell(1).setCellStyle(cellStyle);
            row.createCell(2).setCellValue("Username");
            row.getCell(2).setCellStyle(cellStyle);
            row.createCell(3).setCellValue("Telephone number");
            row.getCell(3).setCellStyle(cellStyle);
            row.createCell(4).setCellValue("Role");
            row.getCell(4).setCellStyle(cellStyle);

            for (Map.Entry<Long, User> longUserEntry : userMap.entrySet()) {
                XSSFRow newRow = sheet.createRow(i + 1);
                newRow.createCell(0).setCellValue(i + 1);
                newRow.getCell(0).setCellStyle(cellStyle);
                newRow.createCell(1).setCellValue(longUserEntry.getValue().getFullName());
                newRow.getCell(1).setCellStyle(cellStyle);
                newRow.createCell(2).setCellValue(longUserEntry.getValue().getUserName());
                newRow.getCell(2).setCellStyle(cellStyle);
                newRow.createCell(3).setCellValue(longUserEntry.getValue().getPhoneNumber());
                newRow.getCell(3).setCellStyle(cellStyle);
                newRow.createCell(priceColNum).setCellValue(longUserEntry.getValue().getRole().toString());
                newRow.getCell(priceColNum).setCellStyle(cellStyle);
                i++;
            }

            workbook.write(fileOutputStream1);
            System.out.println("Successfully created !!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String converter(User currentUser, String str, String amount) {
        Currency currency = new Currency();
        for (Currency currency1 : currencyList) {
            if (currency1.getCcy().equals(str)) {
                currency = currency1;
                break;
            }
        }
        BigDecimal bigDecimal1 = new BigDecimal(amount);
        BigDecimal bigDecimal2 = new BigDecimal(currency.getRate());
        String result;

        if (toUzb) {
            double v = Double.parseDouble(amount) / Double.parseDouble(currency.getRate());
            result = String.valueOf(v);
        } else {
            BigDecimal multiply = bigDecimal1.multiply(bigDecimal2);
            result = multiply.toString();
        }
        for (int i = 1; i < result.length(); i++) {
            if (result.charAt(i) == '.') {
                result = result.substring(0, i + 3);
                break;
            }
        }
        result = result + " " + (toUzb ? currency.getCcy() : "sum");

        LocalDateTime now = LocalDateTime.now();
        Transactions transactions = new Transactions(currentUser, currency.getCcy(), amount, result, now);

        transactionsList.add(transactions);

        return result;
    }
}