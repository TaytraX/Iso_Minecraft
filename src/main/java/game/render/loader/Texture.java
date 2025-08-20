package game.render.loader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import systeme.filesystem.GameDirectoryManager;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Texture {

    int textureID;
    int width, height;
    private final GameDirectoryManager gameDirectoryManager;
    private final String[] extensions = {".png", ".jpg", ".jpeg"};

    public Texture(String filename) throws Exception {
        gameDirectoryManager = new GameDirectoryManager();

        if (!tryLoadFromTexturePack(filename)) {
            textureID = tryLoadEmbeddedTexture(filename);

            if (textureID == 0) {
                textureID = createDefaultTexture();
            }
        }

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private boolean tryLoadFromTexturePack(String textureName) throws Exception {
        Optional<String> packOpt = getFirstTexturPackName();

        if (packOpt.isEmpty()) return false;
        String packName = packOpt.get();

        for (String ext : extensions) {
            Path texturePath = gameDirectoryManager.getTexturePacksDirectory().toPath()
                    .resolve(packName + "/" + textureName + ext);

            if (Files.exists(texturePath)) {
                textureID = loadTextureFromFile(texturePath.toString());
                return true;
            }
        }
        return false;
    }

    private int loadTextureFromFile(String filepath) throws Exception {
        ByteBuffer buffer;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            // STBImage charge directement depuis le fichier
            buffer = STBImage.stbi_load(filepath, w, h, comp, 4);
            if (buffer == null) {
                throw new Exception("Could not load file " + filepath + " " + STBImage.stbi_failure_reason());
            }

            this.width = w.get();
            this.height = h.get();
        }

        // Création texture OpenGL (même code qu'avant)
        int textureID = GL11.glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL11.GL_RGBA, this.width, this.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        STBImage.stbi_image_free(buffer);
        return textureID;
    }

    private Optional<String> getFirstTexturPackName() {
        try(var dirs = Files.list(gameDirectoryManager.getTexturePacksDirectory().toPath())) {
            return dirs
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .findFirst();
        }catch (IOException e) {
            return Optional.empty();
        }
    }

    private int tryLoadEmbeddedTexture(String filename) {
        for (String ext : extensions) {
            try {
                InputStream inputStream = getClass().getResourceAsStream("/textures/" + filename + ext);

                if (inputStream != null) {

                    int loadedTextureID = loadTextureFromStream(inputStream);
                    inputStream.close();
                    return loadedTextureID;
                }
            } catch (Exception e) {
                // Continue avec l'extension suivante
                System.err.println("Erreur lors du chargement de " + filename + ext + ": " + e.getMessage());
            }
        }
        return 0;
    }

    // Pour les ressources embarquées (InputStream)
    private int loadTextureFromStream(InputStream inputStream) throws Exception {
        ByteBuffer imageBuffer;
        ByteBuffer textureBuffer;
        int textureID;

        // Lire InputStream en byte[]
        byte[] imageData = inputStream.readAllBytes();

        // Allouer dans le heap natif (pas la stack !)
        imageBuffer = MemoryUtil.memAlloc(imageData.length);
        imageBuffer.put(imageData);
        imageBuffer.flip();

        // Allouer les IntBuffer sur la stack (ça c’est ok)
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            // Charger avec STBImage
            textureBuffer = STBImage.stbi_load_from_memory(imageBuffer, w, h, comp, 4);
            if (textureBuffer == null) {
                MemoryUtil.memFree(imageBuffer);
                throw new Exception("Could not load from memory: " + STBImage.stbi_failure_reason());
            }

            this.width = w.get();
            this.height = h.get();
        }

        // Libérer l’image brute (on n’en a plus besoin)
        MemoryUtil.memFree(imageBuffer);

        // Génération texture OpenGL
        textureID = GL11.glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL11.GL_RGBA, this.width, this.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureBuffer);

        // Filtrage
        GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        // Libérer textureBuffer de stb_image
        STBImage.stbi_image_free(textureBuffer);

        return textureID;
    }


    public int createDefaultTexture() {
        // Texture 2x2 pixels blancs
        ByteBuffer data = org.lwjgl.BufferUtils.createByteBuffer(16);
        for (int i = 0; i < 16; i++) {
            data.put((byte) 255); // Blanc opaque
        }
        data.flip();

        int textureID = GL11.glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL11.GL_RGBA, 2, 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);

        GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        this.width = 2;
        this.height = 2;

        return textureID;
    }

    public void cleanUp(){
        GL11.glDeleteTextures(textureID);
    }

    public int getTextureID() {
        return textureID;
    }
}