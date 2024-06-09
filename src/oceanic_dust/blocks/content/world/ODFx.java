package oceanic_dust.blocks.content.world;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.entities.*;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;

public class ODFx{
    public static final Rand rand = new Rand();
    public static final Vec2 v = new Vec2();

    public static final Effect

    none = new Effect(0, 0f, e -> {
    }),

    pulverize = new Effect(20, e -> {
        color(e.color.a(0.75f));
        stroke(e.fout() * 2f);
        Lines.circle(e.x, e.y, e.finpow() * (e.lifetime / 5));

        randLenVectors(e.id + 1, 4, 18 * e.finpow(), (x, y) ->
        lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 1f + e.fout() * 3f));
    }),

    smokePuff = new Effect(18, e -> {
        color(e.color.a(0.2f));
        randLenVectors(e.id, 6, 30f * e.finpow(), (x, y) -> {
            Fill.circle(e.x + x, e.y + y, e.fout() * 2f);
            Fill.circle(e.x + x / 2f, e.y + y / 2f, e.fout());
        });
    });
}
