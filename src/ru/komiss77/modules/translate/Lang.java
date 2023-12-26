package ru.komiss77.modules.translate;

import com.destroystokyo.paper.ClientOption;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.util.HttpConstants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import ru.komiss77.Ostrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.Timer;
import ru.komiss77.events.ChatPrepareEvent;
import ru.komiss77.listener.ChatLst;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;

//https://github.com/DeepLcom/deepl-java?tab=readme-ov-file
//https://github.com/AsyncHttpClient/async-http-client


public class Lang {

    //Идентификатор ключа: ajehqd0ihg63s9sefjak Ваш секретный ключ: AQVN0dNBKMDD4njnzVS20UcLvvz9KkNnekav6qFa

    
    //добавить локальный буфер в файлике
    
    //private static final EnumMap<EnumLang, HashMap<String, String>> langs = langMaps();
    private static final Map<String, String> ruToEng;//Map<Integer, HashMap<String, String>> ruToEng;-возможно потом добавить сортировку по длинне
    public static int updateStamp;
    private static final RequestBuilder rb;
    
    static {
        ruToEng = new ConcurrentHashMap<>();
        rb = new RequestBuilder(HttpConstants.Methods.POST)
                .setUrl("https://translate.api.cloud.yandex.net/translate/v2/translate")
                .setHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Api-Key AQVN0dNBKMDD4njnzVS20UcLvvz9KkNnekav6qFa")
                //.setBody("{\"targetLanguageCode\":\"en\",\"folderId\":\"b1g583enhsdlegeb50uu\",\"texts\":\""+ruMsg+"\"}")
                .setCharset(Charset.forName("UTF-8"));
    }

    /*private static EnumMap<EnumLang, HashMap<String, String>> langMaps() {
        final EnumMap<EnumLang, HashMap<String, String>> lmp = new EnumMap<>(EnumLang.class);
        for (final EnumLang rl : EnumLang.values()) lmp.put(rl, new HashMap<>());
        return lmp;
    }*/

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
            if (add>0) Ostrov.log_ok("Lang loadBase добавлено записей : §b"+add+" (всего:"+ruToEng.size()+")");
        } catch (SQLException ex) {
            Ostrov.log_err("Lang loadBase error : "+ex.getMessage());
        }
    }
    
    public static String t (final Player p, final String ruMsg) {
        final boolean eng = !p.getClientOption(ClientOption.LOCALE).equals("ru_ru");
//Ostrov.log("sendMessage locale="+locale);
        if (eng) {
            return translate(ruMsg);
        } else {
            return ruMsg;
        }
    }
    
    
    //подменять >p.sendMessage(< на >Lang.sendMessage(p, <
    //p.getClientOption(ClientOption.LOCALE).equalsIgnoreCase("ru_ru")
    public static void sendMessage (final Player p, final String ruMsg) {
        //final Oplayer op = PM.getOplayer(p);
        final String locale = p.getClientOption(ClientOption.LOCALE);
//Ostrov.log("sendMessage locale="+locale);
        if (locale.equals("ru_ru")) {
            p.sendMessage(ruMsg);
        } else {
            p.sendMessage(translate(ruMsg));
        }
    }
    
    //переводик сообщений
    //public static String t (final Player p, final String ruMsg) {
    //    final Oplayer op = PM.getOplayer(p);
    //    return op==null ? ruMsg : t(op, ruMsg);
    //}
    
    
     //откинуть цветовой код!!
    private static String translate (final String ruMsg) {
        //if (!op.e) return ruMsg;
        String trans = ruToEng.get(ruMsg);
        if (trans == null) { //перевода нема
            
            ruToEng.put(ruMsg, ruMsg); //вставить заглушку, чтобы не дублировало запросы на переводы

            final Request request = rb.setBody("{\"targetLanguageCode\":\"en\",\"folderId\":\"b1g583enhsdlegeb50uu\",\"texts\":\""+ruMsg+"\"}").build();
            
            final AsyncCompletionHandler ah = new AsyncCompletionHandler() {
                @Override
                public Object onCompleted(Response response) throws Exception {
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
                    translateResult = translateResult.replaceAll("'", ""); // ' багает мускул
                    
                    ruToEng.put(ruMsg, translateResult);
                    OstrovDB.executePstAsync(Bukkit.getConsoleSender(), "INSERT INTO `lang` (`lenght`, `rus`, `eng`, `stamp`) VALUES ('"+ruMsg.length()+"', '"+ruMsg+"', '"+translateResult+"', '"+Timer.getTime()+"')  ON DUPLICATE KEY UPDATE eng=VALUES(eng), stamp=VALUES(stamp)");
//Ostrov.log_ok("t:"+ruMsg+"->"+translateResult);
                    return AsyncHandler.State.ABORT;
                }
            };

            try {
                final Future f = Ostrov.HTTP.executeRequest(request, ah);
                f.get();
            } catch (InterruptedException | ExecutionException | NullPointerException ex) {
                Ostrov.log_err("Lang t error : "+ex.getMessage());
            }

            return ruMsg;
        }
        return trans;
    }

  /*  */
    
    
    
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
        final Request request;
        if (ce.stripMsgRu!=null) {
            request = rb.setBody("{\"targetLanguageCode\":\"en\",\"folderId\":\"b1g583enhsdlegeb50uu\",\"texts\":\""+ce.stripMsgRu+"\"}").build();
        } else {
            request = rb.setBody("{\"targetLanguageCode\":\"ru\",\"folderId\":\"b1g583enhsdlegeb50uu\",\"texts\":\""+ce.stripMsgEn+"\"}").build();
        }
        
        final AsyncCompletionHandler ah = new AsyncCompletionHandler() {
            @Override
            public Object onCompleted(Response response) throws Exception {
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
            final Future f = Ostrov.HTTP.executeRequest(request, ah);
            f.get();
        } catch (InterruptedException | ExecutionException | NullPointerException ex) {
            if (ce.stripMsgEn==null) {
                ce.stripMsgEn = ce.stripMsgRu;
            } else {
                ce.stripMsgRu = ce.stripMsgEn;
            }
            Ostrov.log_err("Lang t error : "+ex.getMessage());
        }
    }

}
