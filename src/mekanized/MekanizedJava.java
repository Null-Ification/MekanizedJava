package mekanized;

import mekanized.content.MekItems;
import mekanized.content.MekLiquids;
import mekanized.content.MekBlocks;
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