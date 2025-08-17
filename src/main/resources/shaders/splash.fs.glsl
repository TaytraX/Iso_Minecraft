#version 330 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D splashTexture;

void main() {
    fragColor = texture(splashTexture, fragTexCoord);
}