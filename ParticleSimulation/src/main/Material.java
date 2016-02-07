/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import glm.vec._4.Vec4;

/**
 *
 * @author elect
 */
public class Material {

    public Vec4 ambient;
    public Vec4 diffuse;
    public Vec4 specular;
    public float shininess;
    public Vec4 emission;

    public Material() {

    }

    public Material(Vec4 ambient, Vec4 diffuse, Vec4 specular, float shininess, Vec4 emission) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = shininess;
        this.emission = emission;
    }
}
