package systeme.exception;

public class ShaderCompilationException extends Exception {
    private final String shaderType;
    private final String shaderSource;
    private final String originalError;

    public ShaderCompilationException(String message, String shaderType, String shaderSource) {
        super(message);
        this.shaderType = shaderType;
        this.shaderSource = shaderSource;
        this.originalError = message;
    }

    public String getShaderType() {
        return shaderType;
    }

    public String getShaderSource() {
        return shaderSource;
    }

    public String getOriginalError() {
        return originalError;
    }
}