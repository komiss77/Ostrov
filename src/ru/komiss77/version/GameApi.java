package ru.komiss77.version;

import java.util.Optional;
import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import ru.komiss77.modules.world.WXYZ;


public class GameApi {

  public static String fromComponent(net.kyori.adventure.text.Component paperComponent) {
    if (paperComponent == null) return "";
    net.minecraft.network.chat.Component component = PaperAdventure.asVanilla(paperComponent);
    if (component instanceof io.papermc.paper.adventure.AdventureComponent)
      component = ((io.papermc.paper.adventure.AdventureComponent) component).deepConverted();
    component = (net.minecraft.network.chat.Component) component;
    StringBuilder out = new StringBuilder();

    boolean hadFormat = false;
    for (Component c : component) {
      Style modi = c.getStyle();
      TextColor color = modi.getColor();
      if (c.getContents() != PlainTextContents.EMPTY || color != null) {
        if (color != null) {
          if (color.format != null) {
            out.append(color.format);
          } else {
            out.append(ChatColor.COLOR_CHAR).append("x");
            for (char magic : color.serialize().substring(1).toCharArray()) {
              out.append(ChatColor.COLOR_CHAR).append(magic);
            }
          }
          hadFormat = true;
        } else if (hadFormat) {
          out.append(ChatColor.RESET);
          hadFormat = false;
        }
      }
      if (modi.isBold()) {
        out.append(ChatFormatting.BOLD);
        hadFormat = true;
      }
      if (modi.isItalic()) {
        out.append(ChatFormatting.ITALIC);
        hadFormat = true;
      }
      if (modi.isUnderlined()) {
        out.append(ChatFormatting.UNDERLINE);
        hadFormat = true;
      }
      if (modi.isStrikethrough()) {
        out.append(ChatFormatting.STRIKETHROUGH);
        hadFormat = true;
      }
      if (modi.isObfuscated()) {
        out.append(ChatFormatting.OBFUSCATED);
        hadFormat = true;
      }
      c.getContents().visit((x) -> {
        out.append(x);
        return Optional.empty();
      });
    }
    return out.toString();
  }


  //для избавления твиста от НМС.
  public static void setFastMat(final WXYZ wxyz, final int sizeX, final int sizeY, final int sizeZ, final Material mat) {
    final ServerLevel sl = Craft.toNMS(wxyz.w);
    final net.minecraft.world.level.block.state.BlockState bs = ((CraftBlockData) mat.createBlockData()).getState();
    for (int x_ = 0; x_ != sizeX; x_++) {
      for (int z_ = 0; z_ != sizeZ; z_++) {
        for (int y_ = 0; y_ != sizeY; y_++) {
          Nms.mutableBlockPosition.set(wxyz.x + x_, wxyz.y + y_, wxyz.z + z_);
          //CraftBlock.setTypeAndData(sl, Nms.mutableBlockPosition, sl.getBlockState(Nms.mutableBlockPosition), bs, false);
          Nms.setNmsData(sl, Nms.mutableBlockPosition, sl.getBlockState(Nms.mutableBlockPosition), bs);
          //sl.setBlock(mutableBlockPosition, bs,)
        }
      }
    }
  }

}
