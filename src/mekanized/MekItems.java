package mekanized;

import arc.graphics.Color;
import mindustry.type.Item;

public class MekItems {
    public static Item iron;

    public static void load(){
        iron = new Item("iron", Color.valueOf("b5b5b5")) {{
            hardness = 1;
            cost = 0.8f;
        }};
    }
}
