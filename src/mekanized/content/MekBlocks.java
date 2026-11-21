package mekanized.content;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.gen.Building;
import mindustry.type.Category;
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
    public static Block infuser, thermalVaporizer, smallTurbine;
    public static Duct ironDuct;
    public static DuctBridge ironBridge;
    public static DuctJunction ironJunction;

    public static void load() {
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