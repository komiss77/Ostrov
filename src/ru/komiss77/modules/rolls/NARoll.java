package ru.komiss77.modules.rolls;

import ru.komiss77.utils.ClassUtil;

public class NARoll extends Roll<Object> {

    private static final String NA_ID = "na";

    public NARoll() {
        super(NA_ID, null, 0, 0);
    }

    public static NARoll get(final String id) {
        return ClassUtil.cast(Roll.get(id), NARoll.class);
    }

    protected Object asAmount(final int amt) {return null;}

    protected String encode() {return "";}

    public void save() {}

    public void delete() {}
}
