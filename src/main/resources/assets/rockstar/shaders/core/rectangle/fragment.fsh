#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <rockstar:common.glsl>

layout(std140) uniform RockstarUniforms {
    vec2 Size;
    vec4 Radius;
    float Smoothness;
};

in vec2 FragCoord;
in vec4 FragColor;

out vec4 OutColor;

void main() {
    vec2 center = Size * 0.5;
    float distance = roundedBoxSDF(center - (FragCoord * Size), center - 1.0, Radius);

    float alpha = 1.0 - smoothstep(1.0 - Smoothness, 1.0, distance);
    vec4 finalColor = vec4(FragColor.rgb, FragColor.a * alpha);

    if (finalColor.a == 0.0) {
        discard;
    }

    OutColor = finalColor * ColorModulator;
}
