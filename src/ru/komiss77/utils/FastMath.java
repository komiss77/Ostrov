package ru.komiss77.utils;

import org.bukkit.util.Vector;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.notes.Slow;

@Deprecated
public class FastMath {

    private static final float R_TO_D = 57.3f;
    private static final double PIx2 = Math.PI * 2;

    public static float toDegree(final float angle) {
        return angle * R_TO_D;
    }

    public static byte pack(final float f) {
        return (byte) (f * 0.71f);
    }

    public static int square(final int num) {
        return num * num;
    }

    public static int delimit(final int i) {
        return i >> 31 | 1;
    }

    @Deprecated
    public static int absInt(int i) {
        return (i + (i >>= 31)) ^ i;
    }

    public static int abs(int i) {
        return (i + (i >>= 31)) ^ i;
    }

    @Deprecated
    public static long absLong(long i) {
        return (i + (i >>= 63)) ^ i;
    }

    public static long abs(long i) {
        return (i + (i >>= 63)) ^ i;
    }

    public static int absDec(final int a, final int b) {
        return abs(a) < abs(b) ? b : a;
    }

    public static float absDec(final float a, final float b) {
        return Math.abs(a) < Math.abs(b) ? b : a;
    }

    public static double absDec(final double a, final double b) {
        return Math.abs(a) < Math.abs(b) ? b : a;
    }

    public static double mulDiff(final int a, final int b) {
        return a > b ? a / b : b / a;
    }

    public static double mulDiff(final float a, final float b) {
        return a > b ? a / b : b / a;
    }

    public static double mulDiff(final double a, final double b) {
        return a > b ? a / b : b / a;
    }

    //Integer.signum
	/*public static int signOf(int i) {
		return (i >> 31) | 1;
	}

	public static int signOf(long i) {
		return (int) ((i >> 63) | 1);
	}*/

    /**
     * Faster replacements for (int)(java.lang.Math.sqrt(integer))
     */
    final static int[] table = {
        0, 16, 22, 27, 32, 35, 39, 42, 45, 48, 50, 53, 55, 57,
        59, 61, 64, 65, 67, 69, 71, 73, 75, 76, 78, 80, 81, 83,
        84, 86, 87, 89, 90, 91, 93, 94, 96, 97, 98, 99, 101, 102,
        103, 104, 106, 107, 108, 109, 110, 112, 113, 114, 115, 116, 117, 118,
        119, 120, 121, 122, 123, 124, 125, 126, 128, 128, 129, 130, 131, 132,
        133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 144, 145,
        146, 147, 148, 149, 150, 150, 151, 152, 153, 154, 155, 155, 156, 157,
        158, 159, 160, 160, 161, 162, 163, 163, 164, 165, 166, 167, 167, 168,
        169, 170, 170, 171, 172, 173, 173, 174, 175, 176, 176, 177, 178, 178,
        179, 180, 181, 181, 182, 183, 183, 184, 185, 185, 186, 187, 187, 188,
        189, 189, 190, 191, 192, 192, 193, 193, 194, 195, 195, 196, 197, 197,
        198, 199, 199, 200, 201, 201, 202, 203, 203, 204, 204, 205, 206, 206,
        207, 208, 208, 209, 209, 210, 211, 211, 212, 212, 213, 214, 214, 215,
        215, 216, 217, 217, 218, 218, 219, 219, 220, 221, 221, 222, 222, 223,
        224, 224, 225, 225, 226, 226, 227, 227, 228, 229, 229, 230, 230, 231,
        231, 232, 232, 233, 234, 234, 235, 235, 236, 236, 237, 237, 238, 238,
        239, 240, 240, 241, 241, 242, 242, 243, 243, 244, 244, 245, 245, 246,
        246, 247, 247, 248, 248, 249, 249, 250, 250, 251, 251, 252, 252, 253,
        253, 254, 254, 255
    };

    /**
     * A faster replacement for (int)(java.lang.Math.sqrt(x)).  Completely accurate for x < 2147483648 (i.e. 2^31)...
     */
    public static int sqrt(int x) {
        if (x >= 0x10000) {
            if (x >= 0x1000000) {
                if (x >= 0x10000000) {
                    if (x >= 0x40000000) {
                        return (table[x >> 24] << 8);
                    }
                    return (table[x >> 22] << 7);
                }
                if (x >= 0x4000000) {
                    return (table[x >> 20] << 6);
                }
                return (table[x >> 18] << 5);
            }
            if (x >= 0x100000) {
                if (x >= 0x400000) {
                    return (table[x >> 16] << 4);
                }
                return (table[x >> 14] << 3);
            }
            if (x >= 0x40000) {
                return (table[x >> 12] << 2);
            }
            return (table[x >> 10] << 1);
        }
        if (x >= 0x100) {
            if (x >= 0x1000) {
                if (x >= 0x4000) {
                    return (table[x >> 8]);
                }
                return (table[x >> 6] >> 1);
            }
            if (x >= 0x400) {
                return (table[x >> 4] >> 2);
            }
            return (table[x >> 2] >> 3);
        }
        if (x >= 0) {
            return table[x] >> 4;
        }
        return -1;
    }

//	public static int sqrtAprx(final int of) {
//		return 512 / (-of - 32) + 16;//max sqrt - 16
//	}

    @Slow(priority = 1)
    public static float getYaw(final Vector vc) {
        return toDegree((float) ((Math.atan2(-vc.getX(), vc.getZ()) + PIx2) % PIx2));
    }

    public static XYZ rndCircPos(final XYZ pos, final int dst) {
        final float aa = Ostrov.random.nextFloat() - 0.5f,
            bb = Ostrov.random.nextFloat() - 0.5f, a2 = aa * aa, b2 = bb * bb;
        return pos.clone().add((int) (dst * (a2 - b2) / (a2 + b2)),
            0, (int) ((dst << 1) * aa * bb / (a2 + b2)));
    }

    public static Vector rndCircPos(final Vector loc, final int dst) {
        final double aa = Ostrov.random.nextDouble() - 0.5d,
            bb = Ostrov.random.nextDouble() - 0.5d, a2 = aa * aa, b2 = bb * bb;
        return new Vector(loc.getX() + dst * (a2 - b2) / (a2 + b2),
            loc.getY(), loc.getZ() + (dst << 1) * aa * bb / (a2 + b2));
    }

    @Slow(priority = 1)
    public static Vector getShotVec(final Vector dst, final double spd) {
        final double DlnSq = dst.lengthSquared() * 0.01d / spd;
        if (dst.getY() > -DlnSq) dst.setY((dst.getY() + 1d) * DlnSq);
        return dst.normalize().multiply(spd);
    }


}
