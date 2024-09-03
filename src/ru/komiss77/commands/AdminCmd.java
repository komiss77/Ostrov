package ru.komiss77.commands;


@Deprecated
public class AdminCmd {
}/* implements OCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("admin")
            .executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player pl)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                final Oplayer op = PM.getOplayer(pl);
                if (op.hasGroup("xpanitely") || op.hasGroup("owner")) {
                    SmartInventory.builder().id("Admin " + cs.getName())
                        .provider(new AdminInv()).size(3, 9)
                        .title("§dМеню Абьюзера").build().open(pl);
                    return Command.SINGLE_SUCCESS;
                }

                cs.sendMessage("§cУ вас нету разрешения на это!");
                return 0;
            })
            .build();
    }

    @Override
    public List<String> aliases() {
        return List.of("админ");
    }

    @Override
    public String description() {
        return "Открывает меню Абьюзера";
    }
}
*/