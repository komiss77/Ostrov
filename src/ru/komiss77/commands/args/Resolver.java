package ru.komiss77.commands.args;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.key.Key;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import ru.komiss77.modules.world.XYZ;

import javax.annotation.Nullable;
import java.util.List;

public class Resolver {

  public static RequiredArgumentBuilder<CommandSourceStack, PlayerSelectorArgumentResolver> player(final String name) {
    return Commands.argument(name, ArgumentTypes.player());
  }

  public static Player player(final CommandContext<CommandSourceStack> cntx, final String name) throws CommandSyntaxException {
    final List<Player> pls = cntx.getArgument(name, PlayerSelectorArgumentResolver.class).resolve(cntx.getSource());
    if (pls.isEmpty()) throw new SimpleCommandExceptionType(() -> "No selection made...").create();
    return pls.getFirst();
  }

  public static RequiredArgumentBuilder<CommandSourceStack, PlayerSelectorArgumentResolver> players(final String name) {
    return Commands.argument(name, ArgumentTypes.players());
  }

  public static List<Player> players(final CommandContext<CommandSourceStack> cntx, final String name) throws CommandSyntaxException {
    return cntx.getArgument(name, PlayerSelectorArgumentResolver.class).resolve(cntx.getSource());
  }

  public static RequiredArgumentBuilder<CommandSourceStack, EntitySelectorArgumentResolver> entity(final String name) {
    return Commands.argument(name, ArgumentTypes.entity());
  }

  public static Entity entity(final CommandContext<CommandSourceStack> cntx, final String name) throws CommandSyntaxException {
    final List<Entity> ents = cntx.getArgument(name, EntitySelectorArgumentResolver.class).resolve(cntx.getSource());
    if (ents.isEmpty()) throw new SimpleCommandExceptionType(() -> "No selection made...").create();
    return ents.getFirst();
  }

  public static RequiredArgumentBuilder<CommandSourceStack, EntitySelectorArgumentResolver> entities(final String name) {
    return Commands.argument(name, ArgumentTypes.entities());
  }

  public static List<Entity> entities(final CommandContext<CommandSourceStack> cntx, final String name) throws CommandSyntaxException {
    return cntx.getArgument(name, EntitySelectorArgumentResolver.class).resolve(cntx.getSource());
  }

  public static RequiredArgumentBuilder<CommandSourceStack, BlockPositionResolver> position(final String name) {
    return Commands.argument(name, ArgumentTypes.blockPosition());
  }

  public static XYZ position(final CommandContext<CommandSourceStack> cntx, final String name) throws CommandSyntaxException {
    final BlockPosition bp = cntx.getArgument(name, BlockPositionResolver.class).resolve(cntx.getSource());
    return new XYZ("", bp.blockX(), bp.blockY(), bp.blockZ());
  }

  public static RequiredArgumentBuilder<CommandSourceStack, Integer> integer(final String name) {
    return Commands.argument(name, IntegerArgumentType.integer());
  }

  public static RequiredArgumentBuilder<CommandSourceStack, Integer> integer(final String name, final int min) {
    return Commands.argument(name, IntegerArgumentType.integer(min));
  }

  public static RequiredArgumentBuilder<CommandSourceStack, Integer> integer(final String name, final int min, final int max) {
    return Commands.argument(name, IntegerArgumentType.integer(min, max));
  }

  public static int integer(final CommandContext<CommandSourceStack> cntx, final String name) throws CommandSyntaxException {
    try {
      return IntegerArgumentType.getInteger(cntx, name);
    } catch (final IllegalArgumentException e) {
      throw new SimpleCommandExceptionType(() -> "Wrong argument for " + name).create();
    }
  }

  public static RequiredArgumentBuilder<CommandSourceStack, Double> descimal(final String name) {
    return Commands.argument(name, DoubleArgumentType.doubleArg());
  }

  public static RequiredArgumentBuilder<CommandSourceStack, Double> descimal(final String name, final int min) {
    return Commands.argument(name, DoubleArgumentType.doubleArg(min));
  }

  public static RequiredArgumentBuilder<CommandSourceStack, Double> descimal(final String name, final int min, final int max) {
    return Commands.argument(name, DoubleArgumentType.doubleArg(min, max));
  }

  public static double descimal(final CommandContext<CommandSourceStack> cntx, final String name) throws CommandSyntaxException {
    try {
      return DoubleArgumentType.getDouble(cntx, name);
    } catch (final IllegalArgumentException e) {
      throw new SimpleCommandExceptionType(() -> "Wrong argument for " + name).create();
    }
  }

  public static RequiredArgumentBuilder<CommandSourceStack, String> string(final String name) {
    return Commands.argument(name, StringArgumentType.string());
  }

  public static String string(final CommandContext<CommandSourceStack> cntx, final String name) throws CommandSyntaxException {
    try {
      return StringArgumentType.getString(cntx, name);
    } catch (final IllegalArgumentException e) {
      throw new SimpleCommandExceptionType(() -> "Wrong argument for " + name).create();
    }
  }

  public static RequiredArgumentBuilder<CommandSourceStack, Boolean> bool(final String name) {
    return Commands.argument(name, BoolArgumentType.bool());
  }

  public static boolean bool(final CommandContext<CommandSourceStack> cntx, final String name) throws CommandSyntaxException {
    try {
      return BoolArgumentType.getBool(cntx, name);
    } catch (final IllegalArgumentException e) {
      throw new SimpleCommandExceptionType(() -> "Wrong argument for " + name).create();
    }
  }

  public static RequiredArgumentBuilder<CommandSourceStack, Key> key(final String name) {
    return Commands.argument(name, ArgumentTypes.key());
  }

  public static Key key(final CommandContext<CommandSourceStack> cntx, final String name) throws CommandSyntaxException {
    try {
      return cntx.getArgument(name, Key.class);
    } catch (final IllegalArgumentException e) {
      throw new SimpleCommandExceptionType(() -> "Wrong argument for " + name).create();
    }
  }

  public static RequiredArgumentBuilder<CommandSourceStack, World> world(final String name) {
    return Commands.argument(name, ArgumentTypes.world());
  }

  public static World world(final CommandContext<CommandSourceStack> cntx, final String name) throws CommandSyntaxException {
    try {
      return cntx.getArgument(name, World.class);
    } catch (final IllegalArgumentException e) {
      throw new SimpleCommandExceptionType(() -> "Wrong argument for " + name).create();
    }
  }

  public static RequiredArgumentBuilder<CommandSourceStack, Integer> time(final String name) {
    return Commands.argument(name, ArgumentTypes.time());
  }

  public static RequiredArgumentBuilder<CommandSourceStack, Integer> time(final String name, final int min) {
    return Commands.argument(name, ArgumentTypes.time(min));
  }

  public static int time(final CommandContext<CommandSourceStack> cntx, final String name) throws CommandSyntaxException {
    try {
      return cntx.getArgument(name, Integer.TYPE);
    } catch (final IllegalArgumentException e) {
      throw new SimpleCommandExceptionType(() -> "Wrong argument for " + name).create();
    }
  }
}
