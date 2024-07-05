package subvoyage.content.liquids;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.type.*;

public class SvLiquids{
    public static Liquid
            //liquid
            polygen,
            //gas
            argon, propane, nitrogen, oxygen
    ;

    public static void load() {

        polygen = new Liquid("meta-water", Color.valueOf("8997FF")) {{
            heatCapacity = 0.35f;
            boilPoint = 0.5f;
            gasColor = Color.grays(0.9f);
            flammability = 0f;
            coolant = false;
            moveThroughBlocks = true;
        }};

        argon = new Liquid("argon",Color.valueOf("FF8C99")) {{
            gas = true;
            flammability = 1f;
        }};

        propane = new Liquid("propane",Color.valueOf("FFB03A")) {{
            gas = true;
            flammability = 1f;
            explosiveness = 0.6f;
            blockReactive = true;
            temperature = 0.8f;
            gasColor = color;
        }};

        nitrogen = Liquids.nitrogen;
    }

}
