#version 330 core

layout (location = 0) in vec2 position;

uniform float progress;      // 0.0 à 1.0
uniform vec2 barPosition;   // Position de la barre (NDC)
uniform vec2 barSize;       // Taille de la barre (NDC)

void main() {
    vec2 scaledPos = position;

    // Mise à l'échelle selon le progress pour l'axe X
    if (scaledPos.x > 0.0) {
        scaledPos.x *= progress;
    }

    // Application de la taille et position
    scaledPos *= barSize;
    scaledPos += barPosition;

    gl_Position = vec4(scaledPos, 0.0, 1.0);
}