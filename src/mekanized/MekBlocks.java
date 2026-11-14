package mekanized;

import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.gen.Building;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.ConsumeItemFlammable;
import mindustry.world.blocks.distribution.Duct;
import mindustry.world.blocks.distribution.DuctBridge;
import mindustry.world.blocks.distribution.DuctJunction;
import mindustry.world.draw.*;
import arc.graphics.Color;

import static mindustry.type.ItemStack.with;

public class MekBlocks{
    public static Block ironSmelter, thermalVaporizer;
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
                color = Color.valueOf("e68569");
            }});
            craftEffect = Fx.pulverize;
            updateEffect = Fx.coalSmeltsmoke;
            updateEffectChance = 0.05f;
        }};

        thermalVaporizer = new GenericCrafter("thermal-vaporizer"){{
            requirements(Category.crafting, with(Items.copper, 35, MekItems.iron, 75));

            size = 3;
            craftTime = 60f;
            liquidCapacity = 120f;

            consume(new ConsumeItemFlammable());
            consumeLiquid(Liquids.water, 1f).boost();

            outputLiquid = new LiquidStack(Liquids.nitrogen, 1f);

            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(){{
                drawLiquid = Liquids.water;
                padding = 5.5f;
            }}, new DrawBubbles(Color.valueOf("7693e3")){{
                sides = 10;
                recurrence = 3f;
                spread = 4.5f;
                radius = 1.5f;
                amount = 12;
            }
                @Override
                public void draw(Building build){
                    if (build.optionalEfficiency > 0f) {
                        super.draw(build);
                    }
                }
            }, new DrawLiquidTile(){{
                drawLiquid = Liquids.nitrogen;
                padding = 5.5f;
            }}, new DrawDefault(), new DrawWarmupRegion());

            buildType = () -> new GenericCrafterBuild(){
                @Override
                public void updateTile(){
                    super.updateTile();
                    if (efficiency > 0 && liquids.get(Liquids.water) > 0.01f){
                        handleLiquid(this, Liquids.nitrogen, 1f * edelta() * optionalEfficiency);
                    }
                }
            };
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