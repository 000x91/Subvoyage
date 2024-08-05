package subvoyage.draw.visual;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.gl.*;
import arc.util.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;
import static mindustry.graphics.Shaders.getShaderFi;


public class SvShaders{
    public static SurfaceShader hardWater;
    public static CacheLayer.ShaderLayer hardWaterLayer;

    public static Fi file(String name){
        return tree.get("shaders/" + name);
    }

    public static void init(){
        hardWater = new SurfaceShader("hard-water");
        CacheLayer.addLast(
                hardWaterLayer = new CacheLayer.ShaderLayer(hardWater)
        );
    }

    public static void dispose(){
        if(!headless){
            hardWater.dispose();
        }
    }

    public static class SurfaceShader extends Shader{
        Texture noiseTex;

        public SurfaceShader(String frag){
            super(getShaderFi("screenspace.vert"), file(frag + ".frag"));
            loadNoise();
        }

        public SurfaceShader(String vertRaw, String fragRaw){
            super(vertRaw, fragRaw);
            loadNoise();
        }

        public String textureName(){
            return "noise";
        }

        public void loadNoise(){
            Core.assets.load("sprites/" + textureName() + ".png", Texture.class).loaded = t -> {
                t.setFilter(TextureFilter.linear);
                t.setWrap(TextureWrap.repeat);
            };
        }

        @Override
        public void apply(){
            setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
            setUniformf("u_resolution", Core.camera.width, Core.camera.height);
            setUniformf("u_time", Time.time);

            if(hasUniform("u_noise")){
                if(noiseTex == null){
                    noiseTex = Core.assets.get("sprites/" + textureName() + ".png", Texture.class);
                }

                noiseTex.bind(1);
                renderer.effectBuffer.getTexture().bind(0);

                setUniformi("u_noise", 1);
            }
        }
    }
}
