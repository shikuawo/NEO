package com.neo.back.docker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServerListDto {
    Long dockerNum;
    String serverName;
    String hostName;
    String gameName;
    String version;
    boolean isFreeAccess;
}