#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <rockstar:common.glsl>

layout(std140) uniform RockstarUniforms {
    vec2 Size;
    vec4 Radius;
    float Smoothness;
    vec4 TopLeftColor;
    vec4 BottomLeftColor;
    vec4 TopRightColor;
    vec4 BottomRightColor;
};

in vec2 FragCoord;
in vec4 FragColor;

out vec4 OutColor;

vec4 bilinearInterpolation(vec2 uv) {
    vec4 topColor = mix(TopLeftColor, TopRightColor, uv.x);
    vec4 bottomColor = mix(BottomLeftColor, BottomRightColor, uv.x);
    return mix(topColor, bottomColor, uv.y);
}

void main() {
    vec2 center = Size * 0.5;
    vec2 uv = FragCoord;

    vec4 gradientColor = bilinearInterpolation(uv);

    float distance = roundedBoxSDF(center - (FragCoord * Size), center - 1.0, Radius);
    float alpha = 1.0 - smoothstep(1.0 - Smoothness, 1.0, distance);

    vec4 finalColor = vec4(gradientColor.rgb, gradientColor.a * alpha);

    if (finalColor.a == 0.0) {
        discard;
    }

    OutColor = finalColor * ColorModulator;
}
