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

    /**
     * 특정 지역명을 파라미터로 받아 실시간 데이터를 요청합니다.
     * @param areaName DB(Region 테이블)에서 조회한 지역명 (예: "강남역", "광화문·덕수궁")
     */
    public SeoulRealTimeDataResponse getSeoulData(String areaName){
        String placeName = "광화문·덕수궁";

        String path = "/%s/json/citydata/1/5/%s".formatted(apiKey, placeName);

        // 실제 호출 및 반환
        return restClient.get()
                .uri("/{apiKey}/json/citydata/1/5/{areaName}", apiKey, areaName)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(SeoulRealTimeDataResponse.class);
    }

}
