package ru.komiss77.enums;

import java.util.Arrays;
import java.util.List;


public enum Settings {

    //все флаги сделаны наоборот

    Fr_RecieveEntryMsgDeny(1, 5, "§6Оповещения о Онлайне", Arrays.asList("<mithril>Видеть когда друзья", "<mithril>заходят / выходят с сервера?")),
    @Deprecated //зачем разделять вход и выход?
    Fr_RecieveExitMsgDeny(2, 100, "§6Получение оповещений о выходе", Arrays.asList("<mithril>Получать оповещения", "<mithril>когда друзья отключаются?")),
    Fr_SendEntryMsgDeny(3, 6, "§6Отправка Онлайна", Arrays.asList("<mithril>Друзьям видеть когда ты", "<mithril>заходишь / выходишь с сервера?")),
    @Deprecated //зачем разделять вход и выход?
    Fr_SendExitMsgDeny(4, 100, "§6Отправление оповещений о выходе", Arrays.asList("<mithril>Отправлять друзьям оповещение", "<mithril>когда вы отключетесь?")),
    Fr_RecieveSwitchMsgDeny(5, 7, "§6Оповещения о Переходах", Arrays.asList("<mithril>Получать оповещения", "<mithril>когда друзья меняют сервер?")),
    Fr_SendSwitchMsgDeny(6, 8, "§6Отправка Переходов", Arrays.asList("<mithril>Оповещять друзей когда", "<mithril>ты меняешь режим?")),

    HideOnline(7, 1, "§3Режим 'Невидимка'", Arrays.asList("<mithril>показывать друзьям", "<mithril>ваше присутствие на сервере?")),
    MsgDeny(8, 3, "§3Личные Сообщения", Arrays.asList("<mithril>Получать личные сообщения", "<mithril>от друзей? Так же работает", "<mithril>когда ты оффлайн!")),
    @Deprecated //зачем разделять обычные и оффлайн?
    MsgOfflineDeny(9, 100, "§3Оффлайн сообщения", Arrays.asList("<mithril>Разрешить друзьям ",
        "<mithril>оставлять Вам сообщения", "<mithril>когда Вы не на сервере?", "<mithril>(Вы их сможете прочитать", "<mithril>при следующем входе)")),// - отделььная графа
    TeleportDeny(10, 2, "§eМаячёк", Arrays.asList("<mithril>Разрешить друзьям", "<mithril>отправлять Вам запросы", "<mithril>на телепорт?")),
    InviteDeny(11, 0, "§6Открытость", Arrays.asList("<mithril>Получать предложения дружить", "<mithril>от других игроков?")),
    HideNonFriends(12, 4, "§9Видимость Игроков §8(лобби)", Arrays.asList("<mithril>Видеть игроков, которые", "<mithril>не у тебя в друзьях?")),

    @Deprecated //переделал
    ShowOtherDeny(13, 8, "§6Видеть остальных в лобби", Arrays.asList("")),

    HideNonParty(14, 4, "§9Видеть только комманду §8(лобби)", Arrays.asList("<mithril>Видеть игроков, которые", "<mithril>не у тебя в комманде?")),
    LeaderTrackDeny(15, 0, "§eОтслеживать лидера", Arrays.asList("<mithril>Получать уведомления,", "<mithril>когда лидер меняет сервер?")),
    LeaderFollowDeny(16, 1, "§eСледовать за лидером", Arrays.asList("<mithril>Телепортировать Вас к лидеру", "<mithril>когда он меняет сервер?", "<mithril>(полезно для командных игр)")),
    InviteFriendsDeny(17, 2, "§eОткрытость друзьям", Arrays.asList("<mithril>Получать предложения", "<mithril>вступить в команду от друзей?")),
    InviteOthersDeny(18, 3, "§eОткрытость остальным", Arrays.asList("<mithril>Получать предложения", "<mithril>вступить в команду от остальных?")),

    @Deprecated //это нужно только гостям + это ломает квесты на серверах где ОНИ ИСПОЛЬЗУЮТСЯ
    JustGame(19, 0, "§8Играть без квестов", Arrays.asList("<mithril>Играть без всяких квестов")),
    ;

    public final int tag;
    public final int menuSlot;
    public final String displayName;
    public final List<String> description;


    Settings(final int tag, final int menuSlot, final String displayName, final List<String> description) {
        this.tag = tag;
        this.menuSlot = menuSlot;
        this.displayName = displayName;
        this.description = description;
    }


    public static boolean hasSettings(final int settingsArray, final Settings settings) {
        return (settingsArray & (1 << settings.tag)) == (1 << settings.tag);
    }


}