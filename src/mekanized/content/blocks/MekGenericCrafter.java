package mekanized.content.blocks;

import mindustry.world.blocks.production.GenericCrafter;

public class MekGenericCrafter extends GenericCrafter {
    public float itemConsumptionMult = 1f;
    public float liquidConsumptionMult = 1f;

    public float itemOutputMult = 1f;
    public float liquidOutputMult = 1f;

    public float craftSpeedMult = 1f;

    public MekGenericCrafter(String name) {
        super(name);

        buildType = MekGenericCrafterBuild::new;
    }

    public class MekGenericCrafterBuild extends GenericCrafterBuild {
    }
}