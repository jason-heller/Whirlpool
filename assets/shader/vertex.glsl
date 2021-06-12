#version 330

uniform mat4 projectionMatrix;
uniform vec4 posTransform;
uniform vec4 texTransform;
uniform float rotation;

layout(location = 0) in vec2 inPos;
layout(location = 1) in vec2 inTexCoord;

out vec2 passTexCoord;

vec2 rotate(vec2 v, float a) {
	float s = sin(a);
	float c = cos(a);
	mat2 m = mat2(c, -s, s, c);
	return m * v;
}

void main(void) {
	vec2 coords = rotate(inPos.xy, rotation) * posTransform.zw;
	gl_Position = projectionMatrix * vec4(coords.xy + posTransform.xy, 0.0, 1.0);
	passTexCoord = vec2(inTexCoord.xy + texTransform.xy) * texTransform.zw;
}
