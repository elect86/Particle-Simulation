#version 430 core 

#define OUTPUT      0

in float color; 

layout (location = OUTPUT, index= 0) out vec4 outColor;

void main(){
	
    if (color < 0.1) {

        // Fast tote Partikel blenden nach Schwarz über.
        outColor = mix(vec4(vec3(0.0),1.0), vec4(0.0,0.5,1.0,1.0), color*10.0);  

    } else if (color > 0.9) {

        // Neu geborene Partikel blenden von Schwarz ein.
        outColor = mix(vec4(0.6,0.05,0.0,1.0), vec4(vec3(0.0),1.0), (color-0.9)*10.0);  

    } else {

        // Lebensdauer Rot --> Blau.
        outColor = mix(vec4(0.0,0.5,1.0,1.0), vec4(0.6,0.05,0.0,1.0), color);
    }	
}
