package com.jwy.ipv6check.job;

import com.jwy.ipv6check.bo.CommandResult;
import com.jwy.ipv6check.concurrent.IJob;
import com.jwy.ipv6check.util.CommandExecUtils;
import com.jwy.ipv6check.util.NetworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class Ipv6CheckJobHandler implements IJob<Void> {
    @Override
    public String getJobName() {
        return "ipv6_check_job@" + this.hashCode();
    }

    @Override
    public boolean isRetry() {
        return false;
    }

    @Override
    public int getRetryCount() {
        return 10;
    }

    @Override
    public Void execute() throws Exception {
        log.info("开始本机检查 IPv6 地址...");
        // 1. 先判断主机是 windows 还是 linux
        String osName = System.getProperty("os.name");
        log.info("当前操作系统为：{}", osName);

        CommandResult commandResult;
        if (osName.contains("Windows")) {
            commandResult = CommandExecUtils.executeCommand("ipconfig", "|", "findstr", "IPv6");
        } else if (osName.contains("Linux")) {
            // 2. ip -6 addr | grep inet6 | awk -F '[ \t]+|/' '$3 == "::1" { next;} $3 ~ /^fe80::/ { next;} /inet6/ {print $3}'
            commandResult = CommandExecUtils.executeCommand("ip -6 addr | grep inet6 | awk -F '[ \\t]+|/' '$3 == \"::1\" { next;} $3 ~ /^fe80::/ { next;} /inet6/ {print $3}'");
        } else {
            log.warn("未知的操作系统:{}，暂不支持", osName);
            return null;
        }

        if (commandResult.getExitCode() == 0) {
            List<String> addresses = NetworkUtils.extractIPv6Addresses(commandResult.getOutput());
            log.info("本机 IPv6 地址为：{}", addresses);
        } else {
            log.error("本机 IPv6 地址获取失败，错误信息为：{}", commandResult.getOutput());
        }
        return null;
    }
}
