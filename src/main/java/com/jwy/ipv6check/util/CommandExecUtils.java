package com.jwy.ipv6check.util;

import com.jwy.ipv6check.bo.CommandResult;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CommandExecUtils {
    private CommandExecUtils() {
    }

    public static CommandResult executeCommand(String... command) {
        List<String> commandList = new ArrayList<>();
        String osName = System.getProperty("os.name");

        if (osName.contains("Windows")) {
            commandList.add("cmd");
            commandList.add("/c");
        }else if (osName.contains("Linux")){
            commandList.add("/bin/bash");
            commandList.add("-c");
        }

        commandList.addAll(List.of(command));

        ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        StringBuilder output = new StringBuilder();
        int exitCode = -1;

        try {
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            log.error("执行命令:{}时报错:", commandList, e);
            return new CommandResult(e.getMessage(), exitCode);
        }

        return new CommandResult(output.toString(), exitCode);
    }
}
