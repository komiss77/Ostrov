package ru.komiss77.objects;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;


public class FigureAnswer {
    
    
    private final List<String> lines = new ArrayList<>();
//    public int second = 5;
    public Player player;
    public Figure figure;
    public boolean vibration;
    public boolean beforeEyes;
    public Sound sound = null;
//    public Map <String, Consumer<Player>> clickLines;
    
    public FigureAnswer () {
        
    }
    
    public FigureAnswer set(final List<String> lines) {
        this.lines.clear();
        return add(lines);
    }
    
    public FigureAnswer add(final List<String> lines) {
        this.lines.addAll(lines);
        return this;
    }
    
    
    
    
    
    public FigureAnswer add(final String line) {
        lines.add(line);
        return this;
    }
    
    public FigureAnswer add(final Material mat) {
        lines.add("ITEM:"+mat.name());
        return this;
    }
    
    /*public FigureAnswer add(final String line, final Consumer<Player> consumer) {
        lines.add(line);
        if (clickLines==null) {
            clickLines = new HashMap<>();
        }
        clickLines.put(line, consumer);
        return this;
    }
    
    public FigureAnswer add(final Material mat, final Consumer<Player> consumer) {
        lines.add("ITEM:"+mat.name());
        if (clickLines==null) {
            clickLines = new HashMap<>();
        }
        clickLines.put("ITEM:"+mat.name(), consumer);
        return this;
    }*/
    
    
    
    
    
    
    /*public FigureAnswer time(final int second) {
        this.second = second;
        return this;
    }*/

    public FigureAnswer vibration(final boolean vibration) {
        this.vibration = vibration;
        return this;
    }
    
    public FigureAnswer beforeEyes(final boolean beforeEyes) {
        this.beforeEyes = beforeEyes;
        return this;
    }
    
    public FigureAnswer sound(final Sound sound) {
        this.sound = sound;
        return this;
    }
    
    
    
    
    
    
    
    
    public List<String> getLines() {
        return lines;
    }

	public int duration() {
		int n = 0;
		for (final String ln : lines) n += ln.length();
		return n;
	}

   
}
