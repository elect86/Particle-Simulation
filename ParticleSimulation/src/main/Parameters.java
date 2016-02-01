/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author elect
 */
public class Parameters {

    public static final int PARTICLE_COUNT = 6000000;
    public static final int ATTRACTOR_COUNT = 8;
    
    public static final int RAND_MAX = Integer.MAX_VALUE;

    public static class Program {

        public static final int GRAPHICS = 0;
        public static final int COMPUTE = 1;
        public static final int MAX = 2;
    }

    public static class Buffer {

        public static final int OBJECTS = 0;
        public static final int POSITION = 1;
        public static final int LIFE = 2;
        public static final int VELOCITY = 3;
        public static final int MAX = 4;
    }
}
