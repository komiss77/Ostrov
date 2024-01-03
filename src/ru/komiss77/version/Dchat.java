package ru.komiss77.version;




public interface Dchat {

    //String setHexColors(String s);

    //String getLastHex(String s);

    //String getVersion();

    //void sendDirectChat(Player player, String s, String s1, Player player1, Player player2);

    //void sendDeluxeChat(Player player, String s, String s1, Set set);

    //void sendFancyMessage(CommandSender commandsender, FancyMessage fancymessage);

    void sendBungeeChat(final String serverName, final String senderName, final String format, final String msg, final boolean override);

    //void sendPrivateMessage(Player player, String s, String s1);

    //String convertMsg(Player player, String s);

    //String convertPm(Player player, String s);
}