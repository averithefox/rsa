package com.ricedotwho.rsa.module.impl.render.utils;

import com.ricedotwho.rsm.module.Module;

public class CubeData extends Module {
        public double x, y, z;
        public float r, g, b, a;

        public CubeData(double x, double y, double z, float r, float g, float b, float a){
            this.x = x;
            this.y = y;
            this.z = z;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

    }