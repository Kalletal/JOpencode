package ai.opencode.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class ToolRegistryTest {
    private ToolRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ToolRegistry();
    }

    @Test
    void testRegisterAndGetTool() {
        Tool mockTool = new Tool() {
            @Override public String getName() { return "test-tool"; }
            @Override public String getDescription() { return "Desc"; }
            @Override public ToolResponse execute(ToolRequest request, ai.opencode.storage.AppConfig config) {
                return new ToolResponse("OK", new HashMap<>(), null);
            }
        };

        registry.registerTool(mockTool);
        assertEquals(mockTool, registry.getTool("test-tool"));
    }

    @Test
    void testGetNonExistentTool() {
        assertNull(registry.getTool("unknown"));
    }
}
