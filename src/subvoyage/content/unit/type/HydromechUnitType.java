package subvoyage.content.unit.type;

import arc.math.geom.*;
import mindustry.game.*;
import mindustry.gen.*;
import subvoyage.content.unit.entity.*;

import java.util.function.*;

public class HydromechUnitType extends AtlacianUnitType {
    public Consumer<HydromechUnitEntity> onDraw = (e) -> {
    };

    public HydromechUnitType(String name) {
        super(name);
        trailScl = 8;
        trailLength = 9;
        waveTrailX = 4f;
        waveTrailY = -3f;
    }

    @Override
    public Unit spawn(Position pos) {
        return super.spawn(pos);
    }

    @Override
    public void update(Unit unit) {
        super.update(unit);
    }

    @Override
    public void draw(Unit unit) {
        if(unit instanceof HydromechUnitEntity h) onDraw.accept(h);
        super.draw(unit);
    }

    @Override
    public Unit create(Team team) {
        Unit unit = constructor.get();
        unit.team = team;
        unit.setType(this);
        unit.ammo = ammoCapacity; //fill up on ammo upon creation
        unit.elevation = flying ? 1f : 0;
        unit.heal();
        if(unit instanceof TimedKillc u){
            u.lifetime(lifetime);
        }
        return unit;
    }

    @Override
    public void init() {
        super.init();
        naval = true;
    }

    @Override
    public Unit spawn(Position pos, Team team) {
        return super.spawn(pos, team);
    }
}
