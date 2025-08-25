package systeme.exception;

public class ShaderCompilationException extends Exception {
    private final String shaderType;

    public ShaderCompilationException(String message) {
        super(message);
        this.shaderType = "Unknown";
    }

    public ShaderCompilationException(String message, String shaderType) {
        super(message);
        this.shaderType = shaderType;
    }

    public ShaderCompilationException(String message, Throwable cause) {
        super(message, cause);
        this.shaderType = "Unknown";
    }

    public ShaderCompilationException(String message, String shaderType, Throwable cause) {
        super(message, cause);
        this.shaderType = shaderType;
    }

    public String getShaderType() {
        return shaderType;
    }
}