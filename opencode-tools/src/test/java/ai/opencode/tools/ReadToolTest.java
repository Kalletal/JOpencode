package ai.opencode.tools;

import ai.opencode.storage.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ReadToolTest {
    private ReadTool readTool;
    private AppConfig mockConfig;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        readTool = new ReadTool();
        mockConfig = AppConfig.createDefault();
    }

    @Test
    void testReadFileSuccess() throws IOException, ToolException {
        Path filePath = tempDir.resolve("test.txt");
        String content = "Hello opencode!";
        Files.writeString(filePath, content);

        ToolRequest request = new ToolRequest(Map.of("path", filePath.toString()), "Read test file");
        ToolResponse response = readTool.execute(request, mockConfig);

        assertEquals(content, response.output());
    }

    @Test
    void testReadNonExistentFile() {
        ToolRequest request = new ToolRequest(Map.of("path", "non_existent_file.txt"), "Read missing file");
        
        assertThrows(ToolException.class, () -> {
            readTool.execute(request, mockConfig);
        });
    }

    @Test
    void testReadDirectory() throws IOException, ToolException {
        Files.createDirectory(tempDir.resolve("dir1"));
        Files.createFile(tempDir.resolve("dir1/file1.txt"));

        ToolRequest request = new ToolRequest(Map.of("path", tempDir.resolve("dir1").toString()), "Read directory");
        ToolResponse response = readTool.execute(request, mockConfig);

        assertTrue(response.output().contains("file1.txt"));
    }
}
