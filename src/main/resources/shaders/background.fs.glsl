#version 330 core

out vec4 fragColor;

uniform float time;
uniform vec2 resolution;

// Fonction de bruit pseudo-aléatoire pour les étoiles
float random(vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

// Fonction pour créer des étoiles scintillantes
float star(vec2 uv, float threshold, float intensity) {
    float noise = random(floor(uv * 50.0)); // Grille d'étoiles 50x50

    // Seuil pour déterminer si une étoile apparaît
    if (noise > threshold) {
        // Scintillement basé sur le temps
        float twinkle = 0.7 + 0.3 * sin(time * 3.0 + noise * 20.0);
        return intensity * twinkle;
    }
    return 0.0;
}

// Fonction pour créer la lune
float moon(vec2 uv, vec2 moonPos, float radius) {
    float dist = distance(uv, moonPos);

    // Lune avec bords flous
    float moonShape = 1.0 - smoothstep(radius - 0.02, radius + 0.02, dist);

    // Cratères subtils (texture de la lune)
    float craters = 0.9 + 0.1 * sin(uv.x * 30.0) * cos(uv.y * 25.0);

    return moonShape * craters;
}

void main() {
    // Normaliser les coordonnées d'écran
    vec2 uv = gl_FragCoord.xy;

    if (resolution.x > 0.0 && resolution.y > 0.0) {
        uv = gl_FragCoord.xy / resolution.xy;
    } else {
        uv = gl_FragCoord.xy / vec2(1200.0, 800.0); // Fallback
    }

    // Couleurs de base du ciel nocturne
    vec3 deepNight = vec3(0.05, 0.05, 0.15);      // Bleu très sombre
    vec3 horizon = vec3(0.1, 0.1, 0.25);          // Bleu un peu plus clair à l'horizon

    // Dégradé vertical (plus sombre en haut, plus clair vers l'horizon)
    vec3 skyColor = mix(deepNight, horizon, pow(uv.y, 2.0));

    // Ajouter des étoiles de différentes tailles
    float bigStars = star(uv, 0.98, 0.8);          // Grosses étoiles brillantes
    float mediumStars = star(uv + vec2(0.3, 0.7), 0.95, 0.5); // Étoiles moyennes
    float smallStars = star(uv + vec2(0.7, 0.2), 0.92, 0.3);  // Petites étoiles

    // Ajouter toutes les étoiles au ciel
    vec3 starColor = vec3(1.0, 0.95, 0.8); // Couleur chaude des étoiles
    skyColor += starColor * (bigStars + mediumStars + smallStars);

    // Ajouter la lune
    vec2 moonPosition = vec2(0.75, 0.8); // Position de la lune
    float moonRadius = 0.08;
    float moonBrightness = moon(uv, moonPosition, moonRadius);

    // Couleur de la lune
    vec3 moonColor = vec3(1.0, 0.95, 0.7);
    skyColor += moonColor * moonBrightness * 0.9;

    // Ajouter de subtiles variations atmosphériques
    float atmosphere = 0.1 * sin(uv.x * 3.0 + time * 0.2) * cos(uv.y * 2.0 + time * 0.15);
    skyColor += vec3(0.02, 0.03, 0.08) * atmosphere;

    // Effet de scintillement général très subtil
    float globalTwinkle = 1.0 + 0.02 * sin(time * 1.5);
    skyColor *= globalTwinkle;

    // Style Pixel Art : réduire les nuances pour un effet plus pixelisé
    skyColor = floor(skyColor * 32.0) / 32.0; // 32 nuances par canal

    // Assurer que les couleurs restent dans la gamme [0,1]
    skyColor = clamp(skyColor, 0.0, 1.0);

    fragColor = vec4(skyColor, 1.0);
}