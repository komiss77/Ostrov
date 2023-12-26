package ru.komiss77.utils;

import org.bukkit.util.Vector;

import ru.komiss77.notes.Slow;

public class FastMath {
	
    private static final float R_TO_D = 57.3f;
    private static final double PIx2 = Math.PI * 2;

    public static float toDegree(float angle) {
        return angle * R_TO_D;
    }    
    
    public static byte toPackedByte(float f) {
        return (byte) (f * 0.71f);
    }
    
    public static int square(final int num) {
        return num * num;
    }

	public static int delimit(final int i) {
		return i >> 31 | 1;
	}

	public static int absInt(int i) {
		return (i + (i >>= 31)) ^ i;
	}

	public static long absLong(long i) {
		return (i + (i >>= 63)) ^ i;
	}
	
	//Integer.signum
	/*public static int signOf(int i) {
		return (i >> 31) | 1;
	}

	public static int signOf(long i) {
		return (int) ((i >> 63) | 1);
	}*/
	
	public static int sqrtAprx(final int of) {
		return 512 / (-of - 32) + 16;//max sqrt - 16
	}
	
	@Slow(priority = 1)
	public static float getYaw(final Vector vc) {
		return toDegree((float) ((Math.atan2(-vc.getX(), vc.getZ()) + PIx2) % PIx2));
	}
}
