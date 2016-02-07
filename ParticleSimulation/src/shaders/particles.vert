#version 430 core

#define POSITION	0
#define LIFE            1
#define TRANSFORM       2

layout(std140, column_major) uniform;

layout (location = POSITION) in vec3 vertPos;
layout (location = LIFE) in float life;

layout(binding = TRANSFORM) uniform Transform {
    mat4 mvp;
} transform;

out float color;

void main() {

    color = life;
    gl_Position =  transform.mvp * vec4(vertPos, 1.0); 
}
