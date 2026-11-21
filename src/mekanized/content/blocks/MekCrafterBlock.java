package mekanized.content.blocks;

import mekanized.content.MekItems;
import mekanized.content.MekLiquids;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;

import static mindustry.type.ItemStack.with;

public class MekCrafterBlock {
    public static Block smelter, infuser;

    public static void load() {
        smelter = new MekGenericCrafter("smelter") {{
            requirements(Category.crafting, with(MekItems.stone, 50));

            size = 2;
            health = 100;
            buildTime = 60f;

            recipes.addAll(
                    new CrafterRecipe(
                            "Iron",
                            60f,
                            0f,
                            ItemStack.with(MekItems.rawIron, 1, Items.coal, 1),
                            null,
                            ItemStack.with(MekItems.iron, 1),
                            null
                    ),
                    new CrafterRecipe(
                            "Copper",
                            60f,
                            0f,
                            ItemStack.with(MekItems.rawCopper, 1, Items.coal, 1),
                            null,
                            ItemStack.with(Items.copper, 1),
                            null
                    )
            );

//            drawer = new DrawMulti(
//                    new DrawDefault(),
//                    new DrawGlowRegion() {{
//                        color = Color.valueOf("e68569");
//                    }}
//            );
//
//            craftEffect = Fx.pulverize;
//            updateEffect = Fx.coalSmeltsmoke;
//            updateEffectChance = 0.05f;
        }};

        infuser = new MekGenericCrafter("infuser") {{
            //TODO: Resprite and rebalance.
            requirements(Category.crafting, with(MekItems.iron, 50, Items.silicon, 25));

            size = 3;
            liquidCapacity = 120f;
            maxHeatEfficiency = 2.0f;

            recipes.addAll(
                    new CrafterRecipe(
                            "Enriched Iron",
                            120f,
                            0f,
                            ItemStack.with(MekItems.iron, 1, Items.coal, 1),
                            LiquidStack.empty,
                            ItemStack.with(MekItems.enrichedIron, 1),
                            LiquidStack.with(Liquids.oil, 10 / 60f)
                    ),
                    new CrafterRecipe(
                            "Testing",
                            40f,
                            10f,
                            ItemStack.with(Items.silicon, 2),
                            LiquidStack.with(Liquids.slag, 40 / 60f),
                            ItemStack.with(Items.surgeAlloy, 1),
                            LiquidStack.empty
                    )
            );

            consumeLiquid(Liquids.arkycite, 5 / 60f);
            consumePower(1f);
            /*
             * iron + coal(turned into carbon or whatever) = enriched iron;
             * enriched iron -> smelter up there to make steel.
             * Osmium- Wait wrong Crafter block. should be in processor manufacturing block thingmasilly.
             */
        }};
    }
}
