package ru.komiss77.modules.signProtect;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class AccesEdit implements InventoryProvider {

  private static final ItemStack fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§8.").build();


  private final Sign sign;
  private final ProtectionData pd;



  public AccesEdit(final Sign sign, final ProtectionData pd) {
    this.sign = sign;
    this.pd = pd;
  }



  @Override
  public void init(final Player p, final InventoryContent contents) {
    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BAMBOO_WOOD_PRESSURE_PLATE_CLICK_ON, 5, 0.5f);
    contents.fillBorders(ClickableItem.empty(AccesEdit.fill));
    contents.fillRow(3, ClickableItem.empty(AccesEdit.fill));


      for (final String name : pd.users) {

        contents.add( ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
            .name(name)
            .addLore("§7")
            .addLore("§7ЛКМ - §cудалить")
            .addLore("§7")
            .build(), e-> {
            if ( e.isLeftClick() && pd.users.remove(name)) {
              SignProtect.updateSign(sign, pd);
              reopen(p, contents);
            }
          }
        ));

      }

      if (pd.users.size()<14) {
        contents.add( ClickableItem.of(
            new ItemBuilder(Material.PLAYER_HEAD)
              .name("§aДобавить")
              .addLore( "" )
              .addLore("§fРазрешить доступ")
              .addLore("§fстоящему рядом")
              .addLore( "§fс вами игроку." )
              .setCustomHeadTexture(ItemUtils.Texture.add)
              .build(), e-> {
              if (e.isLeftClick()) {

                Player find = null;
                int minDistance = Integer.MAX_VALUE;
                for (final Player pl : p.getWorld().getPlayers()) {
                  if (p.getEntityId() != pl.getEntityId() && !pd.users.contains(pl.getName())) {
                    final int dst = LocationUtil.getDistance(p.getLocation(), pl.getLocation());
                    if (dst < minDistance && dst < 30) {
                      find = pl;
                      minDistance = dst;
                    }
                  }
                }
                if (find==null) {
                  p.sendMessage("§6Рядом никого не найдено!");
                } else {
                  pd.users.add(find.getName());// - List.of immutebleб в него не добавить!!
                  SignProtect.updateSign(sign, pd);
                  reopen(p, contents);
                }
              }
            }
          )
        );
      }




/*

    if (pi.userCount()<14) {
      contents.set(4, 1, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
        .name("§fДобавить разрешение")
        .addLore("§7")
        .addLore("§7Лимит 14 разрешений.")
        .addLore("§7Свободных ячеек: "+(14-pi.userCount()))
        .addLore("§7ЛКМ - ввести имя")
        .addLore("§7")
        .build(),  "ник", msg -> {

        final String strip = TCUtils.stripColor(msg);

        if(strip.length()>16 ) {
          p.sendMessage("§cЛимит 16 символов!");
          FM.soundDeny(p);
          return;
        }

        if (pi.addUser(msg)) {
          DbEngine.saveProtectionInfo(cLoc, sLoc, pi);
          updateSign();
          reopen(p, contents);
        } else {
          p.sendMessage("§cНичего не изменилось..");
          FM.soundDeny(p);
        }

      }));

    } else {

      contents.set(4, 1, ClickableItem.empty(new ItemBuilder( Material.NAME_TAG)
        .name("§7Лимит 14 разрешений!")
        .build()));

    }
*/


    if (ApiOstrov.isLocalBuilder(p, false) && pd.valid>0) {

      contents.set( 4, 1, ClickableItem.of(new ItemBuilder(Material.FIREWORK_ROCKET)
          .name("§bПометить постоянным")
          .addLore("§7")
          .addLore("§7ЛКМ - бессрочно")
          .addLore("§7")
          .build(), e-> {
          if ( e.isLeftClick()) {
            pd.valid = -1;
            SignProtect.updateSign(sign, pd);
            reopen(p, contents);
          }
        }
      ));

    } /*else if ( pi.validTo!=-1 && pi.validTo - FM.getTime() < 1209600) {//60*60*24*14

      contents.set( 4, 3, ClickableItem.of(new ItemBuilder(Material.FIREWORK_ROCKET)
          .name("§bПродлить ограничение")
          .addLore("§7")
          .addLore("§7ЛКМ - продлить на 3 месяца")
          .addLore("§7")
          .build(), e-> {
          if ( e.isLeftClick()) {
            pi.validTo = FM.getTime() + 60*60*24*90;
            DbEngine.saveProtectionInfo(cLoc, sLoc, pi);
            reopen(p, contents);
          }
        }
      ));
    } else {

      contents.set( 4, 3, ClickableItem.empty(new ItemBuilder(Material.FIREWORK_ROCKET)
        .name("§bДействие ограничения")
        .addLore("§7")
        .addLore(pi.getExpiriedInfo())
        .addLore("§7")
        .addLore(pi.validTo!=-1 ? "§7Вы сможете продлить" : "§7Сломайте табличку,")
        .addLore(pi.validTo!=-1 ? "§7срок действия" : "§7чтобы снять")
        .addLore(pi.validTo!=-1 ? "§7за 2 недели" : "§7ограничение.")
        .addLore(pi.validTo!=-1 ? "§7до окончания." : "")
        .build()
      ));

    }*/
    final Oplayer op = PM.getOplayer(p);
    int curr = 1;
    if (op.mysqlData.containsKey("signProtect")) {
      curr = Integer.parseInt(op.mysqlData.get("signProtect"));
    }

    contents.set( 4, 4, ClickableItem.empty(new ItemBuilder(Material.PAPER)
        .name("§bЛимит табличек")
        .addLore("§7")
        .addLore("§7Найдено активных: "+curr)
        .addLore("§7Можно поставить: "+(SignProtectLst.LIMIT-curr))
        .build()
    ));




    contents.set( 4, 7, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("закрыть").build(), e ->
      p.closeInventory()
    ));





  }




}



