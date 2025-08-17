package render.loader;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String parts = uniformLine.split("//")[0].trim();

        if (!parts.startsWith("uniform")) return null;

        Pattern pattern = Pattern.compile("uniform\\s+\\w+\\s+(\\w+)\\s*(?:\\[\\d+\\])?\\s*;");
        Matcher matcher = pattern.matcher(parts);
        return matcher.find() ? matcher.group(1) : null;
    }

    void cleanup() {
        uniforms.clear();
    }

}