package ru.komiss77.modules.translate;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import com.destroystokyo.paper.ClientOption;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.Timer;
import ru.komiss77.events.ChatPrepareEvent;
import ru.komiss77.listener.ChatLst;
import ru.komiss77.modules.games.GM;

//https://github.com/DeepLcom/deepl-java?tab=readme-ov-file
//https://github.com/AsyncHttpClient/async-http-client

//добавить локальный буфер в файлике
//https://docs.papermc.io/paper/dev/component-api/i18n

//"+Lang.t(p, "           (bp.e ? "" :
//final boolean eng = !p.getClientOption(ClientOption.LOCALE).equals("ru_ru");

public class Lang {

  private static final Map<String, String> ruToEng;//Map<Integer, HashMap<String, String>> ruToEng;-возможно потом добавить сортировку по длинне
  //public static int updateStamp;
  private static final HttpClient HTTP;
  private static HttpRequest.Builder rb;
  public static final Locale RU, EN;
  private static final TextComponent err;
  private static String apiKey = "", folderId = "";


  static {
    ruToEng = new ConcurrentHashMap<>();
    HTTP = HttpClient.newHttpClient();
    RU = Locale.forLanguageTag("ru_ru");
    EN = Locale.forLanguageTag("en_us");
    err = Component.text("{}");
  }


  //при старте вычитает все записи, свежее updateStamp
  //затем будет подкидывать обновы вместе с GM.loadArenaInfo
  public static void updateBase(final ResultSet rs) {
    try {
      int add = 0;
      while (rs.next()) {
        ruToEng.put(rs.getString("rus"), rs.getString("eng"));
        add++;
        if (rs.getLong("ts") > GM.tsLang) {// (GM.tsLang.getTime() < rs.getTimestamp("ts").getTime()) {//запоминаем наибольший последний апдейт - в след.раз прогрузим с него
          GM.tsLang = rs.getLong("ts");
        }
      }

      //updateStamp = Timer.getTime();
      if (add>0) {
        Ostrov.log_ok("Lang loadBase добавлено записей : §b"+add+" (всего:"+ruToEng.size()+")");
      }
      apiKey = ruToEng.getOrDefault("apiKey", "");
      folderId = ruToEng.getOrDefault("folderId", "");
      if (rb==null && !apiKey.isEmpty() && !folderId.isEmpty()) { //не надо тут оптимизаций
        // Lang translateChat error : responce=Request Header Fields Too Large
        rb = HttpRequest.newBuilder()
          .uri(URI.create("https://translate.api.cloud.yandex.net/translate/v2/translate"))
          .headers("Content-Type", "application/json", "Authorization", "Api-Key "+apiKey) //Yandex Cloud	 Утечка конфиденциальных данных вашего аккаунта
          .timeout(Duration.of(5, ChronoUnit.SECONDS))
          .version(java.net.http.HttpClient.Version.HTTP_1_1); //это звиздец, эта строчка стоила дня моей жизни
      }
    } catch (SQLException ex) {
      Ostrov.log_err("Lang loadBase error : "+ex.getMessage());
    }
  }




  public static String t (final Player p, final String ruMsg) {
    final boolean ru = p==null || p.getClientOption(ClientOption.LOCALE).equals("ru_ru");
    if (ru) {
      return ruMsg;
    } else {
      return translate(ruMsg, EN);
    }
  }


  public static String t (final String ruMsg, final Locale locale) {
    if (locale == RU) {
      return ruMsg;
    } else {
      return translate(ruMsg, locale);
    }
  }

  //перевод названий предметов,чар,биомов и всего что имеет перевод mojang
  public static Component t (final Translatable o, final Player p) {
    final Locale locale = p==null ? RU : p.locale(); //не убирать! расчитано, что иногда приходит с null, так надо!
    return o == null ? err : t(o, locale);
  }

  public static Component t (final Translatable o, final Locale locale) {
    return o == null ? err : GlobalTranslator.render(Component.translatable(o), locale);
  }



  //подменять >p.sendMessage(< на >Lang.sendMessage(p, <
  public static void sendMessage (final Player p, final String ruMsg) {
    final String locale = p.getClientOption(ClientOption.LOCALE);
    if (locale.equals("ru_ru")) {
      p.sendMessage(ruMsg);
    } else {
      p.sendMessage(translate(ruMsg, EN));
    }
  }

  public static String translate (final String ruMsg, final Locale locale) {
    String trans = ruToEng.get(ruMsg);  //при написании \ .\ или ..\ Lang t error : Unexpected character (C) at position 0.

    if (trans == null) { //перевода нема
      if (rb==null) return ruMsg;// ключи еще не подгрузились
      ruToEng.put(ruMsg, ruMsg); //вставить заглушку, чтобы не дублировало запросы на переводы

      final HttpRequest request = rb.POST(HttpRequest.BodyPublishers.ofString("{\"targetLanguageCode\":\""+(locale==RU?"ru":"en")
          +"\",\"folderId\":\""+folderId+"\",\"texts\":\""+ruMsg.replace('\\', ' ')+"\"}"))
        .build();

      final CompletableFuture<Void> cf = HTTP.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofByteArray())
        .thenApply(java.net.http.HttpResponse::body)
        //.thenAccept(System.out::println);
        .thenAccept( array -> {
          String responce = new String(array);
          int idx = responce.indexOf("text");
          if (idx>0) {
            responce = responce.substring(idx+8);
            idx = responce.indexOf("\"");
            if (idx>0) {
              responce = responce.substring(0,idx);
              responce = responce.replace('\'', ' '); // ' багает мускул
              upd(ruMsg, responce);
            }
          }
        })
        .exceptionally( ex -> {
          Ostrov.log_err("Lang t error : "+ex.getMessage());
          return null;
        });
      cf.join();


    }
    return ruMsg;
  }

  public static void upd(final String ruMsg, final String translateResult) {
    ruToEng.put(ruMsg, translateResult);
    OstrovDB.executePstAsync(Bukkit.getConsoleSender(),
      "INSERT INTO `lang` (`lenght`, `rus`, `eng`, `ts`) VALUES ('"+ruMsg.length()+"', '"+ruMsg+"', '"+translateResult
        +"', NOW()+0)  ON DUPLICATE KEY UPDATE eng=VALUES(eng), ts=NOW()+0;");
  }



  //в эвенте переводим недостающий язык
  public static void translateChat(final ChatPrepareEvent ce) {
//Ostrov.log("translateChat apiKey="+apiKey+" folderId="+folderId);
    if (rb == null) {// ключи еще не подгрузились
      abort(ce);
      return;
    }
    //final Request request = rb.setBody("{\"targetLanguageCode\":\"ru\",\"folderId\":\"\",\"texts\":\""+ce.oriStripMsg+"\"}").build();
    final HttpRequest request;
    if (ce.stripMsgRu!=null) {
      request = rb.POST(HttpRequest.BodyPublishers.ofString("{\"targetLanguageCode\":\"en\",\"folderId\":\""+folderId+"\",\"texts\":\""
        +ce.stripMsgRu.replace('\\', ' ')+"\"}")).build();
    } else {
      request = rb.POST(HttpRequest.BodyPublishers.ofString("{\"targetLanguageCode\":\"ru\",\"folderId\":\""+folderId+"\",\"texts\":\""
        +ce.stripMsgEn.replace('\\', ' ')+"\"}")).build();
    }
//Ostrov.log("request ="+request);
    final CompletableFuture<Void> cf = HTTP.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofByteArray())
      .thenApply(java.net.http.HttpResponse::body)
      //.thenAccept(System.out::println);
      .thenAccept( array -> {
        String responce = new String(array);
        int idx = responce.indexOf("text");
        if (idx>0) {
          responce = responce.substring(idx+8);
          idx = responce.indexOf("\"");
          if (idx>0) {
            responce = responce.substring(0,idx);
            if (ce.stripMsgRu==null) {
              ce.stripMsgRu = responce;
            } else {
              ce.stripMsgEn = responce;
            }
            ChatLst.process(ce);
            return;
          }
        }
        Ostrov.log_err("Lang translateChat error : responce="+responce);
        abort(ce);
      })
      .exceptionally( ex -> {
        abort(ce);
        Ostrov.log_err("Lang translateChat error : "+ex.getMessage());
        return null;
      });
    cf.join();
  }

  private static void abort(final ChatPrepareEvent ce) {
    if (ce.stripMsgEn==null) {
      ce.stripMsgEn = ce.stripMsgRu;
    } else {
      ce.stripMsgRu = ce.stripMsgEn;
    }
    ChatLst.process(ce);
  }

  public static String getTranslate(final String rus) {
    return ruToEng.getOrDefault(rus, "");
  }




}



//НЕ УБИРАТЬ, МОЖЕТ ЕЩЁ ПРИГОДИТЬСЯ!!
//rb = new RequestBuilder(HttpConstants.Methods.POST)
//        .setUrl("https://translate.api.cloud.yandex.net/translate/v2/translate")
//        .setHeader("Content-Type", "application/json")
//        .addHeader("Authorization", "Api-Key ")
//.setBody("{\"targetLanguageCode\":\"en\",\"folderId\":\"\",\"texts\":\""+ruMsg+"\"}")
//        .setCharset(StandardCharsets.UTF_8);


        /* final Request request = rb.setBody("{\"targetLanguageCode\":\""+(locale==RU?"ru":"en")+"\",\"folderId\":\"\",\"texts\":\""+ruMsg.replace('\\', ' ')+"\"}").build();
            final AsyncCompletionHandler<Response> ah = new AsyncCompletionHandler<>() {
                @Override
                public @Nullable Response onCompleted(final @Nullable Response response) {
                    return response;
                }
                @Override
                public AsyncHandler.State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                    String retSrc = new String(bodyPart.getBodyPartBytes());//EntityUtils.toString(entity);
                    Object jsob_obj = new JSONParser().parse(retSrc);
                    JSONObject json_res = (JSONObject) jsob_obj;
                    JSONArray res_translate = (JSONArray) json_res.get("translations");
                    JSONObject res_json_obj = (JSONObject) res_translate.get(0);
                    String translateResult = (String) res_json_obj.get("text");
                    translateResult = translateResult.replace('\'', ' '); // ' багает мускул
                    upd(ruMsg, translateResult);
                    return AsyncHandler.State.ABORT;
                }
            };

            try {
                Ostrov.HTTP.executeRequest(request, ah).get();
            } catch (InterruptedException | ExecutionException | NullPointerException ex) {
                Ostrov.log_err("Lang t error : "+ex.getMessage());
            }


        final Request request;
        if (ce.stripMsgRu!=null) {
            request = rb.setBody("{\"targetLanguageCode\":\"en\",\"folderId\":\"\",\"texts\":\""+ce.stripMsgRu+"\"}").build();
        } else {
            request = rb.setBody("{\"targetLanguageCode\":\"ru\",\"folderId\":\"\",\"texts\":\""+ce.stripMsgEn+"\"}").build();
        }
        
        final AsyncCompletionHandler<Response> ah = new AsyncCompletionHandler<>() {
            @Override
            public @Nullable Response onCompleted(final @Nullable Response response) {
                return response;
            }
            @Override
            public AsyncHandler.State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                String retSrc = new String(bodyPart.getBodyPartBytes());//EntityUtils.toString(entity);
                Object jsob_obj = new JSONParser().parse(retSrc);
                JSONObject json_res = (JSONObject) jsob_obj;
                JSONArray res_translate = (JSONArray) json_res.get("translations");
                JSONObject res_json_obj = (JSONObject) res_translate.get(0);
                if (ce.stripMsgRu==null) {
                    ce.stripMsgRu = (String) res_json_obj.get("text");
                } else {
                    ce.stripMsgEn = (String) res_json_obj.get("text");
                }
//Ostrov.log_ok("t:"+ruMsg+"->"+translateResult);
                ChatLst.process(ce);
                return AsyncHandler.State.ABORT;
            }
        };

        try {
            Ostrov.HTTP.executeRequest(request, ah).get();
        } catch (InterruptedException | ExecutionException | NullPointerException ex) {
            if (ce.stripMsgEn==null) {
                ce.stripMsgEn = ce.stripMsgRu;
            } else {
                ce.stripMsgRu = ce.stripMsgEn;
            }
            Ostrov.log_err("Lang translateChat error : "+ex.getMessage());
        }
        
        if (ce.stripMsgEn==null) {
            ce.stripMsgEn = ce.stripMsgRu;
        } else {
            ce.stripMsgRu = ce.stripMsgEn;
        }


*/
