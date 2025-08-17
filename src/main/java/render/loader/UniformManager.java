package render.loader;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;

public class UniformManager {
    private final Map<String, Integer> uniforms = new HashMap<>();
    private final int programID;

    UniformManager(int programID) {
        this.programID = programID;
    }

    void createUniform(String name) {
        int location = glGetUniformLocation(programID, name);
        if (location < 0) {
            System.err.println("Uniform not found: " + name);
        }
        uniforms.put(name, location);
    }

    void parseUniformsFromShader(String source) {
        // Parser les deux shaders
        parseUniformsFromSource(source);
    }

    void parseUniformsFromSource(String shaderSource) {
        String[] lines = shaderSource.split("\n");

        for (String line : lines) {
            line = line.trim(); // Supprimer espaces

            // Chercher les lignes qui commencent par "uniform"
            if (line.startsWith("uniform") && !line.startsWith("//")) {
                String uniformName = extractUniformName(line);
                if (uniformName != null) {
                    createUniform(uniformName);
                    System.out.println("Uniform détecté : " + uniformName);
                }
            }
        }
    }

    String extractUniformName(String uniformLine) {
        // Exemple : "uniform mat4 transformationMatrix;"
        //           → extraire "transformationMatrix"

        String[] parts = uniformLine.split("\\s+"); // Split sur espaces

        if (parts.length >= 3) {
            // parts[0] = "uniform"
            // parts[1] = "mat4" (type)
            // parts[2] = "transformationMatrix;" (nom avec ;)

            // Supprimer ;
            return parts[2].replace(";", "");
        }
        return null;
    }

    void cleanup() {
        uniforms.clear();
    }

}