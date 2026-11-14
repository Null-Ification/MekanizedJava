package mekanized;

import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.ConsumeItemFlammable;
import mindustry.world.blocks.distribution.Duct;
import mindustry.world.blocks.distribution.DuctBridge;
import mindustry.world.blocks.distribution.DuctJunction;
import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawGlowRegion;
import mindustry.world.draw.DrawMulti;

import arc.graphics.Color;

import static mindustry.type.ItemStack.with;

public class MekBlocks{
    public static Block ironSmelter;
    public static Duct ironDuct;
    public static Block ironBridge, ironJunction;

    public static void load(){
        ironSmelter = new GenericCrafter("iron-smelter"){{
            requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));

            size = 2;
            craftTime = 60f;
            outputItem = new ItemStack(MekItems.iron, 1);

            consumeItem(Items.copper, 1); // the placeholder ingredient
            consume(new ConsumeItemFlammable()); // accepts any burnable item

            drawer = new DrawMulti(new DrawDefault(), new DrawGlowRegion(){{
                color = Color.valueOf("ffa300");
            }});
            craftEffect = Fx.formsmoke;
            updateEffect = Fx.plasticburn;
            updateEffectChance = 0.05f;
        }};

        ironDuct = new Duct("iron-duct") {{
            requirements(Category.distribution, with(MekItems.iron, 1));
            health = 90;
            speed = 6f;
        }};

        ironJunction = new DuctJunction("iron-junction") {{
            requirements(Category.distribution, with(MekItems.iron, 2));
            health = 120;
            speed = 6f;
        }};
        ironDuct.junctionReplacement = ironJunction;

        ironBridge = new DuctBridge("iron-bridge") {{
            requirements(Category.distribution, with(MekItems.iron, 4));
            health = 180;
            speed = 6f;
        }};
        ironDuct.bridgeReplacement = ironBridge;
    }
}