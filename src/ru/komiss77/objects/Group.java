package ru.komiss77.objects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;





    //не менять, классы бакит не импортироапть, использует bauth!


public class Group {
    
    public String name;
    public String chat_name;
    public String type;
    public int price_per_month;
    public String group_desc; 
    public String mat;
    public int inv_slot;
    public List<String>lore;
    public Set<String> permissions;
    public Set<String> inheritance; //наследуемые группы
    
    
    //для банжика, не менять на Материал!!

    public Group(final String name, final String chat_name, final String inheritance, final String type, final int price_per_month, final int inv_slot, final String mat, final String group_desc ) {
        this.name=name;
        this.chat_name=chat_name;
        this.type=type;
        this.price_per_month=price_per_month;
        this.inv_slot=inv_slot;
        this.mat=mat;
        this.group_desc=group_desc;
        this.permissions=new HashSet<>();
        this.inheritance=new HashSet<>();
        for (String inh:inheritance.split(",")) {
            this.inheritance.add(inh.trim());
        }
        
        lore = Gen_lore(null, group_desc, "§7");
        lore.add("");
        lore.add("§f15 дней §7- §b"+getPrice(15)+" рил");
        lore.add("§f1 месяц §7- §b"+getPrice(31)+" рил");
        lore.add("§f3 месяца §7- §b"+getPrice(90)+" рил");
        lore.add("");
        lore.add("§7ЛКМ - выбрать длительность");
        
        //this.inheritance.addAll(Arrays.asList(inheritance.split(", ")));
//System.out.println("NEW Group! name="+name+" chat_name="+chat_name+" inheritance="+inheritance+" permissions="+permissions);
    }



    public int getPrice(final int days) {//больше-дешевле
        return (int) ((double)price_per_month/30 * days *( days<30 ? 1.3 :  days<45? 1 :  days<75 ? 0.8 :  days<105 ? 0.7 :  days<165 ? 0.6 : 0.5 ) );
    }


    public boolean isStaff () {
        return type.equals("staff");
    }
   
    
    
    




    private static List<String> Gen_lore(List<String> current_lore, String text, String text_color){
        if (text_color==null) text_color="";
        text = text.replaceAll("&","§");
        if (current_lore==null) {
            current_lore=new ArrayList<>();
        }
        
        String[] блоки= {text};
        if (text.contains("<br>")) {
            блоки=text.split("<br>");
        }
        List<String> нарезка_построчно;
        //else блоки = {text};
        for(String блок: блоки){
            нарезка_построчно = split(блок, 25);
                for (String строчка : нарезка_построчно) {
                    current_lore.add(text_color+строчка);
                }
        }
            return current_lore;
			
    }
    
    
    private static List<String> split(String блок, int line_lenght) {
        //int text_lenght = блок.length()- (блок.length()-блок.replaceAll("§", "").length())*2;  //не учитываем цветовые коды в 
//System.out.println("line_lenght="+line_lenght+"to_split_lenght="+блок.length()+"  text_lenght="+text_lenght+" to_split="+блок);
        List<String> split=new ArrayList<>();
        if (блок.length() <= line_lenght) {
            split.add(блок);
            return split;
        }
        //String[] split = new String [text_lenght / line_lenght + 1];
        //Arrays.fill(split, "");
//System.out.println("  split=new String["+(int)(text_lenght / line_lenght + 1)+"]");        
        boolean nextLine = false;
        //int index = 0;
        int current_line_lenght=line_lenght;
        
        StringBuilder sb = new StringBuilder();
        char[] блок_array = блок.toCharArray();
        
        for (int position = 0; position < блок_array.length; position++) {
//System.out.println("111 index="+index+"  position="+position+" char="+блок_array[position] );        
            
            if (блок_array[position]=='§') {
//System.out.println("skip § 111 position="+position );        
                sb.append(блок_array[position]);
                //position++;
                current_line_lenght++;
                if (position < блок_array.length) {
                    position++;
                    sb.append(блок_array[position]);
                    current_line_lenght++;
                }
//System.out.println("skip § 222 position="+position );       
            } else {
//System.out.println("222 index="+index+"  position="+position );        
                if (position != 0 && position % current_line_lenght == 0) {
//System.out.println("nextLine 111 position="+position+"  current_line_lenght="+current_line_lenght );        
                    nextLine = true;
                }
                if(nextLine && (блок_array[position] == ' ' || блок_array[position] == '|' || блок_array[position] == ','  || блок_array[position] == '.')) {
                    nextLine = false;
                    split.add(sb.toString());
                    //index++;
                    sb = new StringBuilder();
                    current_line_lenght=line_lenght;
//System.out.println("nextLine 222 index="+index+" position="+position+"  current_line_lenght="+current_line_lenght );        
                } else sb.append(блок_array[position]);
            }
        }
        split.add(sb.toString()); //добавляем, что осталось
        

        return split;
    }
	

    
}
