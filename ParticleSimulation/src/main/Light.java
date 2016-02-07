/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import glm.vec._3.Vec3;
import glm.vec._4.Vec4;

/**
 *
 * @author elect
 */
public class Light {

    /**
     * Default values.
     *
     * http://www.sccg.sk/~samuelcik/opengl/opengl_07.pdf
     */
    public Vec4 position = new Vec4(0f, 0f, 1f, 0f);
    public Vec4 diffuse = new Vec4(1f);
    public Vec4 ambient = new Vec4(0f, 0f, 0f, 1f);
    public Vec4 specular = new Vec4(1.0f);
    public float constantAttenuation = 1.0f;
    public float linearAttenuation = 0f;
    public float quadraticAttenuation = 0f;
    public float spotCutoff = 180f;
    public float spotExponent = 0f;
    public Vec3 spotDirection = new Vec3(0f, 0f, -1f);

    public Light(Vec4 position, Vec4 ambient, Vec4 diffuse, Vec4 specular, float spotCutOff,
            float spotExponent) {

        this.position.set(position);
        this.diffuse.set(diffuse);
        this.ambient.set(ambient);
        this.specular = specular;
        this.spotExponent = spotExponent;
    }
}
