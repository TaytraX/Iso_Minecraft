package render.loader;

import systeme.filesystem.GameDirectoryManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private int programID;
    private int vertexShaderID;
    private int fragmentShaderID;
    private final GameDirectoryManager gameDirectoryManager = new GameDirectoryManager();

    private final Map<String, Integer> uniforms = new HashMap<>();

    public Shader(String shaderName) {

        try {
            if (!tryLoadFromShaderpack(shaderName)) {

                String sources = loadEmbeddedShader(shaderName);
                parseUniformsFromShader(sources);
            }
        }catch (Exception e) {
            System.err.println("Erreur lors du chargement des shaders: " + e.getMessage());
            loadDefaultShader();
        }
    }

    private Optional<String> getFirstShaderpackName() {
        try(var dirs = Files.list(gameDirectoryManager.getShaderpacksDirectory().toPath())) {
            return dirs
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .findFirst();
        }catch (IOException e) {
            return Optional.empty();
        }
    }

    private String loadEmbeddedShader(String shaderName) throws IOException {
        InputStream vertexInputStream = getClass().getResourceAsStream("/shaders/" + shaderName + ".vs.glsl");
        String vertexSource = vertexInputStream != null ? new String(vertexInputStream.readAllBytes()) : null;

        InputStream fragmentInputStream = getClass().getResourceAsStream("/shaders/" + shaderName + ".fs.glsl");
        String fragmentSource = fragmentInputStream != null ? new String(fragmentInputStream.readAllBytes()) : null;

        compile(vertexSource, fragmentSource);

        return vertexSource + "\n" + fragmentSource;
    }

    private boolean tryLoadFromShaderpack(String shaderName) throws IOException {
        String packName = String.valueOf(getFirstShaderpackName());
        if (packName == null) return false; // Aucun pack trouvé

        Path externalVertexPath = gameDirectoryManager.getShaderpacksDirectory().toPath().resolve(packName + '/' + shaderName + ".vs.glsl");
        Path externalFragmentPath = gameDirectoryManager.getShaderpacksDirectory().toPath().resolve(packName + '/' + shaderName + ".fs.glsl");

        if (Files.exists(externalFragmentPath) && Files.exists(externalVertexPath)) {
            String vertexSource = Files.readString(externalVertexPath);
            String fragmentSource = Files.readString(externalFragmentPath);

            compile(vertexSource, fragmentSource);
            return true;
        }
        return false;
    }

    public void compile(String vertexSource, String fragmentSource) {
        // Compiler le vertex shader
        vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShaderID, vertexSource);
        glCompileShader(vertexShaderID);

        if (glGetShaderi(vertexShaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Erreur vertex shader: " + glGetShaderInfoLog(vertexShaderID));
            return;
        }

        // Compiler le fragment shader
        fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShaderID, fragmentSource);
        glCompileShader(fragmentShaderID);

        if (glGetShaderi(fragmentShaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Erreur fragment shader: " + glGetShaderInfoLog(fragmentShaderID));
            return;
        }

        // Créer le programme
        programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        glLinkProgram(programID);

        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
            System.err.println("Erreur linking: " + glGetProgramInfoLog(programID));
            return;
        }

        glValidateProgram(programID);
        if (glGetProgrami(programID, GL_VALIDATE_STATUS) == GL_FALSE) {
            System.err.println("Erreur validation: " + glGetProgramInfoLog(programID));
        }
    }

    public void createUniform(String name) {
        int location = glGetUniformLocation(programID, name);
        if (location < 0) {
            System.err.println("Uniform not found: " + name);
        }
        uniforms.put(name, location);
    }

    private void loadDefaultShader() {
        // Crée un shader par défaut en hardcoded
        String defaultVertex = """
                #version 330 core
                in vec3 position;
                void main() { gl_Position = vec4(position, 1.0); }""";

        String defaultFragment = """
                #version 330 core
                out vec4 fragColor;
                void main() { fragColor = vec4(1.0, 0.0, 1.0, 1.0); }"""; // Rose shocking

        compile(defaultVertex, defaultFragment);
    }

    private void parseUniformsFromShader(String source) {
        // Parser les deux shaders
        parseUniformsFromSource(source);
    }

    private void parseUniformsFromSource(String shaderSource) {
        String[] lines = shaderSource.split("\n");

        for (String line : lines) {
            line = line.trim(); // Supprimer espaces

            // Chercher les lignes qui commencent par "uniform"
            if (line.startsWith("uniform") && !line.startsWith("//")) {
                String uniformName = extractUniformName(line);
                String uniformType = extractUniformType(line);
                if (uniformName != null) {
                    createUniform(uniformName);
                    System.out.println("Uniform détecté : " + uniformName);
                }
            }
        }
    }

    private String extractUniformType(String uniformLine) {
        // Exemple : "uniform mat4 transformationMatrix;"
        //           → extraire "transformationMatrix"

        String[] parts = uniformLine.split("\\s+"); // Split sur espaces

        if (parts.length > 2) {
            // parts[0] = "uniform"
            // parts[1] = "mat4" (type)
            // parts[2] = "transformationMatrix;" (nom avec ;)
            return parts[1];
        }
        return null;
    }

    private String extractUniformName(String uniformLine) {
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

    public void use() {
        glUseProgram(programID);
    }

    public void stop() {
        glUseProgram(0);
    }

    public void cleanup() {
        stop();
        uniforms.clear();
        glDetachShader(programID, vertexShaderID);
        glDetachShader(programID, fragmentShaderID);
        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);
        glDeleteProgram(programID);
    }
}