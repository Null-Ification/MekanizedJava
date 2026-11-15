package mekanized;

import mindustry.mod.*;

public class MekanizedJava extends Mod{

    public MekanizedJava(){
    }

    @Override
    public void loadContent(){
        MekItems.load();
        MekLiquids.load();
        MekBlocks.load();
    }
}
