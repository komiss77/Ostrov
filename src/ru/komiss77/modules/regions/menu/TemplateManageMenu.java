package ru.komiss77.modules.regions.menu;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.regions.RM;
import ru.komiss77.modules.regions.Template;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class TemplateManageMenu implements InventoryProvider {

  @Override
  public void init(final Player p, final InventoryContent contents) {

    //final ArrayList<Template> list = new ArrayList<>(TemplateManager.getTemplates(p.getWorld()));
    //Collections.sort(list);

    for (final Template t : RM.templates.values()) {
      final ItemStack is = new ItemBuilder((t.getIconMat() == null) ? Material.OAK_FENCE : t.getIconMat().getType())
          .name(t.name)
          .lore(t.displayname)
          //final ArrayList <String> list2 = new ArrayList(template.getDescription());
          //list2.replaceAll(s -> TCUtil.translateAlternateColorCodes('&', s));
          .lore(t.description)
          .build();

      //final RegionClaim claim;
      contents.add(ClickableItem.of(is, e -> {
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        RM.editTemplate(p, t);
      }));
    }

    contents.set(5, 4, new InputButton(InputType.ANVILL, new ItemBuilder(Material.EMERALD).name("§2Создание заготовки").build(), "Заготовка", name -> {
      if (name.length() > 16 || !StringUtil.checkString(name, false, true, false)) {
        p.sendMessage("§cНедопустимое имя!");
      } else if (RM.template(name) != null) {
        p.sendMessage("§cТакая заготовка уже есть");
      } else {
        final Template t = new Template(name);
        RM.templates.put(t.name, t);
        RM.saveTemplate(t);
        RM.editTemplate(p, t);
      }
    }));


  }


}
