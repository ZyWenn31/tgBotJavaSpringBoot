package com.enduro.test.enduroTest.service;

import com.enduro.test.enduroTest.config.BotConfig;
import com.enduro.test.enduroTest.model.EnduroEntity;
import com.enduro.test.enduroTest.model.Request;
import com.enduro.test.enduroTest.model.User;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    Map<Long, UserStatus> userStatusMap = new HashMap<>();
    private EnduroEntity enduroEntity;

    private static final String SUPPORT_TEXT = EmojiParser.parseToUnicode("Мы будем рады любой обратной связи, с нами можно связаться:\n\n" +
            "\uD83D\uDD35 Телеграмм: @tg_loha323\n\n" +
            "\uD83E\uDEE7 Группа Вконтакте: https://vk.com/endurodrive_spb\n\n" +
            "\uD83C\uDF10 Наш сайт: https://t.me/EnduroDrives\n\n");

    private final EnduroEntityService enduroEntityService;
    private final FeedbackService feedbackService;
    private final UserService userService;
    private final RequestService requestService;

    public TelegramBot(BotConfig botConfig, EnduroEntityService enduroEntityService, FeedbackService feedbackService, UserService userService, RequestService requestService) {
        this.botConfig = botConfig;
        this.enduroEntityService = enduroEntityService;
        this.feedbackService = feedbackService;
        this.userService = userService;
        this.requestService = requestService;


        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/request", "Получить запросы на покупку"));
        botCommands.add(new BotCommand("/newenduro", "Добавить эндуро в каталог"));
        botCommands.add(new BotCommand("/deleteenduro", "Удалить эндуро из каталога по названию"));
        botCommands.add(new BotCommand("/completerequest", "Отметить заявку как выполненую"));

        try {
            execute(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: {}", e.getMessage());
        }

    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && (update.getMessage().hasText() || update.getMessage().hasPhoto())){
            long chatId = update.getMessage().getChatId();
            String text;
            if (userStatusMap.get(chatId) == UserStatus.ENTER_ENDURO_DESCRIPTION){
                text = EmojiParser.parseToUnicode(update.getMessage().getText());
            } else{
                text = update.getMessage().getText();
            }
            if (text == null){
                text = "text_is_nul_but_it_is_save_he";
            }


            if (update.getMessage().hasText()){
                if (text.contains("/request") && (userService.findById(chatId).isAdministrator() || userService.findById(chatId).getRole().equals("MANAGER"))){
                    sendMessageToChat(chatId, getAllRequestAsString(), getBackToMainKeyboardMarkup());
                    userStatusMap.put(chatId, UserStatus.DEFAULT);
                    return;
                }

                if (text.contains("/newenduro") && (userService.findById(chatId).isAdministrator() || userService.findById(chatId).getRole().equals("MANAGER"))){
                    sendMessageToChat(chatId, "Пришлите название модели", getBackToMainKeyboardMarkup());
                    userStatusMap.put(chatId, UserStatus.ENTER_ENDURO_NAME);
                    return;
                }

                if (text.contains("/deleteenduro") && (userService.findById(chatId).isAdministrator() || userService.findById(chatId).getRole().equals("MANAGER"))){
                    String enduroNameToDelete = text.substring(text.indexOf(" ") + 1);
                    EnduroEntity enduroToDelete = enduroEntityService.findByName(enduroNameToDelete);

                    if (enduroToDelete != null){
                        sendMessageToChat(chatId, "Эндуро " +enduroToDelete.getName()+" успешно удален из каталога", getBackToMainKeyboardMarkup());
                        enduroEntityService.delete(enduroToDelete.getId());
                        return;
                    }

                    sendMessageToChat(chatId, "Эндуро с таким именем не найден, повторите ввод команды", getBackToMainKeyboardMarkup());
                    return;
                }

                if (text.contains("/completerequest") && (userService.findById(chatId).isAdministrator() || userService.findById(chatId).getRole().equals("MANAGER"))){
                    String requestNumberToDelete = text.substring(text.indexOf(" ") + 1);

                    Long reqNum;

                    try {
                        reqNum = Long.parseLong(requestNumberToDelete);
                    } catch (NumberFormatException e) {
                        sendMessageToChat(chatId, "Это должно быть целое число, повторите ввод", getBackToMainKeyboardMarkup());
                        return;
                    }

                    Request requestToDelete = requestService.findById(reqNum);
                    if (requestToDelete != null && !requestToDelete.isCompleted()){
                        sendMessageToChat(chatId, "Запрос на покупку с номером " + requestToDelete.getId() +",  от пользователя @" +requestToDelete.getUserName()+" успешно переведен в статус завершенных", getBackToMainKeyboardMarkup());
                        requestToDelete.setCompleted(true);
                        requestToDelete.setUserNameManager(userService.findById(chatId).getUserName());
                        requestService.save(requestToDelete);
                        return;
                    }

                    sendMessageToChat(chatId, "Запрос с таким номером не найден, повторите ввод, повторите ввод команды", getBackToMainKeyboardMarkup());
                    return;
                }
            }


            switch (text){
                case "/start":{
                    registerUser(chatId, update.getMessage().getChat());
                    sendMessageToChat(chatId, EmojiParser.parseToUnicode("Добро пожаловать в мазагин ENDURO, рады вас привествовать " + "\uD83D\uDE0A \n\n" +
                            "Для просмотра каталога доступных эндуро нажмите 'Каталог';\n\n" +
                            "Для получения полной информации про эндуро скопирйте его название и отправте его боту;\n\n" +
                            "Для оформления заказа нажмите 'Сделать заказ'."), getKeyboardMarkup());
                    break;
                }
                case "На главную":{
                    sendMessageToChat(chatId, EmojiParser.parseToUnicode("Самое время оформить заказ - выберите модель и оставте заявку  \uD83D\uDEF8"), getKeyboardMarkup());
                    userStatusMap.put(chatId, UserStatus.DEFAULT);
                    enduroEntity = new EnduroEntity();
                    break;
                }
                case "Связь с нами":{
                    sendMessageToChat(chatId, EmojiParser.parseToUnicode(SUPPORT_TEXT), getBackToMainKeyboardMarkup());
                    break;
                }
                case "Каталог":{
                    sendMessageToChat(chatId, EmojiParser.parseToUnicode("Ниже представлен каталог доступных для заказа эндуро \uD83D\uDE0A"), getBackToMainKeyboardMarkup());
                    sendMessageToChatWithCopyCodes(chatId, getAllEnduroCatalogByString(), null);
                    break;
                }
                case "Сделать заказ":{
                    userStatusMap.put(chatId, UserStatus.WAITING_ENDURO_NAME);
                    sendMessageToChat(chatId, EmojiParser.parseToUnicode("Отправьте название эндуро, на который хотите сделать заказ, будьте внимательны при написании, советуем скопировать его из карточки товара!!!!"), getBackToMainKeyboardMarkup());
                    break;
                }
                default:
                    if (userStatusMap.get(chatId) == UserStatus.WAITING_ENDURO_NAME){
                        EnduroEntity enduro = enduroEntityService.findByName(text);
                        if (enduro != null){

                            userStatusMap.put(chatId, UserStatus.DEFAULT);

                            sendMessageToChat(chatId, "Заказ успешно оформлен, можно выйти в главное меню, мы уже занимаемся вашей заявкой, ждите пока вам напишет менеджер", getBackToMainKeyboardMarkup());
                            NotifyAllManagers(requestService.save(new Request(update.getMessage().getChat().getUserName(), text, LocalDateTime.now(), null, false)));
                            return;
                        }

                        sendMessageToChat(chatId, "Такого эндуро не найдено, проверьте правильность написания названия и отправте его снова, рекомендуем скопировать его, что бы избежать ошибок", getBackToMainKeyboardMarkup());
                        return;
                    }

                    if (userStatusMap.get(chatId) == UserStatus.ENTER_ENDURO_NAME){
                        EnduroEntity enduro = enduroEntityService.findByName(text);
                        if(enduro == null){
                            sendMessageToChat(chatId, "Отлично, теперь пришлите описание для эндуро", getBackToMainKeyboardMarkup());
                            userStatusMap.put(chatId, UserStatus.ENTER_ENDURO_DESCRIPTION);

                            this.enduroEntity = new EnduroEntity();
                            enduroEntity.setName(text);
                            return;
                        }

                        sendMessageToChat(chatId, "Эндуро с таким именем уже существует, введите другое", getBackToMainKeyboardMarkup());
                        return;
                    }

                    if (userStatusMap.get(chatId) == UserStatus.ENTER_ENDURO_DESCRIPTION){
                        enduroEntity.setDescription(text);
                        userStatusMap.put(chatId, UserStatus.ENTER_ENDURO_SPECS);

                        sendMessageToChat(chatId, "Теперь необходимо ввести характеристики в отдельном сообщении, строго в виде:\nкубы,такты,лошадСилы,вес", getBackToMainKeyboardMarkup());
                        return;
                    }

                    if (userStatusMap.get(chatId) == UserStatus.ENTER_ENDURO_SPECS){
                        if (enduroSpecsValid(text)){
                            userStatusMap.put(chatId, UserStatus.ENTER_ENDURO_PRICE);
                            sendMessageToChat(chatId, "Характеристики успешно добавленны, введите стоимость", getBackToMainKeyboardMarkup());
                            return;
                        }


                        sendMessageToChat(chatId, "Характеристике введены неверно, повторите ввод, внимательно следите за форматом данных", getBackToMainKeyboardMarkup());
                        return;
                    }

                    if (userStatusMap.get(chatId) == UserStatus.ENTER_ENDURO_PRICE){
                        try{
                            enduroEntity.setPrice(Integer.parseInt(text));
                        } catch (NumberFormatException e) {
                            sendMessageToChat(chatId, "Цена должна быть целым числом", getBackToMainKeyboardMarkup());
                            return;
                        }

                        sendMessageToChat(chatId, "Данные записаны, отправте фото для эндуро", getBackToMainKeyboardMarkup());
                        userStatusMap.put(chatId, UserStatus.ENTER_ENDURO_PHOTO);
                        return;
                    }

                    if (userStatusMap.get(chatId) == UserStatus.ENTER_ENDURO_PHOTO){

                        if (getAndSavePhoto(update.getMessage().getPhoto())){
                            EnduroEntity savedEnduro = enduroEntityService.save(enduroEntity);
                            sendMessageToChat(chatId, "Эндуро добавлен в каталог:", getBackToMainKeyboardMarkup());
                            sendMessageWithImage(chatId, getInfoByEnduro(savedEnduro), savedEnduro.getName(), getBackToMainKeyboardMarkup());

                            userStatusMap.put(chatId, UserStatus.DEFAULT);
                            return;
                        }

                        sendMessageToChat(chatId, "Что то пошло не так при загрузке фото", getBackToMainKeyboardMarkup());
                        return;
                    }


                    EnduroEntity enduro = enduroEntityService.findByName(text);
                    if (enduro != null){
                        sendMessageWithImage(chatId, getInfoByEnduro(enduro), text, getBackToMainKeyboardMarkup());
                        return;
                    }
                    sendMessageToChat(chatId, EmojiParser.parseToUnicode("Я вас не понял, введите другую команду \uD83D\uDE0A"), null);
                    break;
            }
        }



    }

    private void NotifyAllManagers(Request request) {
        List<User> managers = userService.findAllByRole("MANAGER");

        for (User manager : managers){
            sendMessageToChat(manager.getChatId(), "Добавлен новый запрос на покупку с номером: " + request.getId() +" от пользователя @" + request.getUserName(), getBackToMainKeyboardMarkup());
        }
    }

    private boolean getAndSavePhoto(List<PhotoSize> photoSizes) {
        PhotoSize photoSize = photoSizes.stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElse(null);

        String fileId = photoSize.getFileId();
        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);
        File file = new File();
        try {
            file = execute(getFile);
        } catch (TelegramApiException e) {
            log.error("Error get file:{}", e.getMessage());
            return false;
        }
        String filePath = file.getFilePath();

        String fileUrl = "https://api.telegram.org/file/bot" + botConfig.getBotToken() + "/" + filePath;
        String savePath = "upload/" + enduroEntity.getName() + ".jpg";

        try (InputStream in = new URL(fileUrl).openStream()) {
            Files.copy(in, Paths.get(savePath), StandardCopyOption.REPLACE_EXISTING);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;

    }

    private boolean enduroSpecsValid(String text) {

        int cubes, tact, horsePower, weight;
        String[] specs = Arrays.stream(text.split(","))
                .map(String::trim)
                .toArray(String[]::new);

        try {
            cubes = Integer.parseInt(specs[0]);
            tact = Integer.parseInt(specs[1]);
            horsePower = Integer.parseInt(specs[2]);
            weight = Integer.parseInt(specs[3]);
        } catch (NumberFormatException e){
            return false;
        }

        enduroEntity.setCubes(cubes);
        enduroEntity.setTact(tact);
        enduroEntity.setHorsepower(horsePower);
        enduroEntity.setWeight(weight);

        return true;
    }

    private String getInfoByEnduro(EnduroEntity enduro) {

        String enduroName= """
                    <code>%s</code>""".formatted(enduro.getName());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(enduroName+"\n");
        stringBuilder.append("\n" + enduro.getDescription()+".");
        stringBuilder.append(EmojiParser.parseToUnicode("\n\nХарактеристики:\n⚙\uFE0F "+enduro.getCubes() + " кубов;"));
        stringBuilder.append(EmojiParser.parseToUnicode("\n⚙\uFE0F "+enduro.getTact() + " тактный;"));
        stringBuilder.append(EmojiParser.parseToUnicode("\n⚙\uFE0F "+enduro.getHorsepower() + " лошадиных сил;"));
        stringBuilder.append(EmojiParser.parseToUnicode("\n⚙\uFE0F "+enduro.getWeight() + " кг;"));
        stringBuilder.append("\n\n\n");

        stringBuilder.append("\uD83D\uDD11  "+enduro.getPrice() + "₽");


        return stringBuilder.toString();
    }


    private void registerUser(Long chatId, Chat chat){
            User user = new User(chatId, chat.getFirstName(), chat.getLastName(), chat.getUserName(), LocalDate.now(), false, "MEMBER");

            userService.save(user);
            log.info("user saved: {}", user);
    }


    private void sendMessageToChat(long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);


        if (replyKeyboardMarkup != null){
            message.setReplyMarkup(replyKeyboardMarkup);
        }


        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message: {}", e.getMessage());
        }
    }

    private void sendMessageWithImage(long chatId, String text, String imageName, ReplyKeyboardMarkup replyKeyboardMarkup){
        String uploadDir = "upload"; // папка на сервере
        java.io.File file = new java.io.File(uploadDir, imageName + ".jpg");

        SendPhoto message = new SendPhoto();
        message.setChatId(chatId);
        message.setCaption(text);

        if (!file.exists()) {
            java.io.File notFound = new java.io.File(uploadDir, "not_found.jpg");
            message.setPhoto(new InputFile(notFound));
        } else {
            message.setPhoto(new InputFile(file));
        }

        message.setParseMode("HTML");

        if (replyKeyboardMarkup != null){
            message.setReplyMarkup(replyKeyboardMarkup);
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message: {}", e.getMessage());
        }
    }


    private void sendMessageToChatWithCopyCodes(long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode("HTML");


        if (replyKeyboardMarkup != null){
            message.setReplyMarkup(replyKeyboardMarkup);
        }


        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message: {}", e.getMessage());
        }
    }

    private ReplyKeyboardMarkup getKeyboardMarkup(){
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();


        keyboardRow.add("Каталог");
        keyboardRow.add("Отзывы");
        keyboardRow.add("Связь с нами");
        keyboardRows.add(keyboardRow);

        keyboardRow = new KeyboardRow();
        keyboardRow.add("Сделать заказ");
        keyboardRows.add(keyboardRow);

        keyboardRow = new KeyboardRow();
        keyboardRow.add("Оставить отзыв");
        keyboardRows.add(keyboardRow);

        keyboardMarkup.setKeyboard(keyboardRows);
        keyboardMarkup.setResizeKeyboard(true);

        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup getBackToMainKeyboardMarkup(){
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();


        keyboardRow.add("На главную");

        keyboardRows.add(keyboardRow);

        keyboardMarkup.setKeyboard(keyboardRows);
        keyboardMarkup.setResizeKeyboard(true);

        return keyboardMarkup;
    }

    private String getAllEnduroCatalogByString(){
        StringBuilder stringBuilder = new StringBuilder();
        List<EnduroEntity> enduroEntities= enduroEntityService.findAll();

        for(EnduroEntity enduroEntity : enduroEntities){
            String enduroNameLink= """
                    <code>%s</code>""".formatted(enduroEntity.getName());
            stringBuilder.append(EmojiParser.parseToUnicode(enduroNameLink + " - " + enduroEntity.getPrice() + " \uD83D\uDD25" + "\n\n"));
        }


        stringBuilder.append(EmojiParser.parseToUnicode("\n"+ "Для получения деатльной информации скопируйте название эндура и отправьте его боту.\n"));
        stringBuilder.append(EmojiParser.parseToUnicode("\n"+ "Они ждут своих новых владельцев \uD83E\uDD79"));

        return stringBuilder.toString();
    }


    public String getAllRequestAsString(){
        StringBuilder stringBuilder = new StringBuilder();
        List<Request> requests = requestService.findByCompleted(false);

        if (requests.isEmpty()){
            return "Нет активных заявок";
        }

        stringBuilder.append("\n\n");
        for (Request request : requests){

            stringBuilder.append(request.getId() + ".  @" + request.getUserName() + "\nЗаказ на: " + request.getEnduroName() + "\nБыл создан: " + request.getCreatedAt().toString());

            stringBuilder.append("\n\n");
        }

        return stringBuilder.toString();
    }
}