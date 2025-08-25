#version 330 core

in vec2 v_texCoord;
in vec3 v_worldPos;

uniform sampler2D u_texture;

out vec4 fragColor;

void main() {
    // Échantillonner la texture
    vec4 texColor = texture(u_texture, v_texCoord);

    // Effet de lighting basique basé sur la hauteur
    float lightIntensity = 0.7 + 0.3 * (v_worldPos.y + 1.0) / 2.0;

    // Couleur finale avec éclairage
    fragColor = vec4(texColor.rgb * lightIntensity, texColor.a);
}