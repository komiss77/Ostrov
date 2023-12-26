package ru.komiss77.enums;


public enum ReportStage {
        
    //не перестывлять! работает по order
        Нет ( "", 0, 0, Operation.NONE, 0),
        Кик ( "выгнан, в следующий раз-бан на час", 3, 12, Operation.GKICK, 0),
        Бан1 ( "бан 1 час, в следующий раз - на день", 6, 24, Operation.GBAN, 60*60),
        Бан2 ( "бан 1 день, в следующий раз - на неделю", 12, 48, Operation.GBAN, 24*60*60),
        Бан3 ( "бан 1 неделя, в следующий раз - на месяц", 24, 96, Operation.GBAN, 7*24*60*60),
        Бан4 ( "бан 1 месяц, в следующий раз - на три", 48, 128, Operation.GBAN, 30*24*60*60),
        Бан5 ( "бан на три месяца", 96, 256, Operation.GBAN, 129600),
        ;

        
        public final int fromConsole;
        public final int fromPlayers;
        public final String msg;
        public final Operation action;
        public final int ammount;
        
        private ReportStage (final String msg, final int fromConsole, final int fromPlayers, final Operation action, final int ammount ) {
            this.msg = msg;
            this.fromConsole = fromConsole;
            this.fromPlayers = fromPlayers;
            this.action = action;
            this.ammount = ammount;
        }
        
        public static ReportStage get(final int order) {
            if (order<1 || order>=ReportStage.values().length) return Нет;
            return ReportStage.values()[order];
        }

        public static boolean reachedNext(final ReportStage current, final int fromConsole, final int fromPlayers) {
            final ReportStage nextStage = getNext(current);
            if (nextStage==current) return false;
            return nextStage.fromConsole==fromConsole || nextStage.fromPlayers==fromPlayers;
        }

        public static ReportStage getNext(final ReportStage current) {
            if (current.ordinal()>=ReportStage.values().length) return current;
            return get (current.ordinal()+1);
        }
        
}
