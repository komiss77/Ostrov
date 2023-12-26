package ru.komiss77.enums;


public enum StatFlag {
    
    
    
        Pandora(1,"Шкатулка пандоры сегодня"),
        InformatorOff(2,"откл.сообщения автоинформатора"),
        NewBieDone(3,"посвящение новичка"),
        LocalChat(4,"Включать локальный чат"),
        //FirstMissionDone(5,"Задание первая миссия в лоббм"),


        ;
    
        public final int tag;
        public final String displayName;

        private StatFlag (final int tag, final String displayName) {
            this.tag = tag;
            this.displayName = displayName;
        }
    

    public static boolean hasFlag(final int flagsArray, final StatFlag flag) {
        return (flagsArray & (1 << flag.tag)) == (1 << flag.tag);
    }


}
