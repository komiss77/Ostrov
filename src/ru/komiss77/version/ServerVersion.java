package ru.komiss77.version;


public enum ServerVersion {

    //v1_14_R1("1.14.4"),
    //v1_15_R1("1.15.1"),
    //v1_15_R2("1.15.2"),
   // v1_16_R1("1.16.1"),
    //v1_16_R2("1.16.2"),
    v1_16_R3("1.16.3"),
    v1_17_R1("1.17"),
    
    ;

    String name;

    ServerVersion(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
