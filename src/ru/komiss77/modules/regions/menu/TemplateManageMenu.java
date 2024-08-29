package ru.komiss77.modules.regions.menu;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
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

    for (final Template t : RM.templates.values()) {


      contents.add(ClickableItem.of(t.editorIcon(true), e -> {
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
        t.description.add("§7Краткое описание");
        RM.templates.put(t.name, t);
        RM.saveTemplate(t);
        RM.editTemplate(p, t);
      }
    }));


  }


}
