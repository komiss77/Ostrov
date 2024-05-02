import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.komiss77.LocalDB;
import ru.komiss77.utils.DateUtil;

public class test {
    

    
    public static void main(String[] args) {

String msg = "SK∫mg01∫∫РАБОТАЕТ∫0∫Острова∫SkyWorld∫РАБОТАЕТ∫∫";

      final String[]s = msg.split("∫");

      for (int i = 0; i < msg.length(); i++) {
        log("i="+i+" s="+s[i]);
      }
     /* StringBuilder sb=new StringBuilder();
      l.forEach( (s) -> {
        sb.append(s).append(",");
      });
      log (sb.toString());*/
//log("valid="+valid+" owner="+owner);
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
