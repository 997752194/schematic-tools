package de.eldoria.schematictools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.eldoria.schematictools.configuration.elements.Tool;

public class ToolJacksonTest {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        // New format with multi-command list
        String yaml = "commandType: CONSOLE\n"
                + "command: \"say legacy\"\n"
                + "commands:\n"
                + "- op: \"say 第一条\"\n"
                + "- player: \"say 第二条\"\n";

        Tool tool = mapper.readValue(yaml, Tool.class);
        System.out.println("Parsed commands entries = " + tool.commands().size());
        System.out.println("hasCommand() = " + tool.hasCommand());
        System.out.println("hasCommands() = " + tool.hasCommands());
        for (var entry : tool.commands()) {
            System.out.println("  entry = " + entry);
        }

        // Round-trip: serialize back to YAML
        String out = mapper.writeValueAsString(tool);
        System.out.println("--- serialized ---");
        System.out.println(out);

        // Re-parse serialized output
        Tool reParsed = mapper.readValue(out, Tool.class);
        System.out.println("re-parsed entries = " + reParsed.commands().size());

        // Old format without commands should still work
        Tool old = mapper.readValue("commandType: CONSOLE\ncommand: \"say hi\"\n", Tool.class);
        System.out.println("legacy hasCommand() = " + old.hasCommand() + ", hasCommands() = " + old.hasCommands());
    }
}
