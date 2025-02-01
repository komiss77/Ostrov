package ru.komiss77.modules.enchants;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public record EnchData(EquipmentSlot es, ItemStack it, int lvl) {}
