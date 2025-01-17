package com.neo.back.service.service;

import com.neo.back.service.dto.GameServerRunDto;
import com.neo.back.service.dto.MyServerInfoDto;
import com.neo.back.service.dto.ServerInputDto;
import com.neo.back.service.dto.UserSettingDto;
import com.neo.back.service.entity.DockerServer;
import com.neo.back.service.entity.GameTag;
import com.neo.back.service.exception.DoNotHaveServerException;
import com.neo.back.service.middleware.DockerAPI;
import com.neo.back.service.repository.DockerServerRepository;
import com.neo.back.service.repository.GameDockerAPICMDRepository;
import com.neo.back.service.repository.GameTagRepository;
import com.neo.back.service.utility.MakeWebClient;
import com.neo.back.authorization.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OtherServerManagingService {
    private final DockerServerRepository dockerServerRepo;
    private final GameTagRepository gameTagRepo;
    private final DockerAPI dockerAPI;
    private final MakeWebClient makeWebClient;
    private final GameDockerAPICMDRepository gameDockerAPICMDRepo;
    private WebClient dockerWebClient;
    
    public Mono<Object> getServerInfo (User user) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(user);
            if (dockerServer == null) throw new DoNotHaveServerException();

            MyServerInfoDto serverInfo = new MyServerInfoDto(
                    dockerServer.getServerName(),
                    dockerServer.getEdgeServer().getExternalIp(),
                    dockerServer.getPort(),
                    dockerServer.getGame().getGameName(),
                    dockerServer.getGame().getVersion(),
                    dockerServer.getRAMCapacity(),
                    dockerServer.isPublic(),
                    dockerServer.isFreeAccess(),
                    dockerServer.getServerComment()
            );

            return Mono.just(serverInfo);
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        }
    }

    public  Mono<Object> setPublic (User user) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(user);
            if (dockerServer == null) throw new DoNotHaveServerException();

            dockerServer.setPublic(!dockerServer.isPublic());
            dockerServerRepo.save(dockerServer);
            return  Mono.just(dockerServer.isPublic());
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        }
    }

    public  Mono<Object> setFreeAccess (User user) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(user);
            if (dockerServer == null) throw new DoNotHaveServerException();

            dockerServer.setFreeAccess(!dockerServer.isFreeAccess());
            dockerServerRepo.save(dockerServer);
            return  Mono.just(dockerServer.isFreeAccess());
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        }
    }

    public  Mono<Object> setComment (User user, String comment) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(user);
            if (dockerServer == null) throw new DoNotHaveServerException();

            dockerServer.setServerComment(comment);
            dockerServerRepo.save(dockerServer);
            return Mono.just("success set comment");
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        }
    }

    @Transactional
    public  Mono<Object> setTags (User user, List<String> tags) {
        try {
            DockerServer dockerServer = dockerServerRepo.findByUser(user);
            if (dockerServer == null) throw new DoNotHaveServerException();
            dockerServer.getGameTagNames()
            .forEach(tag ->{
                dockerServer.removeGameTag(gameTagRepo.findByTag(tag));
            });
            tags.forEach(tag ->{
                if(gameTagRepo.findByTag(tag) == null){
                    GameTag game = new GameTag();
                    game.setTag(tag);
                    gameTagRepo.save(game);
                    dockerServer.addGameTag(game);
                }
                else{
                    dockerServer.addGameTag(gameTagRepo.findByTag(tag));
                }
            });
            dockerServerRepo.save(dockerServer);
            return Mono.just("success set setTags");
        } catch (DoNotHaveServerException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server"));
        }
    }

    public Mono<String> sendInputToServer(User user, ServerInputDto input) {
        UserSettingDto UserSetting = dockerAPI.settingIDS(user);
        this.dockerWebClient = makeWebClient.makeDockerWebClient(UserSetting.getIp());
        return this.dockerAPI.MAKEexec("input", UserSetting.getDockerId(), this.dockerWebClient,"INPUT",input.getInput())
        .then(Mono.defer(() -> {
            return Mono.just("Input Success");
        }))
        .onErrorResume(error -> { // 값이 없음
            return Mono.just("Error : " + error.getMessage());
        });
    }

    public Mono<GameServerRunDto> getServerRunning(User user) {
        UserSettingDto UserSetting = this.dockerAPI.settingIDS(user);
        this.dockerWebClient = makeWebClient.makeDockerWebClient(UserSetting.getIp());
        // 일단 이렇게 하고 후에 다대다 관계를 성립시켜야함
        String searchString = "/server/craftbukkit-";
        return this.dockerAPI.MAKEexec("running_mine", UserSetting.getDockerId(), this.dockerWebClient)
        .flatMap(response -> {
            System.out.println(response);
            int count = 0;
            int index = 0;
    
            // 반복문을 통해 검색 문자열의 위치를 찾음
            while ((index = response.indexOf(searchString, index)) != -1) {
                count++;
                index += searchString.length(); // 검색 문자열의 길이만큼 인덱스를 증가시켜서 중복 카운트를 방지
            }
            GameServerRunDto run = new GameServerRunDto();
            if (count == 3){ // 3의 값은 현재 프로세스가 돌고 있다 간주, 아니면 안돌고 있음
                run.setIsWorking(true);
            }
            else{
                run.setIsWorking(false);
            }
            return Mono.just(run);
        })
        .onErrorResume(error -> { // 값이 없음
            return Mono.just(new GameServerRunDto(false));
        });
    }
}
