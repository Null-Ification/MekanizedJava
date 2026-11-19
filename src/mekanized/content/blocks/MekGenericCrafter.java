package mekanized.content.blocks;

import arc.struct.EnumSet;
import arc.struct.Seq;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BlockFlag;

public class MekGenericCrafter extends Block {

    public Seq<MekCustomRecipe> recipes = new Seq<>();

    public float craftTime = 60f;
    public Effect craftEffect = Fx.none;
    public DrawBlock drawer = new DrawDefault();

    public MekGenericCrafter(String name) {
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        sync = true;
        flags = EnumSet.of(BlockFlag.factory);
    }

    @Override
    public void init() {
        for (MekCustomRecipe recipe : recipes) {
            if (recipe.inputs == null && recipe.input != null) {
                recipe.inputs = new ItemStack[]{recipe.input};
            }

            if (recipe.outputs == null && recipe.output != null) {
                recipe.outputs = new ItemStack[]{recipe.output};
            }
        }

        super.init();
        buildType = MekGenericCrafterBuilding::new;
    }

    public class MekGenericCrafterBuilding extends Building {

        public float progress;
        public MekCustomRecipe currentRecipe;

        @Override
        public boolean acceptItem(Building source, Item item) {
            if (items.get(item) >= itemCapacity) return false;

            for (MekCustomRecipe recipe : recipes) {
                for (ItemStack input : recipe.inputs) {
                    if (item == input.item) {
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public void handleItem(Building source, Item item) {
            items.add(item, 1);
        }

        public MekCustomRecipe findRecipe() {
            for (MekCustomRecipe recipe : recipes) {
                boolean valid = true;

                for (ItemStack input : recipe.inputs) {
                    if (items.get(input.item) < input.amount) {
                        valid = false;
                        break;
                    }
                }

                if (valid) {
                    return recipe;
                }
            }

            return null;
        }

        @Override
        public void updateTile() {
            MekCustomRecipe recipe = findRecipe();

            if (recipe != currentRecipe) {
                progress = 0f;
                currentRecipe = recipe;
            }

            if (recipe != null) {
                progress += edelta();

                if (progress >= craftTime) {
                    progress %= craftTime;

                    for (ItemStack input : recipe.inputs) {
                        items.remove(input.item, input.amount);
                    }

                    for (ItemStack output : recipe.outputs) {
                        items.add(output.item, output.amount);
                    }

                    craftEffect.at(x, y);
                }

                for (ItemStack output : recipe.outputs) {
                    dump(output.item);
                }

            } else {
                progress = 0f;
            }
        }
    }
}
