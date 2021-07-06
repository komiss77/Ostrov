
import java.util.Calendar;
import java.util.TimeZone;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import static ru.komiss77.Ostrov.calendar;





public class Main {
    
    
    //static Calendar calendar = Calendar.getInstance();
    
    
    public static void main(String[] args) {
        calendar = Calendar.getInstance();
        Ostrov.calendar.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        Ostrov.calendar.setTimeInMillis(System.currentTimeMillis());
        Ostrov.calendar.set(Calendar.DAY_OF_YEAR, Ostrov.calendar.get(Calendar.DAY_OF_YEAR)+1);
        Ostrov.calendar.set(Calendar.HOUR_OF_DAY, 0);
        Ostrov.calendar.set(Calendar.MINUTE, 0);
        Ostrov.calendar.set(Calendar.SECOND, 0);
        int midnightStamp = (int) (Ostrov.calendar.getTimeInMillis()/1000);
        
        System.out.println("midnightStamp="+midnightStamp);
        
        int left = midnightStamp - (int)(System.currentTimeMillis()/1000);
        System.out.println("left="+left);

        //Ostrov.random = new Random();
        
        //System.out.println(getPercentLine(100, 27));
        //System.out.println(calendar.get(Calendar.HOUR)+":"+String.format("%02d", calendar.get(Calendar.MINUTE))  );
        
        System.out.println(ApiOstrov.secondToTime( left ));
        
        
        
    }
    
    
    private static String getPercentLine(final int max, final int current) { 
System.out.println("max="+max+" curr="+current);
        final double percent = (double)current / max * 100;
        int p10 = (int) (percent*10);
        final double percent1d = (double) p10 / 10;
System.out.println("current / max = "+(current / max));
System.out.println("current / max) * 1000 = "+((current / max) * 1000));
System.out.println("percent="+percent);
System.out.println("p10="+p10);
System.out.println("percent1d="+percent1d);
        final int pos = p10/40;
System.out.println("pos="+pos);
        StringBuilder sb = new StringBuilder("§a||||||||||||||||||||||||| ");
System.out.println(">>"+sb.toString());
        return sb.insert(pos, "§8").append(percent1d).append("%").toString();
       /* line = line.
        String res="   ";
        double delta=(max_value-base_value)/10;
        if(delta<1)delta=1;
        for (int i=0; i<10; i++) {
            if (curr_value>=base_value) res=res+"§a| | ";
            else res=res+"§8| | ";
            base_value=(int) (base_value+delta);
        }
        return  res+"   ";*/
    } 
    
}
