package mekanized.content;

import arc.graphics.Color;
import mindustry.type.Liquid;

public class MekLiquids {
    public static Liquid steam;

    public static void load(){
        steam = new Liquid("steam", Color.valueOf("dceeff")) {{
            gas = true;

            temperature = 1f;
            viscosity = 0.1f;
        }};
    }
}
