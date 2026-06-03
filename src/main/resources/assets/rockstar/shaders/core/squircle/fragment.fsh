#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <rockstar:common.glsl>

layout(std140) uniform RockstarUniforms {
    vec2 Size;
    vec4 Radius;
    float Smoothness;
    float CornerSmoothness;
};

in vec2 FragCoord;
in vec4 FragColor;

out vec4 OutColor;

float squircleSDF(vec2 p, vec2 b, vec4 r, float smoothness) {
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x = (p.y > 0.0) ? r.x : r.y;
    vec2 q = abs(p) - b + r.x;
    vec2 q_clamped = max(q, 0.0);

    float len = pow(pow(q_clamped.x, smoothness) + pow(q_clamped.y, smoothness), 1.0 / smoothness);

    return min(max(q.x, q.y), 0.0) + len - r.x;
}

void main() {
    vec2 center = Size * 0.5;

    float distance = squircleSDF(
        center - (FragCoord * Size),
        center - 1.0,
        Radius,
        CornerSmoothness
    );

    float alpha = 1.0 - smoothstep(1.0 - Smoothness, 1.0, distance);
    vec4 finalColor = vec4(FragColor.rgb, FragColor.a * alpha);

    if (finalColor.a == 0.0) {
        discard;
    }

    OutColor = finalColor * ColorModulator;
}
