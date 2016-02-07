/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.jogamp.nativewindow.util.Dimension;
import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;

/**
 *
 * @author elect
 */
public class ParticleSimulation {

    public static void main(String[] args) {
        new ParticleSimulation();
    }

    private static int screenIdx = 0;
//    private static Dimension windowSize = new Dimension(1920, 1080);
    private static Dimension windowSize = new Dimension(640, 480);
    private static boolean undecorated = false;
    private static boolean alwaysOnTop = false;
    private static boolean fullscreen = false;
    private static boolean mouseVisible = true;
    private static boolean mouseConfined = false;
    private static String title = "lots and lots of particles ... yeay ... :-)";
    public static GLWindow glWindow;
    public static Animator animator;

    public ParticleSimulation() {

        Display display = NewtFactory.createDisplay(null);
        Screen screen = NewtFactory.createScreen(display, screenIdx);
        GLProfile glProfile = GLProfile.get(GLProfile.GL4);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glWindow = GLWindow.create(screen, glCapabilities);

        glWindow.setSize(windowSize.getWidth(), windowSize.getHeight());
        glWindow.setPosition(50, 50);
        glWindow.setUndecorated(undecorated);
        glWindow.setAlwaysOnTop(alwaysOnTop);
        glWindow.setFullscreen(fullscreen);
        glWindow.setPointerVisible(mouseVisible);
        glWindow.confinePointer(mouseConfined);
        glWindow.setTitle(title);
        glWindow.setVisible(true);

        glWindow.addGLEventListener(new OpenGlListener());
//        glWindow.addKeyListener(particleSimulation);

        animator = new Animator(glWindow);
        animator.setRunAsFastAsPossible(false);
        animator.setUpdateFPSFrames(1000, System.out);
        animator.start();
    }

}
