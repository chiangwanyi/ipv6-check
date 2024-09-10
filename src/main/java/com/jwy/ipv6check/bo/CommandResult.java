package com.jwy.ipv6check.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandResult {
    private String output;
    private int exitCode;
}
