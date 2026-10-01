package io.anuke.ucore.util;

public interface Interp {
    Interp linear = (a) -> a;
    Interp reverse = (a) -> 1.0F - a;
    Interp smooth = (a) -> a * a * (3.0F - 2.0F * a);
    Interp smooth2 = (a) -> {
        a = a * a * (3.0F - 2.0F * a);
        return a * a * (3.0F - 2.0F * a);
    };
    Interp one = (a) -> 1.0F;
    Interp zero = (a) -> 0.0F;
    Interp slope = Mathf::slope;
    Interp smoother = (a) -> a * a * a * (a * (a * 6.0F - 15.0F) + 10.0F);
    Interp fade = smoother;
    Pow pow2 = new Pow(2);
    PowIn pow2In = new PowIn(2);
    PowIn slowFast = pow2In;
    PowOut pow2Out = new PowOut(2);
    PowOut fastSlow = pow2Out;
    Interp pow2InInverse = (a) -> (float)Math.sqrt((double)a);
    Interp pow2OutInverse = (a) -> 1.0F - (float)Math.sqrt((double)(-(a - 1.0F)));
    Pow pow3 = new Pow(3);
    PowIn pow3In = new PowIn(3);
    PowOut pow3Out = new PowOut(3);
    Interp pow3InInverse = (a) -> (float)Math.cbrt((double)a);
    Interp pow3OutInverse = (a) -> 1.0F - (float)Math.cbrt((double)(-(a - 1.0F)));
    Pow pow4 = new Pow(4);
    PowIn pow4In = new PowIn(4);
    PowOut pow4Out = new PowOut(4);
    Pow pow5 = new Pow(5);
    PowIn pow5In = new PowIn(5);
    PowIn pow10In = new PowIn(10);
    PowOut pow10Out = new PowOut(10);
    PowOut pow5Out = new PowOut(5);
    Interp sine = (a) -> (1.0F - Mathf.cos(a * (float)Math.PI)) / 2.0F;
    Interp sineIn = (a) -> 1.0F - Mathf.cos(a * (float)Math.PI / 2.0F);
    Interp sineOut = (a) -> Mathf.sin(a * (float)Math.PI / 2.0F);
    Exp exp10 = new Exp(2.0F, 10.0F);
    ExpIn exp10In = new ExpIn(2.0F, 10.0F);
    ExpOut exp10Out = new ExpOut(2.0F, 10.0F);
    Exp exp5 = new Exp(2.0F, 5.0F);
    ExpIn exp5In = new ExpIn(2.0F, 5.0F);
    ExpOut exp5Out = new ExpOut(2.0F, 5.0F);
    Interp circle = (a) -> {
        if (a <= 0.5F) {
            a *= 2.0F;
            return (1.0F - (float)Math.sqrt((double)(1.0F - a * a))) / 2.0F;
        } else {
            --a;
            a *= 2.0F;
            return ((float)Math.sqrt((double)(1.0F - a * a)) + 1.0F) / 2.0F;
        }
    };
    Interp circleIn = (a) -> 1.0F - (float)Math.sqrt((double)(1.0F - a * a));
    Interp circleOut = (a) -> {
        --a;
        return (float)Math.sqrt((double)(1.0F - a * a));
    };
    Elastic elastic = new Elastic(2.0F, 10.0F, 7, 1.0F);
    ElasticIn elasticIn = new ElasticIn(2.0F, 10.0F, 6, 1.0F);
    ElasticOut elasticOut = new ElasticOut(2.0F, 10.0F, 7, 1.0F);
    Swing swing = new Swing(1.5F);
    SwingIn swingIn = new SwingIn(2.0F);
    SwingOut swingOut = new SwingOut(2.0F);
    Bounce bounce = new Bounce(4);
    BounceIn bounceIn = new BounceIn(4);
    BounceOut bounceOut = new BounceOut(4);

    float apply(float var1);

    default float apply(float start, float end, float a) {
        return start + (end - start) * this.apply(a);
    }

    public static class Pow implements Interp {
        final float power;

        public Pow(float power) {
            this.power = power;
        }

        public Pow(int power) {
            this.power = (float)power;
        }

        public float apply(float a) {
            return a <= 0.5F ? (float)Math.pow((double)(a * 2.0F), (double)this.power) / 2.0F : (float)Math.pow((double)((a - 1.0F) * 2.0F), (double)this.power) / (float)(this.power % 2.0F == 0.0F ? -2 : 2) + 1.0F;
        }
    }

    public static class PowIn extends Pow {
        public PowIn(float power) {
            super(power);
        }

        public PowIn(int power) {
            super(power);
        }

        public float apply(float a) {
            return (float)Math.pow((double)a, (double)this.power);
        }
    }

    public static class PowOut extends Pow {
        public PowOut(int power) {
            super(power);
        }

        public PowOut(float power) {
            super(power);
        }

        public float apply(float a) {
            return (float)Math.pow((double)(a - 1.0F), (double)this.power) * (float)(this.power % 2.0F == 0.0F ? -1 : 1) + 1.0F;
        }
    }

    public static class Exp implements Interp {
        final float value;
        final float power;
        final float min;
        final float scale;

        public Exp(float value, float power) {
            this.value = value;
            this.power = power;
            this.min = (float)Math.pow((double)value, (double)(-power));
            this.scale = 1.0F / (1.0F - this.min);
        }

        public float apply(float a) {
            return a <= 0.5F ? ((float)Math.pow((double)this.value, (double)(this.power * (a * 2.0F - 1.0F))) - this.min) * this.scale / 2.0F : (2.0F - ((float)Math.pow((double)this.value, (double)(-this.power * (a * 2.0F - 1.0F))) - this.min) * this.scale) / 2.0F;
        }
    }

    public static class ExpIn extends Exp {
        public ExpIn(float value, float power) {
            super(value, power);
        }

        public float apply(float a) {
            return ((float)Math.pow((double)this.value, (double)(this.power * (a - 1.0F))) - this.min) * this.scale;
        }
    }

    public static class ExpOut extends Exp {
        public ExpOut(float value, float power) {
            super(value, power);
        }

        public float apply(float a) {
            return 1.0F - ((float)Math.pow((double)this.value, (double)(-this.power * a)) - this.min) * this.scale;
        }
    }

    public static class Elastic implements Interp {
        final float value;
        final float power;
        final float scale;
        final float bounces;

        public Elastic(float value, float power, int bounces, float scale) {
            this.value = value;
            this.power = power;
            this.scale = scale;
            this.bounces = (float)bounces * (float)Math.PI * (float)(bounces % 2 == 0 ? 1 : -1);
        }

        public float apply(float a) {
            if (a <= 0.5F) {
                a *= 2.0F;
                return (float)Math.pow((double)this.value, (double)(this.power * (a - 1.0F))) * Mathf.sin(a * this.bounces) * this.scale / 2.0F;
            } else {
                a = 1.0F - a;
                a *= 2.0F;
                return 1.0F - (float)Math.pow((double)this.value, (double)(this.power * (a - 1.0F))) * Mathf.sin(a * this.bounces) * this.scale / 2.0F;
            }
        }
    }

    public static class ElasticIn extends Elastic {
        public ElasticIn(float value, float power, int bounces, float scale) {
            super(value, power, bounces, scale);
        }

        public float apply(float a) {
            return (double)a >= 0.99 ? 1.0F : (float)Math.pow((double)this.value, (double)(this.power * (a - 1.0F))) * Mathf.sin(a * this.bounces) * this.scale;
        }
    }

    public static class ElasticOut extends Elastic {
        public ElasticOut(float value, float power, int bounces, float scale) {
            super(value, power, bounces, scale);
        }

        public float apply(float a) {
            if (a == 0.0F) {
                return 0.0F;
            } else {
                a = 1.0F - a;
                return 1.0F - (float)Math.pow((double)this.value, (double)(this.power * (a - 1.0F))) * Mathf.sin(a * this.bounces) * this.scale;
            }
        }
    }

    public static class Bounce extends BounceOut {
        public Bounce(float[] widths, float[] heights) {
            super(widths, heights);
        }

        public Bounce(int bounces) {
            super(bounces);
        }

        private float out(float a) {
            float test = a + this.widths[0] / 2.0F;
            return test < this.widths[0] ? test / (this.widths[0] / 2.0F) - 1.0F : super.apply(a);
        }

        public float apply(float a) {
            return a <= 0.5F ? (1.0F - this.out(1.0F - a * 2.0F)) / 2.0F : this.out(a * 2.0F - 1.0F) / 2.0F + 0.5F;
        }
    }

    public static class BounceOut implements Interp {
        final float[] widths;
        final float[] heights;

        public BounceOut(float[] widths, float[] heights) {
            if (widths.length != heights.length) {
                throw new IllegalArgumentException("Must be the same number of widths and heights.");
            } else {
                this.widths = widths;
                this.heights = heights;
            }
        }

        public BounceOut(int bounces) {
            if (bounces >= 2 && bounces <= 5) {
                this.widths = new float[bounces];
                this.heights = new float[bounces];
                this.heights[0] = 1.0F;
                switch (bounces) {
                    case 2:
                        this.widths[0] = 0.6F;
                        this.widths[1] = 0.4F;
                        this.heights[1] = 0.33F;
                        break;
                    case 3:
                        this.widths[0] = 0.4F;
                        this.widths[1] = 0.4F;
                        this.widths[2] = 0.2F;
                        this.heights[1] = 0.33F;
                        this.heights[2] = 0.1F;
                        break;
                    case 4:
                        this.widths[0] = 0.34F;
                        this.widths[1] = 0.34F;
                        this.widths[2] = 0.2F;
                        this.widths[3] = 0.15F;
                        this.heights[1] = 0.26F;
                        this.heights[2] = 0.11F;
                        this.heights[3] = 0.03F;
                        break;
                    case 5:
                        this.widths[0] = 0.3F;
                        this.widths[1] = 0.3F;
                        this.widths[2] = 0.2F;
                        this.widths[3] = 0.1F;
                        this.widths[4] = 0.1F;
                        this.heights[1] = 0.45F;
                        this.heights[2] = 0.3F;
                        this.heights[3] = 0.15F;
                        this.heights[4] = 0.06F;
                }

                float[] var10000 = this.widths;
                var10000[0] *= 2.0F;
            } else {
                throw new IllegalArgumentException("bounces cannot be < 2 or > 5: " + bounces);
            }
        }

        public float apply(float a) {
            if (a == 1.0F) {
                return 1.0F;
            } else {
                a += this.widths[0] / 2.0F;
                float width = 0.0F;
                float height = 0.0F;
                int i = 0;

                for(int n = this.widths.length; i < n; ++i) {
                    width = this.widths[i];
                    if (a <= width) {
                        height = this.heights[i];
                        break;
                    }

                    a -= width;
                }

                a /= width;
                float z = 4.0F / width * height * a;
                return 1.0F - (z - z * a) * width;
            }
        }
    }

    public static class BounceIn extends BounceOut {
        public BounceIn(float[] widths, float[] heights) {
            super(widths, heights);
        }

        public BounceIn(int bounces) {
            super(bounces);
        }

        public float apply(float a) {
            return 1.0F - super.apply(1.0F - a);
        }
    }

    public static class Swing implements Interp {
        private final float scale;

        public Swing(float scale) {
            this.scale = scale * 2.0F;
        }

        public float apply(float a) {
            if (a <= 0.5F) {
                a *= 2.0F;
                return a * a * ((this.scale + 1.0F) * a - this.scale) / 2.0F;
            } else {
                --a;
                a *= 2.0F;
                return a * a * ((this.scale + 1.0F) * a + this.scale) / 2.0F + 1.0F;
            }
        }
    }

    public static class SwingOut implements Interp {
        private final float scale;

        public SwingOut(float scale) {
            this.scale = scale;
        }

        public float apply(float a) {
            --a;
            return a * a * ((this.scale + 1.0F) * a + this.scale) + 1.0F;
        }
    }

    public static class SwingIn implements Interp {
        private final float scale;

        public SwingIn(float scale) {
            this.scale = scale;
        }

        public float apply(float a) {
            return a * a * ((this.scale + 1.0F) * a - this.scale);
        }
    }
}
