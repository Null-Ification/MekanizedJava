package mekanized.content;

import arc.graphics.Color;
import mindustry.type.Item;

public class MekItems {
    public static Item rawIron, iron, enrichedIron,
            rawCopper,
            stone;

    public static void load(){
        rawIron = new Item("raw-iron", Color.valueOf("9b705b")) {{
            hardness = 1;
            cost = 0.5f;
        }};

        rawCopper = new Item("raw-copper", Color.valueOf("a66d45")) {{
            hardness = 1;
            cost = 0.6f;
        }};

        iron = new Item("iron", Color.valueOf("b5b5b5")) {{
            hardness = 1;
            cost = 0.8f;
        }};

        enrichedIron = new Item("enriched-iron", Color.valueOf("e6e6e6")) {{
            hardness = 2;
            cost = 1.5f;
        }};

        stone = new Item("stone", Color.valueOf("7d7c70")) {{
            hardness = 1;
            cost = 0.5f;
        }};
    }
}
