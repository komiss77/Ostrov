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

//https://github.com/DeepLcom/deepl-java?tab=readme-ov-file
//https://github.com/AsyncHttpClient/async-http-client

//Идентификатор ключа: ajehqd0ihg63s9sefjak Ваш секретный ключ: AQVN0dNBKMDD4njnzVS20UcLvvz9KkNnekav6qFa
//добавить локальный буфер в файлике
//https://docs.papermc.io/paper/dev/component-api/i18n


public class Lang {
    
    
    @Deprecated
    public static String t (final String ruMsg, final EnumLang lang) {
        if (lang==EnumLang.RU_RU) {
            return ruMsg;
        } else {
            return translate(ruMsg, EnumLang.EN_US);
        }
    }
    @Deprecated
    public static  Component t (final Player p, final Object o) {
        return o instanceof Translatable ? t((Translatable) o, p) : err;
    }

    @Deprecated
    public static  Component t (final Object o, final Locale locale) {
        return o instanceof Translatable ? t((Translatable) o, locale) : err;
    }  

    @Deprecated
    public static String translate (final String ruMsg, final EnumLang l) {
        final Locale lang = l==EnumLang.RU_RU ? RU : EN;
        return translate(ruMsg, lang);
    }



    private static final Map<String, String> ruToEng;//Map<Integer, HashMap<String, String>> ruToEng;-возможно потом добавить сортировку по длинне
    public static int updateStamp;
    private static final HttpClient HTTP;
    private static final HttpRequest.Builder rb;
    public static final Locale RU, EN;
    private static final TextComponent err;
    

    static {
        ruToEng = new ConcurrentHashMap<>();
        HTTP = HttpClient.newHttpClient();
        rb = HttpRequest.newBuilder()
            .uri(URI.create("https://translate.api.cloud.yandex.net/translate/v2/translate"))
            .headers("Content-Type", "application/json", 
                    "Authorization", "Api-Key AQVN0dNBKMDD4njnzVS20UcLvvz9KkNnekav6qFa")
            //.POST(HttpRequest.BodyPublishers.ofString("{\"targetLanguageCode\":\""+("en")+"\",\"folderId\":\"b1g583enhsdlegeb50uu\",\"texts\":\""+"книга"+"\"}"))
            .timeout(Duration.of(5, ChronoUnit.SECONDS))
            .version(java.net.http.HttpClient.Version.HTTP_1_1);
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
            }
            updateStamp = Timer.getTime();
            if (add>0) {
                Ostrov.log_ok("Lang loadBase добавлено записей : §b"+add+" (всего:"+ruToEng.size()+")");
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
            
            ruToEng.put(ruMsg, ruMsg); //вставить заглушку, чтобы не дублировало запросы на переводы
            
            final HttpRequest request = rb.POST(HttpRequest.BodyPublishers.ofString("{\"targetLanguageCode\":\""+(locale==RU?"ru":"en")
                            +"\",\"folderId\":\"b1g583enhsdlegeb50uu\",\"texts\":\""+ruMsg.replace('\\', ' ')+"\"}"))
                            .build();
            
            final CompletableFuture cf = HTTP.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofByteArray())
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
        
        return trans;
    }

    public static void upd(final String ruMsg, final String translateResult) {
        ruToEng.put(ruMsg, translateResult);
        OstrovDB.executePstAsync(Bukkit.getConsoleSender(), "INSERT INTO `lang` (`lenght`, `rus`, `eng`, `stamp`) VALUES ('"+ruMsg.length()+"', '"+ruMsg+"', '"+translateResult+"', '"+Timer.getTime()+"')  ON DUPLICATE KEY UPDATE eng=VALUES(eng), stamp=VALUES(stamp)");
    }
    
    
    
//                              игрок           сообщ.              вставки в %...%
    /*public static String trans(final Player p, final String msg, final String... subs) {
        String tmg = langs.get(EnumLang.get(p.locale())).getOrDefault(msg, msg);//берем перевод
        final Pattern pt = Pattern.compile("%\\w+%");//например "...abc %player% abc..."
        final HashSet<String> snd = new HashSet<>();//чтоб одинаковые групировало
        final Matcher mt = pt.matcher(msg);
        for (int i = 0; mt.find(); i++) {//для каждого найденого %...%
            if (snd.add(mt.group())) tmg = tmg.replace(mt.group(), subs[i]);
        }

        return tmg;
    }*/


    
    
    //в эвенте переводим недостающий язык
    public static void translateChat(final ChatPrepareEvent ce) {
        //final Request request = rb.setBody("{\"targetLanguageCode\":\"ru\",\"folderId\":\"b1g583enhsdlegeb50uu\",\"texts\":\""+ce.oriStripMsg+"\"}").build();
        final HttpRequest request;
        if (ce.stripMsgRu!=null) {
            request = rb.POST(HttpRequest.BodyPublishers.ofString("{\"targetLanguageCode\":\"en\",\"folderId\":\"b1g583enhsdlegeb50uu\",\"texts\":\""
                    +ce.stripMsgRu.replace('\\', ' ')+"\"}")).build();
        } else {
            request = rb.POST(HttpRequest.BodyPublishers.ofString("{\"targetLanguageCode\":\"ru\",\"folderId\":\"b1g583enhsdlegeb50uu\",\"texts\":\""
                    +ce.stripMsgEn.replace('\\', ' ')+"\"}")).build();
        }
        
        final CompletableFuture cf = HTTP.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofByteArray())
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
                    }
                }
            })
            .exceptionally( ex -> {
                if (ce.stripMsgEn==null) {
                    ce.stripMsgEn = ce.stripMsgRu;
                } else {
                    ce.stripMsgRu = ce.stripMsgEn;
                }
                ChatLst.process(ce);
                Ostrov.log_err("Lang translateChat error : "+ex.getMessage());
                return null;
            });
        cf.join();
    }

    public static String getTranslate(final String rus) {
        return ruToEng.getOrDefault(rus, "");
    }



    
}



//НЕ УБИРАТЬ, МОЖЕТ ЕЩЁ ПРИГОДИТЬСЯ!!
        //rb = new RequestBuilder(HttpConstants.Methods.POST)
        //        .setUrl("https://translate.api.cloud.yandex.net/translate/v2/translate")
        //        .setHeader("Content-Type", "application/json")
        //        .addHeader("Authorization", "Api-Key AQVN0dNBKMDD4njnzVS20UcLvvz9KkNnekav6qFa")
                //.setBody("{\"targetLanguageCode\":\"en\",\"folderId\":\"b1g583enhsdlegeb50uu\",\"texts\":\""+ruMsg+"\"}")
        //        .setCharset(StandardCharsets.UTF_8);


        /* final Request request = rb.setBody("{\"targetLanguageCode\":\""+(locale==RU?"ru":"en")+"\",\"folderId\":\"b1g583enhsdlegeb50uu\",\"texts\":\""+ruMsg.replace('\\', ' ')+"\"}").build();
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
            request = rb.setBody("{\"targetLanguageCode\":\"en\",\"folderId\":\"b1g583enhsdlegeb50uu\",\"texts\":\""+ce.stripMsgRu+"\"}").build();
        } else {
            request = rb.setBody("{\"targetLanguageCode\":\"ru\",\"folderId\":\"b1g583enhsdlegeb50uu\",\"texts\":\""+ce.stripMsgEn+"\"}").build();
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