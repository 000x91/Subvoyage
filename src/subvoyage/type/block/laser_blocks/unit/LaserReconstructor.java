package subvoyage.type.block.laser_blocks.unit;

import arc.Core;
import arc.Events;
import arc.Graphics;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Scaling;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.ai.UnitCommand;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.blocks.units.Reconstructor;
import mindustry.world.blocks.units.UnitBlock;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import subvoyage.SubvoyageMod;
import subvoyage.type.block.laser.LaserModule;

import static mindustry.Vars.state;

public class LaserReconstructor extends LaserUnitBlock {
    public float constructTime = 60 * 2;
    public Seq<UnitType[]> upgrades = new Seq<>();
    public int[] capacities = {};
    public LaserReconstructor(String name) {
        super(name);
        regionRotated1 = 1;
        regionRotated2 = 2;
        commandable = true;
        ambientSound = Sounds.respawning;
        configurable = true;
        config(UnitCommand.class, (Reconstructor.ReconstructorBuild build, UnitCommand command) -> build.command = command);
        configClear((Reconstructor.ReconstructorBuild build) -> build.command = null);
    }
    @Override
    public void load(){
        super.load();
        topRegion = Core.atlas.find(name + "-top", SubvoyageMod.ID + "-factory-top-" + size + regionSuffix);
        outRegion = Core.atlas.find(name + "-out", SubvoyageMod.ID + "-factory-out-" + size + regionSuffix);
        inRegion = Core.atlas.find(name + "-in", SubvoyageMod.ID + "-factory-in-" + size + regionSuffix);
    }
    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(region, plan.drawx(), plan.drawy());
        Draw.rect(inRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.rect(outRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.rect(topRegion, plan.drawx(), plan.drawy());
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region, inRegion, outRegion, topRegion};
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("progress", (ReconstructorBuild entity) -> new Bar("bar.progress", Pal.ammo, entity::fraction));
        addBar("units", (ReconstructorBuild e) ->
                new Bar(
                        () -> e.unit() == null ? "[lightgray]" + Iconc.cancel :
                                Core.bundle.format("bar.unitcap",
                                        Fonts.getUnicodeStr(e.unit().name),
                                        e.team.data().countType(e.unit()),
                                        Units.getStringCap(e.team)
                                ),
                        () -> Pal.power,
                        () -> e.unit() == null ? 0f : (float)e.team.data().countType(e.unit()) / Units.getCap(e.team)
                ));
    }

    @Override
    public void setStats(){
        stats.timePeriod = constructTime;
        super.setStats();

        stats.add(Stat.productionTime, constructTime / 60f, StatUnit.seconds);
        stats.add(Stat.output, table -> {
            table.row();
            for(var upgrade : upgrades){
                if(upgrade[0].unlockedNow() && upgrade[1].unlockedNow()){
                    table.table(Styles.grayPanel, t -> {
                        t.left();

                        t.image(upgrade[0].uiIcon).size(40).pad(10f).left().scaling(Scaling.fit);
                        t.table(info -> {
                            info.add(upgrade[0].localizedName).left();
                            info.row();
                        }).pad(10).left();
                    }).fill().padTop(5).padBottom(5);

                    table.table(Styles.grayPanel, t -> {

                        t.image(Icon.right).color(Pal.darkishGray).size(40).pad(10f);
                    }).fill().padTop(5).padBottom(5);

                    table.table(Styles.grayPanel, t -> {
                        t.left();

                        t.image(upgrade[1].uiIcon).size(40).pad(10f).right().scaling(Scaling.fit);
                        t.table(info -> {
                            info.add(upgrade[1].localizedName).right();
                            info.row();
                        }).pad(10).right();
                    }).fill().padTop(5).padBottom(5);

                    table.row();
                }
            }
        });
    }

    @Override
    public void init(){
        capacities = new int[Vars.content.items().size];

        ConsumeItems cons = findConsumer(c -> c instanceof ConsumeItems);
        if(cons != null){
            for(ItemStack stack : cons.items){
                capacities[stack.item.id] = Math.max(capacities[stack.item.id], stack.amount * 2);
                itemCapacity = Math.max(itemCapacity, stack.amount * 2);
            }
        }

        consumeBuilder.each(c -> c.multiplier = b -> state.rules.unitCost(b.team));

        super.init();
    }

    public void addUpgrade(UnitType from, UnitType to){
        upgrades.add(new UnitType[]{from, to});
    }

    public class ReconstructorBuild extends LaserUnitBuild {
        public @Nullable Vec2 commandPos;
        public @Nullable UnitCommand command;
        @Override
        public void created() {
            super.created();
        }

        public float fraction(){
            return progress / constructTime;
        }

        @Override
        public boolean shouldActiveSound(){
            return shouldConsume();
        }

        @Override
        public Vec2 getCommandPosition(){
            return commandPos;
        }

        @Override
        public void onCommand(Vec2 target){
            commandPos = target;
        }

        @Override
        public boolean acceptUnitPayload(Unit unit){
            return hasUpgrade(unit.type) && !upgrade(unit.type).isBanned();
        }

        public boolean canSetCommand(){
            var output = unit();
            return output != null && output.commands.length > 1;
        }

        @Override
        public Graphics.Cursor getCursor(){
            return canSetCommand() ? super.getCursor() : Graphics.Cursor.SystemCursor.arrow;
        }

        @Override
        public boolean shouldShowConfigure(Player player){
            return canSetCommand();
        }

        @Override
        public void buildConfiguration(Table table){
            var unit = unit();

            if(unit == null){
                deselect();
                return;
            }

            var group = new ButtonGroup<ImageButton>();
            group.setMinCheckCount(0);
            int i = 0, columns = 4;

            table.background(Styles.black6);

            var list = unit().commands;
            for(var item : list){
                ImageButton button = table.button(item.getIcon(), Styles.clearNoneTogglei, 40f, () -> {
                    configure(item);
                    deselect();
                }).tooltip(item.localized()).group(group).get();

                button.update(() -> button.setChecked(command == item || (command == null && unit.defaultCommand == item)));

                if(++i % columns == 0){
                    table.row();
                }
            }
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload){
            if(!(this.payload == null
                    && (this.enabled || source == this)
                    && relativeTo(source) != rotation
                    && payload instanceof UnitPayload pay)){
                return false;
            }

            var upgrade = upgrade(pay.unit.type);

            if(upgrade != null){
                if(!upgrade.unlockedNowHost() && !team.isAI()){
                    //flash "not researched"
                    pay.showOverlay(Icon.tree);
                }

                if(upgrade.isBanned()){
                    //flash an X, meaning 'banned'
                    pay.showOverlay(Icon.cancel);
                }
            }

            return upgrade != null && (team.isAI() || upgrade.unlockedNowHost()) && !upgrade.isBanned();
        }

        @Override
        public int getMaximumAccepted(Item item){
            return Mathf.round(capacities[item.id] * state.rules.unitCost(team));
        }

        @Override
        public void overwrote(Seq<Building> builds){
            if(builds.first().block == block){
                items.add(builds.first().items);
            }
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            //draw input
            boolean fallback = true;
            for(int i = 0; i < 4; i++){
                if(blends(i) && i != rotation){
                    Draw.rect(inRegion, x, y, (i * 90) - 180);
                    fallback = false;
                }
            }
            if(fallback) Draw.rect(inRegion, x, y, rotation * 90);

            Draw.rect(outRegion, x, y, rotdeg());

            if(constructing() && hasArrived()){
                Draw.draw(Layer.blockOver, () -> {
                    Draw.alpha(1f - progress/ constructTime);
                    Draw.rect(payload.unit.type.fullIcon, x, y, payload.rotation() - 90);
                    Draw.reset();
                    Drawf.construct(this, upgrade(payload.unit.type), payload.rotation() - 90f, progress / constructTime, speedScl, time);
                });
            }else{
                Draw.z(Layer.blockOver);

                drawPayload();
            }

            Draw.z(Layer.blockOver + 0.1f);
            Draw.rect(topRegion, x, y);
        }

        @Override
        public Object senseObject(LAccess sensor){
            if(sensor == LAccess.config) return unit();
            return super.senseObject(sensor);
        }

        @Override
        public void updateTile(){
            if(lasers == null) return;
            lasers.update(this);
            if(lasers.power() >= maxLaserPower) {
                Fx.hitMeltdown.create(x,y,0, Pal.accent,new Object());
                damage(10);
            }
            lasers.graph.consumers.each(consumer -> {
                if(consumer instanceof LaserBlockBuilding lb) {
                    lb.lasers.setPower(lasers.power());
                }
            });
            System.out.println("---");
            System.out.println(lasers.smoothEfficiency);
            System.out.println(lasers.power());
            System.out.println(this.efficiency);
            System.out.println(rawLaserEfficiency());
            boolean valid = false;

            if(payload != null){
                //check if offloading
                if(!hasUpgrade(payload.unit.type)){
                    moveOutPayload();
                }else{ //update progress
                    if(moveInPayload()){
                        if(efficiency > 0){
                            valid = true;
                            progress += efficiency() * delta() * state.rules.unitBuildSpeed(team);
                        }

                        //upgrade the unit
                        if(progress >= constructTime){
                            payload.unit = upgrade(payload.unit.type).create(payload.unit.team());

                            if(payload.unit.isCommandable()){
                                if(commandPos != null){
                                    payload.unit.command().commandPosition(commandPos);
                                }
                                if(command != null){
                                    //this already checks if it is a valid command for the unit type
                                    payload.unit.command().command(command);
                                }
                            }

                            progress %= 1f;
                            Effect.shake(2f, 3f, this);
                            Fx.producesmoke.at(this);
                            consume();
                            Events.fire(new EventType.UnitCreateEvent(payload.unit, this));
                        }
                    }
                }
            }

            speedScl = Mathf.lerpDelta(speedScl, Mathf.num(valid), 0.05f);
            time += efficiency() * delta() * speedScl * state.rules.unitBuildSpeed(team);
        }

        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.progress) return Mathf.clamp(fraction());
            if(sensor == LAccess.itemCapacity) return Mathf.round(itemCapacity * state.rules.unitCost(team));
            return super.sense(sensor);
        }

        @Override
        public boolean shouldConsume(){
            return constructing() && enabled;
        }

        @Override
        public Object config(){
            return command;
        }

        public UnitType unit(){
            if(payload == null) return null;

            UnitType t = upgrade(payload.unit.type);
            return t != null && (t.unlockedNowHost() || team.isAI()) ? t : null;
        }

        public boolean constructing(){
            return payload != null && hasUpgrade(payload.unit.type);
        }

        public boolean hasUpgrade(UnitType type){
            UnitType t = upgrade(type);
            return t != null && (t.unlockedNowHost() || team.isAI()) && !type.isBanned();
        }

        public UnitType upgrade(UnitType type){
            UnitType[] r =  upgrades.find(u -> u[0] == type);
            return r == null ? null : r[1];
        }

        @Override
        public byte version(){
            return 3;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(progress);
            TypeIO.writeVecNullable(write, commandPos);
            TypeIO.writeCommand(write, command);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 1){
                progress = read.f();
            }

            if(revision >= 2){
                commandPos = TypeIO.readVecNullable(read);
            }

            if(revision >= 3){
                command = TypeIO.readCommand(read);
            }
        }

    }
}
