package ru.komiss77.Objects;

import java.util.Arrays;






public class Group {
    
    public String name;
    public String chat_name;
    public String type;
    public int price_per_month;
    public String group_desc;
    public String mat;
    public int inv_slot;
    
    //public Collection<String> permissions;
    //public Collection<String> inheritance;
    public CaseInsensitiveLinkedTreeSet permissions;
    public CaseInsensitiveLinkedTreeSet inheritance;
    
    
    //для банжика, не менять на Материал!!

    public Group(final String name, final String chat_name, final String inheritance, final String type, final int price_per_month, final int inv_slot, final String mat, final String group_desc ) {
        this.name=name;
        this.chat_name=chat_name;
        this.type=type;
        this.price_per_month=price_per_month;
        this.inv_slot=inv_slot;
        this.mat=mat;
        this.group_desc=group_desc;
        //this.permissions=new CaseInsensitiveSet();
        this.permissions=new CaseInsensitiveLinkedTreeSet();
        //this.inheritance=new CaseInsensitiveSet();
        this.inheritance=new CaseInsensitiveLinkedTreeSet();
        this.inheritance.addAll(Arrays.asList(inheritance.split(", ")));
//System.out.println("NEW Group! name="+name+" chat_name="+chat_name+" inheritance="+inheritance+" permissions="+permissions);
    }



    public int getPrice(final int days) {//больше-дешевле
        return (int) ((double)price_per_month/30 * days *( days<30 ? 1.3 :  days<45? 1 :  days<75 ? 0.8 :  days<105 ? 0.7 :  days<165 ? 0.6 : 0.5 ) );
    }


    public boolean isStaff () {
        return type.equals("staff");
    }
   
    
    
    
    
    
}
