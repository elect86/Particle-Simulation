/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_INVALID_ENUM;
import static com.jogamp.opengl.GL.GL_INVALID_FRAMEBUFFER_OPERATION;
import static com.jogamp.opengl.GL.GL_INVALID_OPERATION;
import static com.jogamp.opengl.GL.GL_INVALID_VALUE;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_NO_ERROR;
import static com.jogamp.opengl.GL.GL_ONE;
import static com.jogamp.opengl.GL.GL_OUT_OF_MEMORY;
import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_ALL_BARRIER_BITS;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_DEPTH;
import static com.jogamp.opengl.GL2ES3.GL_DYNAMIC_COPY;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import static com.jogamp.opengl.GL3ES3.GL_COMPUTE_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_SHADER_STORAGE_BUFFER;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.GL_MAP_COHERENT_BIT;
import static com.jogamp.opengl.GL4.GL_MAP_PERSISTENT_BIT;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._4.Vec4;
import glm.vec._3.Vec3;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import static main.Parameters.*;
import semantic.Buffer;
import semantic.Program;

/**
 *
 * @author elect
 */
public class OpenGlListener implements GLEventListener {

    private final String SHADERS_ROOT = "src/shaders";
    private final String SHADERS_SOURCE = "particles";

    private Light[] lights = new Light[2];
    private Material material = new Material();
    private int[] programName = new int[Program.MAX], bufferName = new int[Buffer.MAX], vertexArrayName = {0};
    private int dtUniform;
    private ByteBuffer uniformPointer;

    private FloatBuffer attractorBuffer;

    @Override
    public void init(GLAutoDrawable drawable) {
        System.out.println("init");
        GL4 gl4 = drawable.getGL().getGL4();

        gl4.glEnable(GL_DEPTH_TEST);
        gl4.glCullFace(GL_BACK);
        gl4.glEnable(GL_CULL_FACE);

        initLight(gl4);

        initPrograms(gl4);

        initBuffers(gl4);
        
        initVertexArray(gl4);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        uniformPointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

        // disable v-sync
        gl4.setSwapInterval(0);

        checkError(gl4, "init");
    }

    private void initLight(GL4 gl4) {

        lights[0] = new Light(new Vec4(0, 0, 1, 0), new Vec4(1.0f), new Vec4(1.0f), new Vec4(1.0f),
                90.0f, 20.0f);
        lights[1] = new Light(new Vec4(0, 0, 1, 0), new Vec4(1.0f), new Vec4(1.0f), new Vec4(1.0f),
                90.0f, 20.0f);

        checkError(gl4, "initLight");
    }

    private void initPrograms(GL4 gl4) {
        /**
         * Graphics.
         */
        {
            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName[Program.GRAPHICS] = shaderProgram.program();

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }
        /**
         * Compute.
         */
        {
            ShaderCode compShaderCode = ShaderCode.create(gl4, GL_COMPUTE_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "comp", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName[Program.COMPUTE] = shaderProgram.program();

            shaderProgram.add(compShaderCode);
            shaderProgram.link(gl4, System.out);

            dtUniform = gl4.glGetUniformLocation(programName[Program.COMPUTE], "dt");
        }
        checkError(gl4, "initPrograms");
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
            gl4.glBufferData(GL_ARRAY_BUFFER, objects.length * Float.BYTES, objectBuffer, GL_DYNAMIC_COPY);

            BufferUtils.destroyDirectBuffer(objectBuffer);
        }
        /**
         * Physic-Simulation!
         * ==================================================.
         */
        /**
         * Position.
         */
        {
            float[] computePositions = new float[PARTICLE_COUNT * 3];
            for (int i = 0; i < computePositions.length; i++) {
                computePositions[i] = (float) ((glm.linearRand(0, RAND_MAX) % 2000) / 500);
            }
            FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(computePositions);

            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.POSITION]);
            gl4.glBufferData(GL_ARRAY_BUFFER, computePositions.length * Float.BYTES, positionBuffer, GL_DYNAMIC_COPY);

            BufferUtils.destroyDirectBuffer(positionBuffer);
        }
        /**
         * Life duration.
         */
        {
            float[] computeLifes = new float[PARTICLE_COUNT];
            for (int i = 0; i < 10; i++) {
                computeLifes[i] = (float) glm.linearRand(0, 1);
            }
            FloatBuffer lifeBuffer = GLBuffers.newDirectFloatBuffer(computeLifes);

            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.LIFE]);
            gl4.glBufferData(GL_ARRAY_BUFFER, computeLifes.length * Float.BYTES, lifeBuffer, GL_DYNAMIC_COPY);
        }
        /**
         * Speed.
         */
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
            gl4.glBufferData(GL_ARRAY_BUFFER, computeVelocities.length * Float.BYTES, velocityBuffer, GL_DYNAMIC_COPY);
        }
        /**
         * Attractors points.
         */
        {
            float[] computeAttractors = new float[ATTRACTOR_COUNT * 4];
            for (int i = 0; i < computeAttractors.length; i += 4) {
                computeAttractors[i + 0] = (float) ((glm.linearRand(0, RAND_MAX) % 500) / 30
                        - (glm.linearRand(0, RAND_MAX) % 500) / 30);
                computeAttractors[i + 1] = (float) ((glm.linearRand(0, RAND_MAX) % 500) / 30
                        - (glm.linearRand(0, RAND_MAX) % 500) / 30);
                computeAttractors[i + 2] = (float) ((glm.linearRand(0, RAND_MAX) % 500) / 30
                        - (glm.linearRand(0, RAND_MAX) % 500) / 30);
                computeAttractors[i + 3] = 0f; // G_Attractor_Mass
            }
            attractorBuffer = GLBuffers.newDirectFloatBuffer(computeAttractors);

            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.ATTRACTORS]);
            gl4.glBufferData(GL_ARRAY_BUFFER, computeAttractors.length * Float.BYTES, attractorBuffer, GL_DYNAMIC_COPY);
        }
        /**
         * mvp mat4.
         */
        {
            int[] uniformBufferOffset = {0};
            gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
            int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            gl4.glBufferStorage(GL_UNIFORM_BUFFER, uniformBlockSize, null,
                    GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
        }
        checkError(gl4, "initBuffers");
    }

    private void initVertexArray(GL4 gl4) {
        
        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.POSITION]);
            gl4.glVertexAttribPointer(semantic.Attr.POSITION, 4, GL_FLOAT, false, Vec4.SIZE, 0);
            gl4.glEnableVertexAttribArray(semantic.Attr.POSITION);
            
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.LIFE]);
            gl4.glVertexAttribPointer(semantic.Attr.LIFE, 1, GL_FLOAT, false, Float.BYTES, 0);
            gl4.glEnableVertexAttribArray(semantic.Attr.LIFE);
            
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
        gl4.glBindVertexArray(0);
    }
    
    @Override
    public void dispose(GLAutoDrawable drawable) {
        ParticleSimulation.animator.stop();
        System.exit(0);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
//        System.out.println("display");
        GL4 gl4 = drawable.getGL().getGL4();

        if (ParticleSimulation.animator.getTotalFPSFrames() >= 1000) {
            ParticleSimulation.animator.resetFPSCounter();
            //        System.out.println("ParticleSimulation.animator.getTotalFPSFrames() " 
//                + ParticleSimulation.animator.getLastFPSUpdateTime());
        }

        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);
        gl4.glClearBufferfv(GL_DEPTH, 0, new float[]{1.0f}, 0);

        gl4.glEnable(GL_BLEND);
        gl4.glBlendFunc(GL_ONE, GL_ONE);

//        gl4.glDisable(GL_CULL_FACE);

        setMvp(gl4);

        for (int i = 0; i < ATTRACTOR_COUNT; i++) {

            attractorBuffer.put((float) (Math.sin(ParticleSimulation.animator.getTotalFPSFrames())
                    * (glm.linearRand(0, RAND_MAX) % 500) / 10.0));
            attractorBuffer.put((float) (Math.cos(ParticleSimulation.animator.getTotalFPSFrames())
                    * (glm.linearRand(0, RAND_MAX) % 500) / 10.0));
            attractorBuffer.put((float) (Math.tan(ParticleSimulation.animator.getTotalFPSFrames())));
        }
        attractorBuffer.rewind();

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.ATTRACTORS]);
        gl4.glBufferData(GL_ARRAY_BUFFER, ATTRACTOR_COUNT * Vec4.SIZE, attractorBuffer, GL_DYNAMIC_COPY);

        gl4.glUseProgram(programName[Program.COMPUTE]);

        /* Send time interval */
        gl4.glUniform1f(dtUniform, (float)(ParticleSimulation.animator.getLastFPSPeriod() / 1_000));
        gl4.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, Buffer.POSITION, bufferName[Buffer.POSITION]);
        gl4.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, Buffer.VELOCITY, bufferName[Buffer.VELOCITY]);
        gl4.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, Buffer.ATTRACTORS, bufferName[Buffer.ATTRACTORS]);
        gl4.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, Buffer.LIFE, bufferName[Buffer.LIFE]);

        gl4.glDispatchCompute(PARTICLE_COUNT / 128, 1, 1); 
        gl4.glMemoryBarrier((int) GL_ALL_BARRIER_BITS);
        
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glDrawArrays(GL_POINTS, 0, PARTICLE_COUNT);
    }

    private void setMvp(GL4 gl4) {

        Mat4 p = glm.perspectiveFov_(90.0f,
                ParticleSimulation.glWindow.getWidth(),
                ParticleSimulation.glWindow.getHeight(),
                0.5f, 10_000f);

        Mat4 v = glm.lookAt_(new Vec3(CAMERA_X, CAMERA_Y, CAMERA_Z),
                new Vec3(0.0f),
                new Vec3(0.0f, 1.0f, 0.0f));

        uniformPointer.asFloatBuffer().put(p.mul(v).toFa_());
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL4 gl4 = drawable.getGL().getGL4();

        gl4.glViewport(0, 0, width, height);
    }

    protected boolean checkError(GL gl, String title) {

        int error = gl.glGetError();
        if (error != GL_NO_ERROR) {
            String errorString;
            switch (error) {
                case GL_INVALID_ENUM:
                    errorString = "GL_INVALID_ENUM";
                    break;
                case GL_INVALID_VALUE:
                    errorString = "GL_INVALID_VALUE";
                    break;
                case GL_INVALID_OPERATION:
                    errorString = "GL_INVALID_OPERATION";
                    break;
                case GL_INVALID_FRAMEBUFFER_OPERATION:
                    errorString = "GL_INVALID_FRAMEBUFFER_OPERATION";
                    break;
                case GL_OUT_OF_MEMORY:
                    errorString = "GL_OUT_OF_MEMORY";
                    break;
                default:
                    errorString = "UNKNOWN";
                    break;
            }
            System.out.println("OpenGL Error(" + errorString + "): " + title);
            throw new Error();
        }
        return error == GL_NO_ERROR;
    }
}
