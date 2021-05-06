package ru.komiss77.utils.inventory;

import java.util.function.Consumer;

public class InventoryListener<T>
{
    private final Class<T> type;
    private final Consumer<T> consumer;
    
    public InventoryListener(final Class<T> type, final Consumer<T> consumer) {
        this.type = type;
        this.consumer = consumer;
    }
    
    public void accept(final T t) {
        this.consumer.accept(t);
    }
    
    public Class<T> getType() {
        return this.type;
    }
}
