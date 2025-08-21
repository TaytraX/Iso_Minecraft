package game.render.loader;

import systeme.exception.ShaderCompilationException;
import systeme.filesystem.GameDirectoryManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    public int programID;
    private int vertexShaderID;
    private int fragmentShaderID;
    private final GameDirectoryManager gameDirectoryManager;
    private final UniformManager uniforms;
    private String sources;

    public Shader(String shaderName) throws ShaderCompilationException {
        gameDirectoryManager = new GameDirectoryManager();
        uniforms = new UniformManager(programID);

        try {
            if (!tryLoadFromShaderpack(shaderName)) {
                loadEmbeddedShader(shaderName);
            }

        } catch (NullPointerException e) {
            System.err.println("Shader '" + shaderName + "' introuvable. " + e.getMessage());
            System.out.println("Chargement du shader par défaut...");
            loadDefaultShader();
        } catch (IOException e) {
            System.err.println("Erreur de fichier shader '" + shaderName + "': " + e.getMessage());
            System.out.println("Chargement du shader par défaut...");
            loadDefaultShader();
        } catch (ShaderCompilationException e) {
            System.err.println("=== SHADER ERROR ===");
            System.err.println("Type: " + e.getShaderType());
            System.err.println("Error: " + e.getMessage());

            loadDefaultShader();
        } catch (RuntimeException e) {
            System.err.println("Erreur OpenGL shader '" + shaderName + "': " + e.getMessage());
            System.out.println("Chargement du shader par défaut...");
            loadDefaultShader();
        } catch (Exception e) { // Fallback pour le reste
            System.err.println("Erreur inattendue shader '" + shaderName + "': " + e.getMessage());
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

    private void loadEmbeddedShader(String shaderName) throws IOException, ShaderCompilationException {
        String vertexSource;
        String fragmentSource;

        // Try-with-resources pour auto-close
        try (InputStream vertexInputStream = getClass().getResourceAsStream("/shaders/" + shaderName + ".vs.glsl")) {
            if (vertexInputStream == null) {
                throw new IOException("Vertex shader file not found: " + shaderName + ".vs.glsl");
            }
            vertexSource = new String(vertexInputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        try (InputStream fragmentInputStream = getClass().getResourceAsStream("/shaders/" + shaderName + ".fs.glsl")) {
            if (fragmentInputStream == null) {
                throw new IOException("Fragment shader file not found: " + shaderName + ".fs.glsl");
            }
            fragmentSource = new String(fragmentInputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        sources = vertexSource + "\n" + fragmentSource;
        uniforms.parseUniformsFromSource(sources);
        compile(vertexSource, fragmentSource); // Peut throw ShaderCompilationException
    }

    private boolean tryLoadFromShaderpack(String shaderName) throws IOException, ShaderCompilationException {
        Optional<String> packOpt = getFirstShaderpackName();

        if (packOpt.isEmpty()) return false;
        String packName = packOpt.get(); // Aucun pack trouvé

        Path externalVertexPath = gameDirectoryManager.getShaderpacksDirectory().toPath().resolve(packName + '/' + shaderName + ".vs.glsl");
        Path externalFragmentPath = gameDirectoryManager.getShaderpacksDirectory().toPath().resolve(packName + '/' + shaderName + ".fs.glsl");

        if (Files.exists(externalFragmentPath) && Files.exists(externalVertexPath)) {
            String vertexSource = Files.readString(externalVertexPath);
            String fragmentSource = Files.readString(externalFragmentPath);

            compile(vertexSource, fragmentSource);
            uniforms.parseUniformsFromSource(vertexSource + "\n" + fragmentSource);
            return true;
        }
        return false;
    }

    public void compile(String vertexSource, String fragmentSource) {
        try {
            // Compiler le vertex shader
            vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
            glShaderSource(vertexShaderID, vertexSource);
            glCompileShader(vertexShaderID);

            if (glGetShaderi(vertexShaderID, GL_COMPILE_STATUS) == GL_FALSE) {
                System.err.println("Erreur vertex shader: " + glGetShaderInfoLog(vertexShaderID));
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            // Compiler le fragment shader
            fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
            glShaderSource(fragmentShaderID, fragmentSource);
            glCompileShader(fragmentShaderID);

            if (glGetShaderi(fragmentShaderID, GL_COMPILE_STATUS) == GL_FALSE) {
                System.err.println("Erreur fragment shader: " + glGetShaderInfoLog(fragmentShaderID));
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    private void loadDefaultShader() {
        // Crée un shader par défaut en hardcoded
        String defaultVertex = """
                #version 330 core
                in vec3 position;
                void main() { gl_Position = vec4(position, 1.0); }""";

        String defaultFragment = """
                #version 330 core
                out vec4 fragColor;
                void main() { fragColor = vec4(1.0, 1.0, 1.0, 1.0); }""";

        sources = defaultVertex + "\n" + defaultFragment;// Rose shocking

        uniforms.parseUniformsFromSource(sources);
        compile(defaultVertex, defaultFragment);
    }

    public UniformManager getUniforms() {
        return uniforms;
    }

    public void use() {
        glUseProgram(programID);
    }

    public void stop() {
        glUseProgram(0);
    }

    public void cleanup() {
        stop();
        uniforms.cleanup();
        glDetachShader(programID, vertexShaderID);
        glDetachShader(programID, fragmentShaderID);
        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);
        glDeleteProgram(programID);
    }
}