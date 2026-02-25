package com.ideal402.urban.external.seoul.client;

import com.ideal402.urban.external.seoul.dto.SeoulRealTimeDataResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SeoulApiClient {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("http://openapi.seoul.go.kr:8088")
            .build();

    @Value("${seoul.api.key}")
    private String apiKey;

    /**
     * 특정 지역명을 파라미터로 받아 실시간 데이터를 요청합니다.
     * @param areaName DB(Region 테이블)에서 조회한 지역명 (예: "강남역", "광화문·덕수궁")
     */
    public SeoulRealTimeDataResponse getSeoulData(String areaName){
        return restClient.get()
                .uri("/{apiKey}/json/citydata/1/5/{areaName}", apiKey, areaName)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(SeoulRealTimeDataResponse.class);
    }

}
