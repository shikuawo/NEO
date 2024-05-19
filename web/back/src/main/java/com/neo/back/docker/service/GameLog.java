package com.neo.back.docker.service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.neo.back.docker.dto.UserSettingDto;
import com.neo.back.docker.middleware.DockerAPI;
import com.neo.back.docker.utility.MakeWebClient;
import com.neo.back.springjwt.entity.User;
import java.util.concurrent.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@Transactional
@RequiredArgsConstructor
public class GameLog {
    private final Map<User, SseEmitter> getUserAndSseEmitter = new ConcurrentHashMap<>();
    private final Map<User, ScheduledExecutorService> getuserAndSche = new ConcurrentHashMap<>();
    private Map<User, String> previousLogs = new ConcurrentHashMap<>();
    private final DockerAPI dockerAPI;
    private final MakeWebClient makeWebClient;
    private WebClient dockerWebClient;
    
    public SseEmitter sendLogContinue(User user){
            UserSettingDto UserSetting = dockerAPI.settingIDS(user);
            this.dockerWebClient = makeWebClient.makeDockerWebClient(UserSetting.getIp());

            SseEmitter emitter = getUserAndSseEmitter.get(user);
            ScheduledExecutorService executor = getuserAndSche.get(user);
            previousLogs.put(user, "");

            if (emitter != null || executor != null) {
                clear(user).run();
            }
            emitter = new SseEmitter();
            executor = Executors.newScheduledThreadPool(1);

            emitter.onCompletion(clear(user));
            emitter.onTimeout(clear(user));
            emitter.onError(e -> clear(user).run());

            getUserAndSseEmitter.put(user, emitter);
            getuserAndSche.put(user, executor);
            executor.scheduleAtFixedRate(sendLogSche(user,UserSetting), 0, 1, TimeUnit.SECONDS);
            

        return emitter;
    }

    private Runnable clear(User user) {
        return () -> { 
            SseEmitter emitter = getUserAndSseEmitter.get(user);
            ScheduledExecutorService executor = getuserAndSche.get(user);
            getUserAndSseEmitter.remove(user);
            getuserAndSche.remove(user);
            emitter.complete();
            executor.shutdown();
        };
    }

    private Runnable sendLogSche(User user, UserSettingDto UserSetting) {
        return () -> {
            try {
                getUserAndSseEmitter.get(user).send(SseEmitter.event().name("LiveCheck").data("Live"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.dockerAPI.MAKEexec("gameLog", UserSetting.getDockerId(), this.dockerWebClient)
            .subscribe(Log -> {
                try {
                    String previousLog = previousLogs.get(user);
                    if (!Log.equals(previousLog)) {
                        String newLog = Log.replace(previousLog, "");
                        getUserAndSseEmitter.get(user).send(SseEmitter.event().name("gameLogs").data(newLog));
                        previousLogs.put(user, Log);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        };
    }

}