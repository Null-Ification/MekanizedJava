package mekanized.content;

import arc.graphics.Color;
import mekanized.content.blocks.MekGenericCrafter;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.gen.Building;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.distribution.Duct;
import mindustry.world.blocks.distribution.DuctBridge;
import mindustry.world.blocks.distribution.DuctJunction;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.ConsumeItemFlammable;
import mindustry.world.draw.*;

import static mindustry.type.ItemStack.with;

public class MekBlocks {
    public static Block ironSmelter, infuser, thermalVaporizer, smallTurbine;
    public static Duct ironDuct;
    public static DuctBridge ironBridge;
    public static DuctJunction ironJunction;

    public static void load() {
        ironSmelter = new MekGenericCrafter("iron-smelter") {{
            //TODO: Add a custom Crafter for this.
            requirements(Category.crafting, with(MekItems.stone, 50));

            size = 2;
            health = 100;
            buildTime = 60f;

            recipes.addAll(
                    new CrafterRecipe(
                            "Iron",
                            60f,
                            ItemStack.with(MekItems.rawIron, 1, Items.coal, 1),
                            ItemStack.with(MekItems.iron, 1)
                    ),
                    new CrafterRecipe(
                            "Copper",
                            60f,
                            ItemStack.with(MekItems.rawCopper, 1, Items.coal, 1),
                            ItemStack.with(Items.copper, 1)
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

        thermalVaporizer = new GenericCrafter("thermal-vaporizer") {{
            //TODO: Down there + resprite.
            requirements(Category.crafting, with(Items.copper, 35, MekItems.iron, 75));

            size = 3;
            craftTime = 60f;
            liquidCapacity = 120f;

            consume(new ConsumeItemFlammable());
            consumeLiquid(Liquids.water, 1f).boost();

            outputLiquid = new LiquidStack(MekLiquids.steam, 1f);

            updateEffect = Fx.fuelburn;
            updateEffectChance = 0.05f;

            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile() {{
                drawLiquid = Liquids.water;
                padding = 5.5f;
            }}, new DrawBubbles(Color.valueOf("7693e3")) {
                {
                    sides = 10;
                    recurrence = 3f;
                    spread = 4.5f;
                    radius = 1.5f;
                    amount = 12;
                }

                @Override
                public void draw(Building build) {
                    if (build.optionalEfficiency > 0f) {
                        super.draw(build);
                    }
                }
            }, new DrawLiquidTile() {{
                drawLiquid = MekLiquids.steam;
                padding = 5.5f;
            }}, new DrawDefault(), new DrawWarmupRegion());

            buildType = () -> new GenericCrafterBuild() {
                // TODO: Either somehow integrate this back to outPutLiquid, or custom Crafter.
                @Override
                public void updateTile() {
                    super.updateTile();
                    if (efficiency > 0 && liquids.get(Liquids.water) > 0.01f) {
                        handleLiquid(this, MekLiquids.steam, 1f * edelta() * optionalEfficiency);
                    }
                }
                // handleLiquid? isn't this forcefully adding steam on top of outputLiquid?
                // Heard that this may break the game.
            };
        }};

        smallTurbine = new ConsumeGenerator("small-turbine") {{
            //TODO: Resprite and rebalance.
            requirements(Category.power, with(MekItems.iron, 25, Items.copper, 10));

            size = 2;
            liquidCapacity = 120f;
            hasLiquids = true;
            consumeLiquid(MekLiquids.steam, 1f);

            powerProduction = 1f;

            generateEffect = Fx.heatReactorSmoke;
            generateEffectRange = 0.1f;
            effectChance = 0.07f;

            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile() {{
                drawLiquid = MekLiquids.steam;
                padding = 1f;
            }}, new DrawRegion("-turbine") {{
                rotateSpeed = 6f;
            }}, new DrawRegion("-turbine") {{
                rotateSpeed = -6f;
                rotation = 45f;
            }}, new DrawDefault(), new DrawRegion("-cap"));
        }};

        infuser = new MekGenericCrafter("infuser") {{
            //TODO: Resprite and rebalance. Also, add a custom Crafter for this, because it needs to have a custom recipe.
            requirements(Category.crafting, with(MekItems.iron, 50, Items.silicon, 25));

            size = 3;
            liquidCapacity = 120f;

            recipes.addAll(
                    new CrafterRecipe(
                            "Enriched Iron",
                            120f,
                            ItemStack.with(MekItems.iron, 1, Items.coal, 1),
                            ItemStack.with(MekItems.enrichedIron, 1),
                            LiquidStack.with(Liquids.oil, 1f)
                    ),
                    new CrafterRecipe(
                            "Arc Smelt",
                            40f,
                            10f, // requires 10 heat
                            ItemStack.with(MekItems.iron, 1),
                            ItemStack.with(Items.surgeAlloy, 1),
                            LiquidStack.empty
                    )
            ); // also need custom recipe, but how?

            consumeLiquid(MekLiquids.steam, 1f);
            consumePower(1.5f);
            /*
             * iron + coal(turned into carbon or whatever) = enriched iron;
             * enriched iron -> smelter up there to make steel.
             * Osmium- Wait wrong Crafter block. should be in processor manufacturing block thingmasilly.
             */
        }};

        //TODO: Resprite. Add more distribution: sorter, flow gates, unloader, ect.
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