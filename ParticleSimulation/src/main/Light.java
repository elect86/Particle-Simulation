/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import dev.Vec4;

/**
 *
 * @author elect
 */
public class Light {
    
    public Vec4 position;
    public Vec4 ambient;
    public Vec4 diffuse;
    public Vec4 specular;
    public float spotCutOff;
    public float spotExponent;

    public Light(Vec4 position, Vec4 ambient, Vec4 diffuse, Vec4 specular, float spotCutOff, float spotExponent) {
        this.position = position;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.spotCutOff = spotCutOff;
        this.spotExponent = spotExponent;
    }
}
