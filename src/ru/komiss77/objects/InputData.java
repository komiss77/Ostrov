package ru.komiss77.objects;

import java.util.function.Consumer;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.inventory.InputButton;


public class InputData {
    
        public final InputButton.InputType type;
        public final Consumer<String> consumer;
        public final XYZ xyz;
        private String result;
        
        public InputData (final InputButton.InputType type, final  Consumer<String> consumer, final XYZ xyz) {
            this.type = type;
            this.consumer = consumer;
            this.xyz = xyz;
        }
        
    public void setResult(final String msg) {
        result = msg.replaceAll("&k", "").replace('&', '§');
    }

    public String getResult() {
        return result;
    }

    public void accept() {
        if (consumer != null && result != null) {
            consumer.accept(result);
        }
    }
}
