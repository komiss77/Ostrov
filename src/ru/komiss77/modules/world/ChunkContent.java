package ru.komiss77.modules.world;

public class ChunkContent {


    protected boolean empty() {
        return true;
    }

    //координата В ЧАНКЕ запакованная в int, высота -128..256
    public static int sLoc(final int x, final int y, final int z) {
        return ((y + 128) << 16) | (x & 0xF) << 8 | (z & 0xF);
    }

}
