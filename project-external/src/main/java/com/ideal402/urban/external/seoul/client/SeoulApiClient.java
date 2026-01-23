package com.ideal402.urban.external.seoul.client;

import com.ideal402.urban.external.seoul.dto.SeoulRealTimeDataResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SeoulApiClient {

    // RestClient 객체 생성
    private final RestClient restClient = RestClient.builder()
            .baseUrl("http://openapi.seoul.go.kr:8088")
            .build();

    // 변수 설정
    @Value("${seoul.api.key}")
    private String apiKey;
    String endpoint =  "/" + apiKey + "/json/citydata/1/5/광화문·덕수궁";

    // 데이터 요청
    public SeoulRealTimeDataResponse getSeoulData(){
        String placeName = "광화문·덕수궁";

        String path = "/%s/json/citydata/1/5/%s".formatted(apiKey, placeName);

        // 실제 호출 및 반환
        return restClient.get()
                .uri(path)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(SeoulRealTimeDataResponse.class);
    }

}
