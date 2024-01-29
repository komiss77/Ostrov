package ru.komiss77.enums;

import java.util.Arrays;
import java.util.List;


public enum Settings {
    
    //все флаги сделаны наоборот
    
    Fr_RecieveEntryMsgDeny  (1, 9, "§6Получение оповещений о входе", Arrays.asList("§7Получать оповещения","§7когда друзья заходят на остров?") ),
    Fr_RecieveExitMsgDeny   (2, 10, "§6Получение оповещений о выходе", Arrays.asList("§7Получать оповещения","§7когда друзья отключаются?") ),
    Fr_SendEntryMsgDeny     (3, 12, "§6Отправление оповещений о входе", Arrays.asList("§7Отправлять друзьям оповещение","§7когда вы заходите на остров?") ),
    Fr_SendExitMsgDeny      (4, 134, "§6Отправление оповещений о выходе", Arrays.asList("§7Отправлять друзьям оповещение","§7когда вы отключетесь?") ),
    Fr_RecieveSwitchMsgDeny (5, 11, "§6Получение оповещений о переходе", Arrays.asList("§7Получать оповещения","§7когда друзья меняют сервер?") ),
    Fr_SendSwitchMsgDeny    (6, 14, "§6Отправление оповещений о переходе", Arrays.asList("§7Отправлять друзьям оповещение","§7когда вы меняете серер?") ),
    
    Fr_HideOnline           (7, 1, "§3Режим 'невидимка'", Arrays.asList("§7показывать друзьям","§7ваше присутствие на сервере?") ),
    Fr_MsgDeny              (8, 3, "§3Личные сообщения", Arrays.asList("§7Получать личные сообщения","§7от друзей?") ),
    Fr_MsgOfflineDeny       (9, 4, "§3Офф-лайн сообщения", Arrays.asList("§7Разрешить друзьям ","§7оставлять Вам сообщения","§7когда Вы не на сервере?","§7(Вы их сможете прочитать","§7при следующем входе)") ),// - отделььная графа
    
    Fr_TeleportDeny         (10, 2, "§eМаячёк", Arrays.asList("§7Разрешить друзьям","§7отправлять Вам запросы","§7на телепорт?") ),
    
    Fr_InviteDeny           (11, 0, "§eОткрытость", Arrays.asList("§7Получать предложения дружить","§7от других игроков?") ),
    Fr_ShowFriendDeny       (12, 6, "§6Видеть друзей в лобби", Arrays.asList("") ),
    Fr_ShowPartyDeny        (13, 7, "§6Видеть команду в лобби", Arrays.asList("") ),
    Fr_ShowOtherDeny        (14, 8, "§6Видеть остальных в лобби", Arrays.asList("") ),
    //ВИДЕТЬ_ВСЕХ(13, 0),
    //ВИДЕТЬ_КОМАНДУ(14, 0),
    //ВИДЕТЬ_ДРУЗЕЙ(15, 0),
    
    Party_LeaderTrackDeny       (15, 11, "§eОтслеживать лидера", Arrays.asList("§7Получать уведомления,","§7когда лидер меняет сервер?") ),
    Party_SlaveDeny             (16, 12, "§eСледовать за лидером", Arrays.asList("§7Телепортировать Вас к лидеру","§7когда он меняет сервер?","§7(полезно для командных игр)") ),
    //ТЕЛЕПОРТ_К_ЛИДЕРУ(10, 0, 9, "§eВедOмый", "§7Когда Вы в команде","§7телепортировать Вас к лидеру","§7когда он меняет сервер?","§7(полезно для командных игр)"),
    Party_InviteFriendsDeny     (17, 13, "§eОткрытость друзьям", Arrays.asList("§7Получать предложения","§7вступить в команду от друзей?") ),
    Party_InviteOtherDeny       (18, 14, "§eОткрытость остальным", Arrays.asList("§7Получать предложения","§7вступить в команду от остальных?") ),
    
    //Party_InviteDenyOther         (13, 0, "§eОткрытость остальным", Arrays.asList("§7Получать предложения","§7вступить в команду от остальных?") ),
    JustGame(19, 0, "§7Играть без всяких квестов", Arrays.asList("§7Играть без всяких квестов")),
    ;
    
    public final int tag;
    public final int menuSlot;
    public final String displayName;
    public final List<String> description;
    
    
    private Settings(final int tag, final int menuSlot, final String displayName, final List<String> description){
        this.tag = tag;
        this.menuSlot = menuSlot;
        this.displayName = displayName;
        this.description = description;
    }
    

    
    public static boolean hasSettings(final int settingsArray, final Settings settings) {
        return (settingsArray & (1 << settings.tag)) == (1 << settings.tag);
    }


    
    
    

}