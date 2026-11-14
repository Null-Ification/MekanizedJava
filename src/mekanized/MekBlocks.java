package mekanized;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.ConsumeItemFlammable;

import static mindustry.type.ItemStack.with;

public class MekBlocks{
    public static Block ironSmelter;

    public static void load(){
        ironSmelter = new GenericCrafter("iron-smelter"){{
            requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));

            size = 2;
            craftTime = 60f;
            outputItem = new ItemStack(MekItems.iron, 1);

            consumeItem(Items.copper, 1); // the placeholder ingredient
            consume(new ConsumeItemFlammable()); // accepts any burnable item
        }};
    }
}