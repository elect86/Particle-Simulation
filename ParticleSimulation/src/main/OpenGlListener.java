/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_DYNAMIC_COPY;
import static com.jogamp.opengl.GL3ES3.GL_COMPUTE_SHADER;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Vec4;
import java.nio.FloatBuffer;
import static main.Parameters.*;

/**
 *
 * @author elect
 */
public class OpenGlListener implements GLEventListener {

    private final String SHADERS_ROOT = "src/shaders";
    private final String SHADERS_SOURCE_GRAPHICS = "graphics";
    private final String SHADER_SOURCE_COMPUTE = "compute";

    private Light[] lights = new Light[2];
    private Material material = new Material();
    private int[] programName = new int[Program.MAX], bufferName = new int[Buffer.MAX];

    @Override
    public void init(GLAutoDrawable drawable) {

        GL4 gl4 = drawable.getGL().getGL4();

        gl4.glEnable(GL_DEPTH_TEST);
        gl4.glCullFace(GL_BACK);
        gl4.glEnable(GL_CULL_FACE);

        initLight(gl4);

        initPrograms(gl4);
    }

    private void initLight(GL4 gl4) {

        lights[0] = new Light(new Vec4(0, 0, 1, 0), new Vec4(1.0f), new Vec4(1.0f), new Vec4(1.0f),
                90.0f, 20.0f);
        lights[1] = new Light(new Vec4(0, 0, 1, 0), new Vec4(1.0f), new Vec4(1.0f), new Vec4(1.0f),
                90.0f, 20.0f);
    }

    private void initPrograms(GL4 gl4) {
        // graphics
        {
            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_GRAPHICS, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_GRAPHICS, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName[Program.GRAPHICS] = shaderProgram.program();

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }
        // compute
        {
            ShaderCode compShaderCode = ShaderCode.create(gl4, GL_COMPUTE_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADER_SOURCE_COMPUTE, "comp", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName[Program.COMPUTE] = shaderProgram.program();

            shaderProgram.add(compShaderCode);
            shaderProgram.link(gl4, System.out);
        }
    }

    private void initBuffers(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);
        {
            float[] objects = {
                -15.0f, -15.0f, -15.0f,
                +15.0f, -15.0f, -15.0f,
                +15.0f, +15.0f, -15.0f,
                -15.0f, -15.0f, -15.0f,
                +15.0f, +15.0f, -15.0f,
                -15.0f, +15.0f, -15.0f};
            FloatBuffer objectBuffer = GLBuffers.newDirectFloatBuffer(objects);

            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.OBJECTS]);
            gl4.glBufferData(GL_ARRAY_BUFFER, objects.length * Float.BYTES, objectBuffer.rewind(), GL_DYNAMIC_COPY);

            BufferUtils.destroyDirectBuffer(objectBuffer);
        }
        {
            float[] computePositions = new float[PARTICLE_COUNT * 3];
            for (int i = 0; i < computePositions.length; i++) {
                computePositions[i] = (float) ((glm.linearRand(0, RAND_MAX) % 2000) / 500);
            }
            FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(computePositions);

            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.POSITION]);
            gl4.glBufferData(GL_ARRAY_BUFFER, computePositions.length * Float.BYTES, positionBuffer.rewind(), GL_DYNAMIC_COPY);

            BufferUtils.destroyDirectBuffer(positionBuffer);
        }
        {
            float[] computeLifes = new float[PARTICLE_COUNT];
            for (int i = 0; i < 10; i++) {
                computeLifes[i] = (float) glm.linearRand(0, 1);
            }
            FloatBuffer lifeBuffer = GLBuffers.newDirectFloatBuffer(computeLifes);

            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.LIFE]);
            gl4.glBufferData(GL_ARRAY_BUFFER, computeLifes.length * Float.BYTES, lifeBuffer.rewind(), GL_BACK);
        }
        {
            float[] computeVelocities = new float[PARTICLE_COUNT * 4];
            for (int i = 0; i < computeVelocities.length; i += 4) {
                computeVelocities[i + 0] = (float) ((glm.linearRand(0, RAND_MAX) % 100) / 500
                        - (glm.linearRand(0, RAND_MAX) % 100) / 500);
                computeVelocities[i + 1] = (float) ((glm.linearRand(0, RAND_MAX) % 100) / 500
                        - (glm.linearRand(0, RAND_MAX) % 100) / 500);
                computeVelocities[i + 2] = (float) ((glm.linearRand(0, RAND_MAX) % 100) / 500
                        - (glm.linearRand(0, RAND_MAX) % 100) / 500);
                computeVelocities[i + 3] = 0;
            }
            FloatBuffer velocityBuffer = GLBuffers.newDirectFloatBuffer(computeVelocities);

            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VELOCITY]);
            gl4.glBufferData(GL_ARRAY_BUFFER, computeVelocities.length * Float.BYTES, velocityBuffer.rewind(), GL_BACK);
        }
        {
            float[] computeAttractors = new float[ATTRACTOR_COUNT * 4];
            for (int i = 0; i < computeAttractors.length; i += 4) {
                computeAttractors[i + 0] = (float) ((glm.linearRand(0, RAND_MAX) % 500) / 30
                        - (glm.linearRand(0, RAND_MAX) % 500) / 30);
                computeAttractors[i + 1] = (float) ((glm.linearRand(0, RAND_MAX) % 500) / 30
                        - (glm.linearRand(0, RAND_MAX) % 500) / 30);
                computeAttractors[i + 2] = (float) ((glm.linearRand(0, RAND_MAX) % 500) / 30
                        - (glm.linearRand(0, RAND_MAX) % 500) / 30);
                computeAttractors[i + 0] = (float) ((glm.linearRand(0, RAND_MAX) % 500) / 30
                        - (glm.linearRand(0, RAND_MAX) % 500) / 30);
            }
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

    }

}
