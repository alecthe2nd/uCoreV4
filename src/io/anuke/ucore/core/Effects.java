package io.anuke.ucore.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.anuke.ucore.entities.impl.EffectEntity;
import io.anuke.ucore.entities.trait.PosTrait;
import io.anuke.ucore.entities.trait.ScaleTrait;
import io.anuke.ucore.function.Consumer;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Interp;
import io.anuke.ucore.util.Mathf;
import io.anuke.ucore.util.Pooling;

public class Effects{
    private static final EffectContainer container = new EffectContainer();
    private static Array<Effect> effects = new Array<>();
    private static ScreenshakeProvider shakeProvider;
    private static float shakeFalloff = 1000f;
    private static EffectProvider provider = (effect, color, x, y, rotation, data) -> {
        EffectEntity entity = Pooling.obtain(EffectEntity.class, EffectEntity::new);
        entity.effect = effect;
        entity.color = color;
        entity.rotation = rotation;
        entity.data = data;
        entity.set(x, y);
        entity.add();
    };

    public static void setEffectProvider(EffectProvider prov){
        provider = prov;
    }

    public static void setScreenShakeProvider(ScreenshakeProvider provider){
        shakeProvider = provider;
    }

    public static void renderEffect(int id, Effect render, Color color, float life, float rotation, float x, float y, Object data){
        container.set(id, color, life, render.lifetime, rotation, x, y, data);
        render.draw.render(container);
    }

    public static Effect getEffect(int id){
        if(id >= effects.size || id < 0)
            throw new IllegalArgumentException("The effect with ID \"" + id + "\" does not exist!");
        return effects.get(id);
    }

    public static Array<Effect> all(){
        return effects;
    }

    public static void effect(Effect effect, float x, float y, float rotation){
        provider.createEffect(effect, Color.WHITE, x, y, rotation, null);
    }

    public static void effect(Effect effect, float x, float y){
        effect(effect, x, y, 0);
    }

    public static void effect(Effect effect, Color color, float x, float y){
        provider.createEffect(effect, color, x, y, 0f, null);
    }

    public static void effect(Effect effect, PosTrait loc){
        provider.createEffect(effect, Color.WHITE, loc.getX(), loc.getY(), 0f, null);
    }

    public static void effect(Effect effect, Color color, float x, float y, float rotation){
        provider.createEffect(effect, color, x, y, rotation, null);
    }

    public static void effect(Effect effect, Color color, float x, float y, float rotation, Object data){
        provider.createEffect(effect, color, x, y, rotation, data);
    }

    public static void effect(Effect effect, float x, float y, float rotation, Object data){
        provider.createEffect(effect, Color.WHITE, x, y, rotation, data);
    }

    public static void sound(String name){
        Sounds.play(name);
    }

    /** Plays a sound, with distance and falloff calulated relative to camera position. */
    public static void sound(String name, PosTrait position){
        sound(name, position.getX(), position.getY());
    }

    /** Plays a sound, with distance and falloff calulated relative to camera position. */
    public static void sound(String name, float x, float y){
        Sounds.playDistance(name, Vector2.dst(Core.camera.position.x, Core.camera.position.y, x, y));
    }

    /** Default value is 1000. Higher numbers mean more powerful shake (less falloff). */
    public static void setShakeFalloff(float falloff){
        shakeFalloff = falloff;
    }

    private static void shake(float intensity, float duration){
        if(shakeProvider == null)
            throw new RuntimeException("Screenshake provider is null! Set it first.");

        shakeProvider.accept(intensity, duration);
    }

    public static void shake(float intensity, float duration, float x, float y){
        float distance = Core.camera.position.dst(x, y, 0);
        if(distance < 1) distance = 1;

        shake(Mathf.clamp(1f / (distance * distance / shakeFalloff)) * intensity, duration);
    }

    public static void shake(float intensity, float duration, PosTrait loc){
        shake(intensity, duration, loc.getX(), loc.getY());
    }

    public interface ScreenshakeProvider{
        void accept(float intensity, float duration);
    }

    public static class Effect{
        private static int lastid = 0;
        public final int id;
        public final EffectRenderer draw;
        public final float lifetime;
        /** Clip size. */
        public float size;
        /** Whether this effect emits light. */
        public boolean emitLight = false;
        /** Light radius in world units. */
        public float lightRadius = 0f;
        /** Light opacity. */
        public float lightOpacity = 0.5f;
        /** Light color. */
        public Color lightColor = Color.WHITE;

        public Effect(float life, float clipsize, EffectRenderer draw){
            this.id = lastid++;
            this.lifetime = life;
            this.draw = draw;
            this.size = clipsize;
            effects.add(this);
        }

        public Effect(float life, EffectRenderer draw){
            this(life, 28f, draw);
        }
    }

    public static class EffectContainer implements ScaleTrait{

        //the aspects of a particle that can be manipulated
        public float x, y, rotation, size = 8;
        public Color color = new Color();

        //the aspects of a particle not manipulated
        public float time, lifetime;
        public int id;
        public Object data;
        private EffectContainer innerContainer;

        public void set(int id, Color color, float life, float lifetime, float rotation, float x, float y, Object data){
            this.x = x;
            this.y = y;
            this.color.set(color);
            this.time = life;
            this.lifetime = lifetime;
            this.id = id;
            this.rotation = rotation;
            this.data = data;
            this.size = 8;
        }

        public void set(EffectContainer other){
            this.x = other.x;
            this.y = other.y;
            this.color.set(other.color);
            this.time = other.time;
            this.lifetime = other.lifetime;
            this.id = other.id;
            this.rotation = other.rotation;
            this.data = other.data;
            this.size = other.size;
        }

        public void scaled(float lifetime, Consumer<EffectContainer> cons){
            if(innerContainer == null) innerContainer = new EffectContainer();
            if(time <= lifetime){
                innerContainer.set(id, color, time, lifetime, rotation, x, y, data);
                cons.accept(innerContainer);
            }
        }

        @Override
        public float fin(){
            return time / lifetime;
        }
    }

    public static interface EffectProvider{
        void createEffect(Effect effect, Color color, float x, float y, float rotation, Object data);
    }

    public static interface EffectRenderer{
        void render(EffectContainer effect);
    }

    public static class EffectParticle implements EffectRenderer{

        public static EffectContainer particleContainer = new EffectContainer();

        public String regionName = "circle";

        public TextureRegion cachedRegion = null;

        public int count = 0;

        public Array<ParticleProgressApplier> progresses = new Array<>();

        public void render(EffectContainer effect){
            //set if unset
            if(cachedRegion == null){
                cachedRegion = Draw.region(regionName);
            }
            //couldn't find AND no error? try for error
            if(cachedRegion == null){
                cachedRegion = Draw.region("error");
            }
            //Trade Offer: doesn't crash <-> doesn't draw anything
            if(cachedRegion == null)return;


            for(int i = 0; i < count; i++){

                particleContainer.set(effect);

                for(ParticleProgressApplier applier: progresses){
                    applier.apply(particleContainer);
                }

                Draw.color(effect.color);
                Draw.rect(cachedRegion, particleContainer.x, particleContainer.y, particleContainer.size, particleContainer.size, particleContainer.rotation);
                Draw.reset();
            }
        }

        public EffectParticle withCount(int count){
            this.count = count;
            return this;
        }

        public EffectParticle withRegion(String regionName){
            this.regionName = regionName;
            return this;
        }

        public EffectParticle withRegion(TextureRegion region){
            this.cachedRegion = region;
            return this;
        }

        public EffectParticle withProgress(ParticleProgressApplier progress){
            this.progresses.add(progress);
            return this;
        }

    }

    public interface ParticleProgressType{

        ParticleProgressType X = (e, n)->e.x +=n;

        ParticleProgressType Y = (e, n)->e.y += n;

        ParticleProgressType ROTATION = (e, n)->e.rotation += n;

        ParticleProgressType SIZE = (e, n)->e.size = n;

        void set(EffectContainer container, float num);

    }

    public static ParticleProgressApplier progressOfType(ParticleProgressType type){
        return new ParticleProgressApplier(type);
    }

    public static ParticleColorApplier progressColor(Color color){
        return new ParticleColorApplier(color);
    }

    public static class ParticleProgressApplier {

        public ParticleProgressType type;

        public ParticleProgress progressChain = ParticleProgress.NONE;

        public float from = 0, to = 1;

        public ParticleProgressApplier(ParticleProgressType type){
            this.type = type;
        }

        public void apply(EffectContainer effect){
            type.set(effect, Mathf.lerp(from, to, progressChain.get(effect)));
        }

        public ParticleProgressApplier from(int from){
            this.from = from;
            return this;
        }

        public ParticleProgressApplier to(int to){
            this.to = to;
            return this;
        }

        public ParticleProgressApplier progress(ParticleProgress progress){
            this.progressChain = progress;
            return this;
        }
    }

    public static class ParticleColorApplier extends ParticleProgressApplier {

        public Color color = new Color();

        public ParticleColorApplier(Color color){
            super(null);
            this.color.set(color);
        }

        @Override
        public void apply(EffectContainer effect){
            effect.color.lerp(color, progressChain.get(effect));
        }

    }

    public interface ParticleProgress{

        ParticleProgress NONE = EffectContainer::fin;

        float get(EffectContainer container);
        
        static ParticleProgress constant(float value){
            return p -> value;
        }

        default float getClamp(EffectContainer e){
            return getClamp(e, true);
        }

        default float getClamp(EffectContainer e, boolean clamp){
            return clamp ? Mathf.clamp(get(e)) : get(e);
        }

        default ParticleProgress inv(){
            return CompatFix.inv(this);
        }

        default ParticleProgress slope(){
            return CompatFix.slope(this);
        }

        default ParticleProgress clamp(){
            return CompatFix.clamp(this);
        }

        default ParticleProgress add(float amount){
            return CompatFix.add(this, amount);
        }

        default ParticleProgress add(ParticleProgress other){
            return CompatFix.add(this, other);
        }

        default ParticleProgress delay(float amount){
            return CompatFix.delay(this, amount);
        }

        default ParticleProgress curve(float offset, float duration){
            return CompatFix.curve(this, offset, duration);
        }

        default ParticleProgress sustain(float offset, float grow, float sustain){
            return CompatFix.sustain(this, offset, grow, sustain);
        }

        default ParticleProgress shorten(float amount){
            return CompatFix.shorten(this, amount);
        }

        default ParticleProgress compress(float start, float end){
            return CompatFix.compress(this, start, end);
        }

        default ParticleProgress blend(ParticleProgress other, float amount){
            return CompatFix.blend(this, other, amount);
        }

        default ParticleProgress mul(ParticleProgress other){
            return CompatFix.mul(this, other);
        }

        default ParticleProgress mul(float amount){
            return CompatFix.mul(this, amount);
        }

        default ParticleProgress min(ParticleProgress other){
            return CompatFix.min(this, other);
        }

        default ParticleProgress sin(float offset, float scl, float mag){
            return CompatFix.sin(this, offset, scl, mag);
        }

        default ParticleProgress sin(float scl, float mag){
            return CompatFix.sin(this, scl, mag);
        }

        default ParticleProgress absin(float scl, float mag){
            return CompatFix.absin(this, scl, mag);
        }

        default ParticleProgress mod(float amount){
            return CompatFix.mod(this, amount);
        }

        default ParticleProgress loop(float time){
            return CompatFix.loop(this, time);
        }

        default ParticleProgress apply(ParticleProgress other, ParticleFunc func){
            return CompatFix.apply(this, other, func);
        }

        default ParticleProgress curve(Interp interp){
            return CompatFix.curve(this, interp);
        }

    }

    public interface ParticleFunc {
        float get(float a, float b);
    }

    /** RoboVM chokes on lambdas referencing self in default methods in interfaces, so they have to be moved into a separate class. */
    private static class CompatFix{

        static ParticleProgress inv(ParticleProgress self){
            return p -> 1f - self.get(p);
        }

        static ParticleProgress slope(ParticleProgress self){
            return p -> Mathf.slope(self.get(p));
        }

        static ParticleProgress clamp(ParticleProgress self){
            return p -> Mathf.clamp(self.get(p));
        }

        static ParticleProgress add(ParticleProgress self, float amount){
            return p -> self.get(p) + amount;
        }

        static ParticleProgress add(ParticleProgress self, ParticleProgress other){
            return p -> self.get(p) + other.get(p);
        }

        static ParticleProgress delay(ParticleProgress self, float amount){
            return p -> (self.get(p) - amount) / (1f - amount);
        }

        static ParticleProgress curve(ParticleProgress self, float offset, float duration){
            return p -> (self.get(p) - offset) / duration;
        }

        static ParticleProgress sustain(ParticleProgress self, float offset, float grow, float sustain){
            return p -> {
                float val = self.get(p) - offset;
                return Math.min(Math.max(val, 0f) / grow, (grow + sustain + grow - val) / grow);
            };
        }

        static ParticleProgress shorten(ParticleProgress self, float amount){
            return p -> self.get(p) / (1f - amount);
        }

        static ParticleProgress compress(ParticleProgress self, float start, float end){
            return p -> Mathf.curve(self.get(p), start, end);
        }

        static ParticleProgress blend(ParticleProgress self, ParticleProgress other, float amount){
            return p -> Mathf.lerp(self.get(p), other.get(p), amount);
        }

        static ParticleProgress mul(ParticleProgress self, ParticleProgress other){
            return p -> self.get(p) * other.get(p);
        }

        static ParticleProgress mul(ParticleProgress self, float amount){
            return p -> self.get(p) * amount;
        }

        static ParticleProgress min(ParticleProgress self, ParticleProgress other){
            return p -> Math.min(self.get(p), other.get(p));
        }

        static ParticleProgress sin(ParticleProgress self, float offset, float scl, float mag){
            return p -> self.get(p) + Mathf.sin(Timers.time() + offset, scl, mag);
        }

        static ParticleProgress sin(ParticleProgress self, float scl, float mag){
            return p -> self.get(p) + Mathf.sin(scl, mag);
        }

        static ParticleProgress absin(ParticleProgress self, float scl, float mag){
            return p -> self.get(p) + Mathf.absin(scl, mag);
        }

        static ParticleProgress mod(ParticleProgress self, float amount){
            return p -> Mathf.mod(self.get(p), amount);
        }

        static ParticleProgress loop(ParticleProgress self, float time){
            return p -> Mathf.mod(self.get(p)/time, 1);
        }

        static ParticleProgress apply(ParticleProgress self, ParticleProgress other, ParticleFunc func){
            return p -> func.get(self.get(p), other.get(p));
        }

        static ParticleProgress curve(ParticleProgress self, Interp interp){
            return p -> interp.apply(self.get(p));
        }
    }
}
