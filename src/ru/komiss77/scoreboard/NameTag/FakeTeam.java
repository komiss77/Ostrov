package ru.komiss77.scoreboard.NameTag;

import java.util.ArrayList;

public class FakeTeam {
    
    private static final String UNIQUE_ID;
    private static int ID;
    private final ArrayList<String> members;
    private String name;
    private String prefix;
    private String suffix;
    
    public FakeTeam(final String prefix, final String suffix, final int sortPriority, final boolean playerTag) {
        this.members = new ArrayList<>();
        this.prefix = "";
        this.suffix = "";
        this.name = FakeTeam.UNIQUE_ID + "_" + this.getNameFromInput(sortPriority) + ++FakeTeam.ID + (playerTag ? "+P" : "");
        
        //if (VersionChecker.getBukkitVersion() == VersionChecker.BukkitVersion.v1_13_R1) {
        //    this.name = ((this.name.length() > 128) ? this.name.substring(0, 128) : this.name);
        //}
      //  else if (VersionChecker.getBukkitVersion() == VersionChecker.BukkitVersion.v1_14_R1) {
            this.name = ((this.name.length() > 128) ? this.name.substring(0, 128) : this.name);
      //  }
      //  else {
       //     this.name = ((this.name.length() > 16) ? this.name.substring(0, 16) : this.name);
       // }
        this.prefix = prefix;
        this.suffix = suffix;
    }
    
    public void addMember(final String player) {
        if (!this.members.contains(player)) {
            this.members.add(player);
        }
    }
    
    public boolean isSimilar(final String prefix, final String suffix) {
        return this.prefix.equals(prefix) && this.suffix.equals(suffix);
    }
    
    private String getNameFromInput(final int input) {
        if (input < 0) {
            return "Z";
        }
        final char letter = (char)(input / 5 + 65);
        final int repeat = input % 5 + 1;
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < repeat; ++i) {
            builder.append(letter);
        }
        return builder.toString();
    }
    
    public ArrayList<String> getMembers() {
        return this.members;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getPrefix() {
        return this.prefix;
    }
    
    public String getSuffix() {
        return this.suffix;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }
    
    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof FakeTeam)) {
            return false;
        }
        final FakeTeam other = (FakeTeam)o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$members = this.getMembers();
        final Object other$members = other.getMembers();
        Label_0065: {
            if (this$members == null) {
                if (other$members == null) {
                    break Label_0065;
                }
            }
            else if (this$members.equals(other$members)) {
                break Label_0065;
            }
            return false;
        }
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        Label_0102: {
            if (this$name == null) {
                if (other$name == null) {
                    break Label_0102;
                }
            }
            else if (this$name.equals(other$name)) {
                break Label_0102;
            }
            return false;
        }
        final Object this$prefix = this.getPrefix();
        final Object other$prefix = other.getPrefix();
        Label_0139: {
            if (this$prefix == null) {
                if (other$prefix == null) {
                    break Label_0139;
                }
            }
            else if (this$prefix.equals(other$prefix)) {
                break Label_0139;
            }
            return false;
        }
        final Object this$suffix = this.getSuffix();
        final Object other$suffix = other.getSuffix();
        if (this$suffix == null) {
            if (other$suffix == null) {
                return true;
            }
        }
        else if (this$suffix.equals(other$suffix)) {
            return true;
        }
        return false;
    }
    
    protected boolean canEqual(final Object other) {
        return other instanceof FakeTeam;
    }
    
    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $members = this.getMembers();
        result = result * 59 + (($members == null) ? 0 : $members.hashCode());
        final Object $name = this.getName();
        result = result * 59 + (($name == null) ? 0 : $name.hashCode());
        final Object $prefix = this.getPrefix();
        result = result * 59 + (($prefix == null) ? 0 : $prefix.hashCode());
        final Object $suffix = this.getSuffix();
        result = result * 59 + (($suffix == null) ? 0 : $suffix.hashCode());
        return result;
    }
    
    @Override
    public String toString() {
        return "FakeTeam(members=" + this.getMembers() + ", name=" + this.getName() + ", prefix=" + this.getPrefix() + ", suffix=" + this.getSuffix() + ")";
    }
    
    static {
        UNIQUE_ID = generateUUID();
        FakeTeam.ID = 0;
    }


    
    public static String generateUUID() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            builder.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return builder.toString();
    }
}
