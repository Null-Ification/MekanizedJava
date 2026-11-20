package mekanized.content.blocks;

import arc.graphics.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import java.util.*;

public class MekGenericCrafter extends Block {
    public Seq<CrafterRecipe> recipes = new Seq<>();

    /** Optional cap on heat-based speed scaling (1.0 = no overdrive speed bonus, 2.0 = up to 2x speed). */
    public float maxHeatEfficiency = 1.0f;

    public static class CrafterRecipe {
        public String name;
        public float craftTime = 60f;
        public float heatRequirement = 0f;
        public ItemStack[] inputs = ItemStack.empty;
        public ItemStack[] outputs = ItemStack.empty;
        public LiquidStack[] outputLiquids = LiquidStack.empty;

        public CrafterRecipe(String name, float craftTime, ItemStack[] inputs, ItemStack[] outputs) {
            this(name, craftTime, 0f, inputs, outputs, LiquidStack.empty);
        }

        public CrafterRecipe(String name, float craftTime, ItemStack[] inputs, ItemStack[] outputs, LiquidStack[] outputLiquids) {
            this(name, craftTime, 0f, inputs, outputs, outputLiquids);
        }

        public CrafterRecipe(String name, float craftTime, float heatRequirement, ItemStack[] inputs, ItemStack[] outputs, LiquidStack[] outputLiquids) {
            this.name = name;
            this.craftTime = craftTime;
            this.heatRequirement = heatRequirement;
            this.inputs = inputs;
            this.outputs = outputs;
            this.outputLiquids = outputLiquids;
        }
    }

    public MekGenericCrafter(String name) {
        super(name);
        update = true;
        hasItems = true;
        sync = true;
        configurable = true;
        saveConfig = true;
        buildType = MekGenericCrafterBuild::new;

        config(Integer.class, (MekGenericCrafterBuild build, Integer index) -> {
            if (recipes.isEmpty()) {
                build.recipeIndex = 0;
                return;
            }
            int clamped = Math.max(0, Math.min(index, recipes.size - 1));
            if (build.recipeIndex != clamped) {
                build.recipeIndex = clamped;
                build.progress = 0f;
            }
        });
    }

    @Override
    public void init() {
        for (CrafterRecipe recipe : recipes) {
            if (recipe.outputLiquids != null && recipe.outputLiquids.length > 0) {
                hasLiquids = true;
                break;
            }
        }
        if (findConsumer(c -> c instanceof ConsumeLiquidBase) != null) {
            hasLiquids = true;
        }
        super.init();
    }

    @Override
    public void setStats() {
        super.setStats();
        if (!recipes.isEmpty()) {
            stats.add(Stat.productionTime, recipes.first().craftTime / 60f, StatUnit.seconds);
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("progress", (MekGenericCrafterBuild entity) ->
                new Bar("bar.progress", Pal.ammo, entity::progressFraction)
        );
        addBar("heat", (MekGenericCrafterBuild entity) ->
                new Bar(
                        () -> entity.heatRequirement() > 0f
                                ? "Heat (" + Math.round(entity.heat) + " / " + Math.round(entity.heatRequirement()) + ")"
                                : "Heat (" + Math.round(entity.heat) + ")",
                        () -> entity.heatRequirement() <= 0f || entity.heat + 0.01f >= entity.heatRequirement()
                                ? Pal.lightOrange
                                : Pal.gray,
                        () -> entity.heatRequirement() > 0f
                                ? Math.min(1f, entity.heat / entity.heatRequirement())
                                : Math.min(1f, entity.heat / 10f)
                )
        );
    }

    public class MekGenericCrafterBuild extends Building implements HeatConsumer {
        public int recipeIndex = 0;
        public float progress = 0f;
        public float heat = 0f;
        public float[] sideHeat = new float[size * 4];

        public CrafterRecipe currentRecipe() {
            if (recipes.isEmpty()) return null;
            if (recipeIndex < 0 || recipeIndex >= recipes.size) {
                recipeIndex = 0;
            }
            return recipes.get(recipeIndex);
        }

        public float progressFraction() {
            CrafterRecipe r = currentRecipe();
            if (r == null || r.craftTime <= 0f) return 0f;
            return progress / r.craftTime;
        }

        public float heatScale() {
            CrafterRecipe r = currentRecipe();
            if (r == null || r.heatRequirement <= 0f) return 1f;
            if (heat + 0.01f < r.heatRequirement) return 0f;
            return Math.min(heat / r.heatRequirement, Math.max(1f, maxHeatEfficiency));
        }

        @Override
        public float[] sideHeat() {
            return sideHeat;
        }

        @Override
        public float heatRequirement() {
            CrafterRecipe r = currentRecipe();
            return r == null ? 0f : r.heatRequirement;
        }

        @Override
        public Integer config() {
            return recipeIndex;
        }

        @Override
        public void buildConfiguration(Table table) {
            table.setBackground(Tex.pane);
            if (recipes.isEmpty()) {
                table.add("No recipes").pad(6f);
                return;
            }

            for (int i = 0; i < recipes.size; i++) {
                final int idx = i;
                CrafterRecipe r = recipes.get(i);
                String label = (r.name != null && !r.name.isEmpty()) ? r.name : ("Recipe " + (i + 1));

                table.button(label, () -> configure(idx))
                        .size(120f, 36f)
                        .pad(3f);

                if ((i + 1) % 2 == 0) {
                    table.row();
                }
            }
        }

        public boolean hasItemInputs() {
            CrafterRecipe r = currentRecipe();
            if (r == null) return false;
            for (ItemStack stack : r.inputs) {
                if (!items.has(stack.item, stack.amount)) return false;
            }
            return true;
        }

        public boolean canFitOutputs() {
            CrafterRecipe r = currentRecipe();
            if (r == null) return false;

            for (ItemStack stack : r.outputs) {
                if (items.get(stack.item) + stack.amount > itemCapacity) return false;
            }
            if (liquids != null && r.outputLiquids != null) {
                for (LiquidStack stack : r.outputLiquids) {
                    if (liquids.get(stack.liquid) + stack.amount * delta() > liquidCapacity) return false;
                }
            }
            return true;
        }

        @Override
        public boolean shouldConsume() {
            if (currentRecipe() == null) return false;
            if (heatRequirement() > 0f && heat + 0.01f < heatRequirement()) return false;
            return canFitOutputs() && hasItemInputs();
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            CrafterRecipe r = currentRecipe();
            if (r == null) return false;
            for (ItemStack stack : r.inputs) {
                if (stack.item == item && items.get(item) < itemCapacity) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            if (!hasLiquids || liquids == null) return false;
            CrafterRecipe r = currentRecipe();
            if (r != null && r.outputLiquids != null) {
                for (LiquidStack stack : r.outputLiquids) {
                    if (stack.liquid == liquid) return false;
                }
            }
            return findConsumer(c -> c instanceof ConsumeLiquid && ((ConsumeLiquid) c).liquid == liquid) != null
                    || findConsumer(c -> c instanceof ConsumeLiquids && Arrays.stream(((ConsumeLiquids) c).liquids).anyMatch(ls -> ls.liquid == liquid)) != null;
        }

        @Override
        public void updateTile() {
            super.updateTile();

            float targetHeat = calculateHeat(sideHeat);
            if (Math.abs(targetHeat - heat) < 0.01f) {
                heat = targetHeat;
            } else {
                heat = Mathf.lerpDelta(heat, targetHeat, 0.1f);
            }

            CrafterRecipe r = currentRecipe();
            float hScale = heatScale();

            if (r != null && efficiency > 0f && hScale > 0f && shouldConsume()) {
                float step = edelta() * hScale;

                if (r.outputLiquids != null && r.outputLiquids.length > 0 && liquids != null) {
                    for (LiquidStack stack : r.outputLiquids) {
                        float add = Math.min(stack.amount * step, liquidCapacity - liquids.get(stack.liquid));
                        if (add > 0f) {
                            liquids.add(stack.liquid, add);
                        }
                    }
                }

                progress += step;
                if (progress >= r.craftTime) {
                    craft();
                    progress %= r.craftTime;
                }
            } else {
                progress = Math.max(0f, progress - delta() * 0.5f);
            }

            dumpCraftOutputs();
        }

        public void craft() {
            CrafterRecipe r = currentRecipe();
            if (r == null) return;

            for (ItemStack stack : r.inputs) {
                items.remove(stack.item, stack.amount);
            }
            for (ItemStack stack : r.outputs) {
                for (int i = 0; i < stack.amount; i++) {
                    offload(stack.item);
                }
            }
        }

        public void dumpCraftOutputs() {
            CrafterRecipe r = currentRecipe();
            if (r == null) return;

            for (ItemStack stack : r.outputs) {
                if (items.get(stack.item) > 0) {
                    dump(stack.item);
                }
            }
            if (liquids != null && r.outputLiquids != null) {
                for (LiquidStack stack : r.outputLiquids) {
                    if (liquids.get(stack.liquid) > 0.001f) {
                        dumpLiquid(stack.liquid);
                    }
                }
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(recipeIndex);
            write.f(progress);
            write.f(heat);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            recipeIndex = read.i();
            progress = read.f();
            heat = read.f();
        }
    }
}