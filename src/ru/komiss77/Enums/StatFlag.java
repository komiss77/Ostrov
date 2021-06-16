package ru.komiss77.Enums;


public enum StatFlag {
    
    
    
        Pandora(1,"Шкатулка пандоры"),


        ;
    
        public final int tag;
        public final String displayName;

        private StatFlag (final int tag, final String displayName) {
            this.tag = tag;
            this.displayName = displayName;
        }
    


}
