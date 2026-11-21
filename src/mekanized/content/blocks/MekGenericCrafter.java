package mekanized.content.blocks;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.Align;
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

    /** If true, first output liquid dumps on (rotation + 0) and second on (rotation + 2) like an Electrolyzer. */
    public boolean directionalLiquidOutput = false;

    /** Set to true if you want the default items bar hidden. */
    public boolean hideItemsBar = true;

    public static class CrafterRecipe {
        public String name;
        public float craftTime = 60f;
        public float heatRequirement = 0f;
        public ItemStack[] inputs = ItemStack.empty;
        public LiquidStack[] inputLiquids = LiquidStack.empty;
        public ItemStack[] outputs = ItemStack.empty;
        public LiquidStack[] outputLiquids = LiquidStack.empty;

        public CrafterRecipe(
                String name,
                float craftTime,
                float heatRequirement,
                ItemStack[] inputs,
                LiquidStack[] inputLiquids,
                ItemStack[] outputs,
                LiquidStack[] outputLiquids
        ) {
            this.name = name;
            this.craftTime = craftTime;
            this.heatRequirement = heatRequirement;
            this.inputs = inputs != null ? inputs : ItemStack.empty;
            this.inputLiquids = inputLiquids != null ? inputLiquids : LiquidStack.empty;
            this.outputs = outputs != null ? outputs : ItemStack.empty;
            this.outputLiquids = outputLiquids != null ? outputLiquids : LiquidStack.empty;
        }

        public float safeCraftTime() {
            return craftTime > 0.01f ? craftTime : 60f;
        }

        public Drawable iconDrawable() {
            if (outputs != null) {
                for (ItemStack stack : outputs) {
                    if (stack != null && stack.item != null && stack.amount > 0) {
                        return new TextureRegionDrawable(stack.item.uiIcon);
                    }
                }
            }
            if (outputLiquids != null) {
                for (LiquidStack stack : outputLiquids) {
                    if (stack != null && stack.liquid != null && stack.amount > 0f) {
                        return new TextureRegionDrawable(stack.liquid.uiIcon);
                    }
                }
            }
            return Icon.production;
        }

        public String displayName(int fallbackIndex) {
            if (name != null && !name.trim().isEmpty()) {
                return name.trim();
            }
            if (outputs != null) {
                for (ItemStack stack : outputs) {
                    if (stack != null && stack.item != null && stack.amount > 0) {
                        return stack.item.localizedName;
                    }
                }
            }
            if (outputLiquids != null) {
                for (LiquidStack stack : outputLiquids) {
                    if (stack != null && stack.liquid != null && stack.amount > 0f) {
                        return stack.liquid.localizedName;
                    }
                }
            }
            return "Recipe " + (fallbackIndex + 1);
        }

        public String summaryText(int fallbackIndex) {
            StringBuilder sb = new StringBuilder();
            sb.append(displayName(fallbackIndex));
            sb.append("\nTime: ").append(String.format(Locale.ROOT, "%.1fs", safeCraftTime() / 60f));
            if (heatRequirement > 0f) {
                sb.append(" | Heat: ").append(Math.round(heatRequirement));
            }
            if (inputs != null && inputs.length > 0) {
                sb.append("\nIn: ");
                for (int i = 0; i < inputs.length; i++) {
                    ItemStack s = inputs[i];
                    if (s == null || s.item == null || s.amount <= 0) continue;
                    if (i > 0) sb.append(", ");
                    sb.append(s.amount).append("x ").append(s.item.localizedName);
                }
            }
            if (inputLiquids != null && inputLiquids.length > 0) {
                sb.append("\nIn Liq: ");
                for (int i = 0; i < inputLiquids.length; i++) {
                    LiquidStack s = inputLiquids[i];
                    if (s == null || s.liquid == null || s.amount <= 0f) continue;
                    if (i > 0) sb.append(", ");
                    sb.append(String.format(Locale.ROOT, "%.1f/s ", s.amount * 60f)).append(s.liquid.localizedName);
                }
            }
            if (outputs != null && outputs.length > 0) {
                sb.append("\nOut: ");
                for (int i = 0; i < outputs.length; i++) {
                    ItemStack s = outputs[i];
                    if (s == null || s.item == null || s.amount <= 0) continue;
                    if (i > 0) sb.append(", ");
                    sb.append(s.amount).append("x ").append(s.item.localizedName);
                }
            }
            if (outputLiquids != null && outputLiquids.length > 0) {
                sb.append("\nOut Liq: ");
                for (int i = 0; i < outputLiquids.length; i++) {
                    LiquidStack s = outputLiquids[i];
                    if (s == null || s.liquid == null || s.amount <= 0f) continue;
                    if (i > 0) sb.append(", ");
                    sb.append(String.format(Locale.ROOT, "%.1f/s ", s.amount * 60f)).append(s.liquid.localizedName);
                }
            }
            return sb.toString();
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
            if (index == null || recipes == null || recipes.isEmpty()) {
                build.recipeIndex = 0;
                return;
            }
            int clamped = Mathf.clamp(index, 0, recipes.size - 1);
            if (build.recipeIndex != clamped) {
                build.recipeIndex = clamped;
                build.progress = 0f;
            }
        });
    }

    @Override
    public void init() {
        if (recipes != null) {
            for (CrafterRecipe recipe : recipes) {
                if (recipe == null) continue;
                if (recipe.inputs == null) recipe.inputs = ItemStack.empty;
                if (recipe.inputLiquids == null) recipe.inputLiquids = LiquidStack.empty;
                if (recipe.outputs == null) recipe.outputs = ItemStack.empty;
                if (recipe.outputLiquids == null) recipe.outputLiquids = LiquidStack.empty;

                if (recipe.inputLiquids.length > 0 || recipe.outputLiquids.length > 0) {
                    hasLiquids = true;
                }
            }
        }
        if (findConsumer(c -> c instanceof ConsumeLiquidBase) != null) {
            hasLiquids = true;
        }
        if (directionalLiquidOutput) {
            rotate = true;
        }
        super.init();
    }

    @Override
    public void setStats() {
        super.setStats();
        if (recipes == null || recipes.isEmpty()) return;

        stats.add(Stat.output, statTable -> {
            statTable.row();

            for (int i = 0; i < recipes.size; i++) {
                CrafterRecipe r = recipes.get(i);
                if (r == null) continue;

                final int idx = i;
                statTable.table(Tex.pane, card -> {
                    card.left().defaults().left().pad(2f);

                    // Header: Recipe name + craft time + optional heat
                    String header = r.displayName(idx) + "  (" + String.format(Locale.ROOT, "%.1fs", r.safeCraftTime() / 60f) + ")";
                    card.add(header).color(Pal.accent).padBottom(4f);
                    if (r.heatRequirement > 0f) {
                        card.add("  [" + Math.round(r.heatRequirement) + " Heat]").color(Pal.lightOrange).padBottom(4f);
                    }
                    card.row();

                    // Inputs (items + liquids)
                    boolean hasAnyIn = hasAnyItem(r.inputs) || hasAnyLiquid(r.inputLiquids);
                    if (hasAnyIn) {
                        card.table(row -> {
                            row.left().defaults().padRight(8f);
                            row.add("In:").color(Color.lightGray);
                            addItemStackIcons(row, r.inputs);
                            addLiquidRateIcons(row, r.inputLiquids);
                        }).row();
                    }

                    // Outputs (items + liquids)
                    boolean hasAnyOut = hasAnyItem(r.outputs) || hasAnyLiquid(r.outputLiquids);
                    if (hasAnyOut) {
                        card.table(row -> {
                            row.left().defaults().padRight(8f);
                            row.add("Out:").color(Color.lightGray);
                            addItemStackIcons(row, r.outputs);
                            addLiquidRateIcons(row, r.outputLiquids);
                        }).row();
                    }
                }).growX().pad(4f).row();
            }
        });
    }

    private boolean hasAnyItem(ItemStack[] stacks) {
        if (stacks == null) return false;
        for (ItemStack s : stacks) {
            if (s != null && s.item != null && s.amount > 0) return true;
        }
        return false;
    }

    private boolean hasAnyLiquid(LiquidStack[] stacks) {
        if (stacks == null) return false;
        for (LiquidStack s : stacks) {
            if (s != null && s.liquid != null && s.amount > 0f) return true;
        }
        return false;
    }

    private void addItemStackIcons(Table row, ItemStack[] stacks) {
        if (stacks == null) return;
        for (ItemStack s : stacks) {
            if (s == null || s.item == null || s.amount <= 0) continue;
            row.table(itemCell -> {
                itemCell.left();
                itemCell.stack(
                        new Image(s.item.uiIcon),
                        new Label(String.valueOf(s.amount)) {{
                            setFontScale(0.8f);
                            setAlignment(Align.bottomRight);
                        }}
                ).size(24f);
            }).padRight(6f);
        }
    }

    private void addLiquidRateIcons(Table row, LiquidStack[] stacks) {
        if (stacks == null) return;
        for (LiquidStack s : stacks) {
            if (s == null || s.liquid == null || s.amount <= 0f) continue;
            row.table(liq -> {
                liq.left();
                liq.image(s.liquid.uiIcon).size(20f).padRight(3f);
                liq.add(String.format(Locale.ROOT, "%.1f/s", s.amount * 60f)).color(Color.lightGray);
            }).padRight(6f);
        }
    }

    @Override
    public void setBars() {
        super.setBars();

        if (hideItemsBar) {
            removeBar("items");
        }

        addBar("progress", (MekGenericCrafterBuild entity) ->
                new Bar(
                        () -> Core.bundle.format("bar.progress", Math.round(entity.progressFraction() * 100f)),
                        () -> Pal.accent,
                        entity::progressFraction
                )
        );

        addBar("heat", (MekGenericCrafterBuild entity) -> {
            float req = entity.heatRequirement();
            return new Bar(
                    () -> {
                        int eff = Math.round(entity.heatScale() * 100f);
                        return "Heat: " + Math.round(entity.heat) + " (" + eff + "%)";
                    },
                    () -> (req <= 0f || entity.heat + 0.01f >= req) ? Pal.lightOrange : Pal.gray,
                    () -> {
                        if (req <= 0f && entity.heat <= 0.01f) return 0f;
                        return req > 0f ? Math.min(1f, entity.heat / req) : Math.min(1f, entity.heat / 10f);
                    }
            );
        });
    }

    public class MekGenericCrafterBuild extends Building implements HeatConsumer {
        public int recipeIndex = 0;
        public float progress = 0f;
        public float heat = 0f;
        public float[] sideHeat = new float[Math.max(1, size * 4)];

        public CrafterRecipe currentRecipe() {
            if (recipes == null || recipes.isEmpty()) return null;
            recipeIndex = Mathf.clamp(recipeIndex, 0, recipes.size - 1);
            return recipes.get(recipeIndex);
        }

        public float progressFraction() {
            CrafterRecipe r = currentRecipe();
            if (r == null) return 0f;
            float duration = r.safeCraftTime();
            return Mathf.clamp(progress / duration, 0f, 1f);
        }

        public float heatScale() {
            CrafterRecipe r = currentRecipe();
            if (r == null) return 0f;
            if (r.heatRequirement <= 0f) return 1f;
            if (heat + 0.01f < r.heatRequirement) return 0f;
            return Math.min(heat / r.heatRequirement, Math.max(1f, maxHeatEfficiency));
        }

        @Override
        public float[] sideHeat() {
            if (sideHeat == null || sideHeat.length != Math.max(1, size * 4)) {
                sideHeat = new float[Math.max(1, size * 4)];
            }
            return sideHeat;
        }

        @Override
        public float heatRequirement() {
            CrafterRecipe r = currentRecipe();
            return r == null ? 0f : Math.max(0f, r.heatRequirement);
        }

        @Override
        public Integer config() {
            if (recipes == null || recipes.isEmpty()) return 0;
            return Mathf.clamp(recipeIndex, 0, recipes.size - 1);
        }

        @Override
        public void buildConfiguration(Table table) {
            table.clear();
            table.setBackground(Tex.pane);

            if (recipes == null || recipes.isEmpty()) {
                table.add("No recipes").pad(6f);
                return;
            }

            ButtonGroup<ImageButton> group = new ButtonGroup<>();
            group.setMinCheckCount(0);

            int cols = Math.min(4, Math.max(1, recipes.size));
            int count = 0;

            for (int i = 0; i < recipes.size; i++) {
                final int idx = i;
                CrafterRecipe r = recipes.get(i);
                if (r == null) continue;

                ImageButton button = table.button(r.iconDrawable(), Styles.clearNoneTogglei, 36f, () -> {
                    configure(idx);
                    deselect();
                }).size(46f, 46f).pad(3f).group(group).get();

                button.setChecked(idx == recipeIndex);
                button.addListener(new Tooltip(t -> {
                    t.setBackground(Tex.pane);
                    t.add(r.summaryText(idx)).left().pad(4f);
                }));

                count++;
                if (count % cols == 0) {
                    table.row();
                }
            }
        }

        public boolean hasItemInputs() {
            CrafterRecipe r = currentRecipe();
            if (r == null) return false;
            if (r.inputs == null || r.inputs.length == 0) return true;
            if (items == null) return false;
            for (ItemStack stack : r.inputs) {
                if (stack == null || stack.item == null || stack.amount <= 0) continue;
                if (!items.has(stack.item, stack.amount)) return false;
            }
            return true;
        }

        public boolean hasLiquidInputs() {
            CrafterRecipe r = currentRecipe();
            if (r == null || r.inputLiquids == null || r.inputLiquids.length == 0) return true;
            if (!hasLiquids || liquids == null || liquidCapacity <= 0.001f) return false;

            float minNeeded = Math.max(0.01f, delta());
            for (LiquidStack stack : r.inputLiquids) {
                if (stack == null || stack.liquid == null || stack.amount <= 0f) continue;
                float required = Math.min(liquidCapacity * 0.5f, Math.max(stack.amount * minNeeded, stack.amount * 2f));
                if (liquids.get(stack.liquid) + 0.0001f < required) {
                    return false;
                }
            }
            return true;
        }

        public boolean canFitOutputs() {
            CrafterRecipe r = currentRecipe();
            if (r == null) return false;

            if (r.outputs != null && items != null) {
                for (ItemStack stack : r.outputs) {
                    if (stack == null || stack.item == null || stack.amount <= 0) continue;
                    if (items.get(stack.item) + stack.amount > itemCapacity) return false;
                }
            }
            if (r.outputLiquids != null && r.outputLiquids.length > 0) {
                if (!hasLiquids || liquids == null || liquidCapacity <= 0.001f) return false;
                for (LiquidStack stack : r.outputLiquids) {
                    if (stack == null || stack.liquid == null || stack.amount <= 0f) continue;
                    if (liquids.get(stack.liquid) + stack.amount * delta() > liquidCapacity) return false;
                }
            }
            return true;
        }

        @Override
        public boolean shouldConsume() {
            if (currentRecipe() == null) return false;
            if (heatRequirement() > 0f && heat + 0.01f < heatRequirement()) return false;
            return canFitOutputs() && hasItemInputs() && hasLiquidInputs();
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            if (item == null || items == null) return false;
            CrafterRecipe r = currentRecipe();
            if (r == null || r.inputs == null) return false;
            for (ItemStack stack : r.inputs) {
                if (stack != null && stack.item == item && stack.amount > 0 && items.get(item) < itemCapacity) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            if (liquid == null || !hasLiquids || liquids == null || liquidCapacity <= 0.001f) return false;
            if (liquids.get(liquid) >= liquidCapacity - 0.0001f) return false;

            CrafterRecipe r = currentRecipe();
            if (r != null && r.outputLiquids != null) {
                for (LiquidStack stack : r.outputLiquids) {
                    if (stack != null && stack.liquid == liquid) return false;
                }
            }
            if (r != null && r.inputLiquids != null) {
                for (LiquidStack stack : r.inputLiquids) {
                    if (stack != null && stack.liquid == liquid && stack.amount > 0f) return true;
                }
            }
            return findConsumer(c -> c instanceof ConsumeLiquid && ((ConsumeLiquid) c).liquid == liquid) != null
                    || findConsumer(c -> c instanceof ConsumeLiquids && ((ConsumeLiquids) c).liquids != null
                    && Arrays.stream(((ConsumeLiquids) c).liquids).anyMatch(ls -> ls != null && ls.liquid == liquid)) != null;
        }

        @Override
        public void updateTile() {
            super.updateTile();

            float[] sides = sideHeat();
            float targetHeat = calculateHeat(sides);
            if (Math.abs(targetHeat - heat) < 0.01f) {
                heat = targetHeat;
            } else {
                heat = Mathf.lerpDelta(heat, targetHeat, 0.1f);
            }

            CrafterRecipe r = currentRecipe();
            float hScale = heatScale();

            if (r != null && efficiency > 0f && hScale > 0f && shouldConsume()) {
                float step = edelta() * hScale;

                if (r.inputLiquids != null && r.inputLiquids.length > 0 && liquids != null) {
                    for (LiquidStack stack : r.inputLiquids) {
                        if (stack == null || stack.liquid == null || stack.amount <= 0f) continue;
                        float drain = Math.min(liquids.get(stack.liquid), stack.amount * step);
                        if (drain > 0f) {
                            liquids.remove(stack.liquid, drain);
                        }
                    }
                }

                if (r.outputLiquids != null && r.outputLiquids.length > 0 && liquids != null) {
                    for (LiquidStack stack : r.outputLiquids) {
                        if (stack == null || stack.liquid == null || stack.amount <= 0f) continue;
                        float add = Math.min(stack.amount * step, liquidCapacity - liquids.get(stack.liquid));
                        if (add > 0f) {
                            liquids.add(stack.liquid, add);
                        }
                    }
                }

                progress += step;
                float duration = r.safeCraftTime();
                if (progress >= duration) {
                    craft();
                    progress %= duration;
                }
            } else {
                progress = Math.max(0f, progress - delta() * 0.5f);
            }

            dumpCraftOutputs();
        }

        public void craft() {
            CrafterRecipe r = currentRecipe();
            if (r == null) return;

            if (r.inputs != null && items != null) {
                for (ItemStack stack : r.inputs) {
                    if (stack == null || stack.item == null || stack.amount <= 0) continue;
                    if (items.has(stack.item, stack.amount)) {
                        items.remove(stack.item, stack.amount);
                    }
                }
            }
            if (r.outputs != null) {
                for (ItemStack stack : r.outputs) {
                    if (stack == null || stack.item == null || stack.amount <= 0) continue;
                    for (int i = 0; i < stack.amount; i++) {
                        offload(stack.item);
                    }
                }
            }
        }

        public void dumpCraftOutputs() {
            CrafterRecipe r = currentRecipe();
            if (r == null) return;

            if (r.outputs != null && items != null) {
                for (ItemStack stack : r.outputs) {
                    if (stack != null && stack.item != null && items.get(stack.item) > 0) {
                        dump(stack.item);
                    }
                }
            }
            if (hasLiquids && liquids != null && r.outputLiquids != null) {
                for (int i = 0; i < r.outputLiquids.length; i++) {
                    LiquidStack stack = r.outputLiquids[i];
                    if (stack == null || stack.liquid == null || liquids.get(stack.liquid) <= 0.001f) continue;

                    if (directionalLiquidOutput) {
                        // First output liquid goes to `rotation`, second goes to `rotation + 2` (opposite side)
                        int dir = (rotation + (i * 2)) % 4;
                        dumpLiquidToSide(stack.liquid, dir);
                    } else {
                        dumpLiquid(stack.liquid);
                    }
                }
            }
        }

        public void dumpLiquidToSide(Liquid liquid, int dir) {
            if (liquids == null || liquids.get(liquid) <= 0.001f) return;
            Point2 offset = Geometry.d4(dir);
            Building next = nearby(offset.x, offset.y);
            if (next != null && next.acceptLiquid(this, liquid)) {
                float avail = liquids.get(liquid);
                float move = Math.min(avail, liquidCapacity * 0.25f);
                if (move > 0.001f) {
                    next.handleLiquid(this, liquid, move);
                    liquids.remove(liquid, move);
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
            progress = Math.max(0f, read.f());
            heat = Math.max(0f, read.f());
            if (recipes != null && !recipes.isEmpty()) {
                recipeIndex = Mathf.clamp(recipeIndex, 0, recipes.size - 1);
            } else {
                recipeIndex = 0;
            }
        }
    }
}