
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Future;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.util.HttpConstants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import ru.komiss77.utils.DateUtil;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gate
 */
public class test {
    
    private static final Calendar calendar;
    static {
        calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        calendar.setTimeInMillis(System.currentTimeMillis());
    }
    
    public static void main(String[] args) {
        
        
        
        /*
        https://www.baeldung.com/async-http-client
После того как мы настроили и получили экземпляр HTTP-клиента, мы можем повторно использовать его во всем приложении. Нам не нужно создавать экземпляр для каждого запроса, поскольку внутри него создаются новые потоки и пулы соединений, что приведет к проблемам с производительностью.
Также важно отметить, что как только мы закончим использовать клиент, нам следует вызвать метод close(), чтобы предотвратить любые утечки памяти или зависание ресурсов.
        */
        
        //HttpClient httpClient = HttpClientBuilder.create().build();
        //String result = "";
        try (AsyncHttpClient client = Dsl.asyncHttpClient()){
            
          /*  HttpPost request = new HttpPost("https://translate.api.cloud.yandex.net/translate/v2/translate");
            //String body = String.format("{\"targetLanguageCode\":\"en\",\"texts\":\"Остров готов к работе\",\"folderId\":\"aje8d83edl4ijvnu0aao\"}");
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Authorization", "Api-Key AQVN0dNBKMDD4njnzVS20UcLvvz9KkNnekav6qFa");
            
            String body = "{\"targetLanguageCode\":\"en\",\"texts\":\"Остров готов к работе\",\"folderId\":\"b1g583enhsdlegeb50uu\"}";
            StringEntity params = new StringEntity(body, "UTF-8");
            params.setContentType("charset=UTF-8");
            request.setEntity(params);
            
            HttpResponse response = httpClient.execute(request);

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String retSrc = EntityUtils.toString(entity);
                Object jsob_obj = new JSONParser().parse(retSrc);
                JSONObject json_res = (JSONObject) jsob_obj;
                JSONArray res_translate = (JSONArray) json_res.get("translations");
                JSONObject res_json_obj = (JSONObject) res_translate.get(0);
                result = (String) res_json_obj.get("text");
                //System.out.println(res_translate);
                log(result);
            } else {
                log ("entity==null : "+response.getStatusLine().getStatusCode());
            }*/

            //AsyncHttpClient client = Dsl.asyncHttpClient();
          //  AsyncHttpClient client = new AsyncHttpClient();
            
            Request request = new RequestBuilder(HttpConstants.Methods.POST)
                .setUrl("https://translate.api.cloud.yandex.net/translate/v2/translate")
                .setHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Api-Key AQVN0dNBKMDD4njnzVS20UcLvvz9KkNnekav6qFa")
                .setBody("{\"targetLanguageCode\":\"en\",\"texts\":\"Остров готов к работе\",\"folderId\":\"b1g583enhsdlegeb50uu\"}")
                .setCharset(Charset.forName("UTF-8"))
                .build();
//log("request="+request);            
            
            AsyncCompletionHandler ah = new AsyncCompletionHandler() {
                //@Override
                //public AsyncHandler.State onStatusReceived(HttpResponseStatus status) throws Exception {
                //    log("status="+status);
                //    return State.CONTINUE;
                //}
                @Override
                public Object onCompleted(Response response) throws Exception {
                    //log("response="+response);
                    return response;
                }
                @Override
                public AsyncHandler.State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                    //String b = new String(bodyPart.getBodyPartBytes());
                    //log("bodyPart="+b);
                        String retSrc = new String(bodyPart.getBodyPartBytes());//EntityUtils.toString(entity);
                        Object jsob_obj = new JSONParser().parse(retSrc);
                        JSONObject json_res = (JSONObject) jsob_obj;
                        JSONArray res_translate = (JSONArray) json_res.get("translations");
                        JSONObject res_json_obj = (JSONObject) res_translate.get(0);
                        String result = (String) res_json_obj.get("text");
                        log(result);
                    return State.CONTINUE;
               }
            };
            
            Future f = client.executeRequest(request, ah);
            //client.executeRequest(request, ah).done();
            var r = f.get();
log("f="+r);            
            //client.close();
            
           /* client.executeRequest(request, rh);
            
            client.get("https://www.google.com", new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                    // called when response HTTP status is "200 OK"
                }

            }); */
            
            /*CompletableFuture<HttpResponse<String>> response = client.sendAsync(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );*/
            

        } catch (Exception ex) {
            log("error : "+ex.getMessage());
        } finally {
            //httpClient.getConnectionManager().shutdown();
        }
        

        
    }
    

//    private static String toBin(int num) {
//        return String.format("%32s", Integer.toBinaryString(num)).replaceAll(" ", "0");
//    }
    
    //private static int nearly(int x, int y, int z) {
    //   System.out.print("nearly: x="+x+" x&0xC="+(x&0xC));
    //   return (x&0xFFFC)<<16 | (y&0xFFFC)<<8 | z&0xFFFC;
    //}
        
    
    
    
    
    
    private static void log(final String s) {
        System.out.println(s);            
    }    
    
    
    
    public static void mainaaaa(String[] args) {
      //  LocalDate ld = LocalDate.now();
//System.out.println(dateFromStamp(ld.));
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        //calendar.setFirstDayOfWeek(0);
        calendar.setTimeInMillis(System.currentTimeMillis());
        
//System.out.println(dateFromStamp(calendar.getTimeInMillis()));
        out(calendar);


        //while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
        //    calendar.add(Calendar.DATE, -1);
       // }
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        
        out(calendar);

        
        calendar.add(Calendar.DATE, -5*7);

        //int currentMonday = (int) (calendar.getTimeInMillis()/1000);
        //calendar.setTimeInMillis(currentMonday*1000);
        
//System.out.println("currentMonday="+currentMonday+" DAY_OF_WEEK="+calendar.get(Calendar.DAY_OF_WEEK)+" "+DateUtil.dayOfWeekName(calendar.get(Calendar.DAY_OF_WEEK)));
       // int fiveWeeksAgo = currentMonday - 7*5*24*60*60;
       // calendar.setTimeInMillis(fiveWeeksAgo*1000);
        
        
        out(calendar);


        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
      //  int dayEnd = fiveWeeksAgo+24*60*60-1;
      //  calendar.setTimeInMillis(dayEnd*1000);
        
        out(calendar);

        //for (int i = 1; i <=100; i++) {
        //    calendar.add(Calendar.HOUR_OF_DAY, 1);
        //    out(calendar);
        //}

        //out(calendar);
    }
    
    
    
    
    public static void out(Calendar calendar) {
        System.out.println( (int)(calendar.getTimeInMillis()/1000)+" "
            +dateFromStamp(calendar.getTimeInMillis())
            +" month="+(calendar.get(Calendar.MONTH)+1)+" DATE="+calendar.get(Calendar.DATE)+" DAY_OF_WEEK="+calendar.get(Calendar.DAY_OF_WEEK)
            +" "+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)
            +" "+DateUtil.dayOfWeekName(calendar.get(Calendar.DAY_OF_WEEK)));


    }
    
    
    public static String dateFromStamp(long stamp) {
        Date date=new java.util.Date(stamp);
        SimpleDateFormat full_sdf = new java.text.SimpleDateFormat("dd.MM.yy HH:mm");
        //date.setTime(stamp_in_second*1000L);
        return full_sdf.format(date);
    }    



    
    
    
    
    
    
    
}
   /*  Random rnd = new Random();
        
        Map<String,Integer> pl = new HashMap<>(5);
        pl.put("komiss77", 50); //hider chanct = 50
        pl.put("40_1", 40);
        pl.put("40_2", 40);
        pl.put("60", 60);
        pl.put("80_1", 80);
        pl.put("80_2", 80);
        //pl.put("10", 10);
        
        //final ValueSortedMap<String, Integer> hiderChanceMap = new ValueSortedMap<>();
        for (final String name : pl.keySet()) {
            //рандом от 0 до шанса прятаться
            //будут рассортированы в порядке увеличения шанса прятаться
            //из начала мапы берём охотников, из конца - зайцев
            int hideChance = rnd.nextInt(pl.get(name));
            pl.replace(name, hideChance); 
System.out.println("  --replace "+name+" : "+hideChance);
        }
        
        log("");
        log("sorted:");
        Stream<Map.Entry<String,Integer>> sorted = pl.entrySet().stream().sorted(Map.Entry.comparingByValue());
        final List<String> sortedList = new ArrayList<>(pl.size());
        for (Entry<String,Integer> entry : sorted.toList()) {
            log(entry.getKey()+":"+entry.getValue());
            sortedList.add(entry.getKey());
        }
        //while (!hiderChanceMap.isEmpty()) {
        //    Entry<String,Integer> entry = hiderChanceMap.pollFirstEntry();
        //    log(entry.getKey()+":"+entry.getValue());
        //}
        //for (final String name : hiderChanceMap.descendingKeySet()) {
            //log(name+":"+hiderChanceMap.get(name));
        //}
        log("");
        log("sortedList="+sortedList);
        log("");
        
        boolean seeker = true;
        int hiderCount = 2;//hidersPerSeker;
        String name;
        while (!sortedList.isEmpty()) {
            if (seeker) {
                //name = sortedList.get(0);
                name = sortedList.remove(0);
                log(name+">> seeker");
                seeker=false;
//System.out.println("  --add seeker list="+sortBySeekChancrList);                
            } else {
                if (hiderCount>0) {
                    name = sortedList.remove(sortedList.size()-1);
                    log(name+">> hider");
                    hiderCount--;
//System.out.println("  --add hider list="+sortBySeekChancrList +" hiderCount="+hiderCount);                
                } 
                if (hiderCount==0) {
//System.out.println("  --hiderCount=0! seeker = true list="+sortBySeekChancrList);                
                    hiderCount = 2;//hidersPerSeker;
                    seeker = true;
                }
            }
        }*/
