package com.zsl_birdid.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ServerInfoController {

    @Value("${SERVER_IP:localhost}")
    private String serverIp;

    @Value("${SERVER_PORT:8080}")
    private String serverPort;

    @GetMapping("/server-info")
    public ServerInfo getServerInfo() {
        return new ServerInfo(serverIp, serverPort);
    }

    @Getter
    @Setter
    public static class ServerInfo {
        private String ip;
        private String port;

        public ServerInfo(String ip, String port) {
            this.ip = ip;
            this.port = port;
        }

    }
}
