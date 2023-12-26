package ru.komiss77.notes;

public @interface Slow {
    /**
     * @return насколько медленный метод / класс / обьект, 0 - быстрый
     */
	public abstract int priority() default 0;
}
