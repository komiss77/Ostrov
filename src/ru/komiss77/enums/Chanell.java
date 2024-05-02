package ru.komiss77.enums;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public enum Chanell {
    
    
    Action ("ostrov:type1"),
    Action_Sender("ostrov:type2"),
    Action_Sender_Int("ostrov:type3"),
    Action_Sender_String("ostrov:type4"),
    Action_Sender_Int_String("ostrov:type5"),
    Action_Sender_Int2_String2("ostrov:type6"),
    Action_Sender_Int3_String3("ostrov:type7"),
    Action_Sender_Int3_String6("ostrov:type8"),
    CHAT_RU ("ostrov:chatru"), 
    CHAT_EN ("ostrov:chaten"), 
    CHAT_PM ("ostrov:chatpm"), 
    SKIN("sr:messagechannel"), 
    ;
    
    public final String name;
    private static final Map<String,Chanell> nameMap;
    
    private Chanell (final String name) {
        this.name = name;
    }
    
    static {
        Map<String,Chanell> sm = new ConcurrentHashMap<>();
        for (Chanell chanell : Chanell.values()) {
            sm.put(chanell.name,chanell); //для velocity - не допускает :
            //sm.put("ostrov:"+chanell.name,chanell); //для старого банжика
        }
        nameMap = Collections.unmodifiableMap(sm);
    }
    
    public static Chanell fromName(final String name) {
        return nameMap.get(name);//stringMap.containsKey(asString) ? stringMap.get(asString) : EMPTY;
    }
    
}
