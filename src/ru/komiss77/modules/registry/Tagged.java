package ru.komiss77.modules.registry;

import io.papermc.paper.registry.tag.Tag;
import org.bukkit.Keyed;

public record Tagged<T extends Keyed>(Tag<T> tag) {
//    public boolean has(final T val) {}
}
