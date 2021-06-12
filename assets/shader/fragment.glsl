#version 330

uniform sampler2D colorSampler;
uniform vec4 color;

in vec2 passTexCoord;

out vec4 outColor;

void main(void) {

	vec4 result = texture(colorSampler, passTexCoord) * color;
	outColor = result;
}
