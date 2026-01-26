package com.ideal402.urban;

import com.ideal402.urban.api.dto.ForecastInfo;
import com.ideal402.urban.api.dto.MapInfo;
import com.ideal402.urban.common.GlobalExceptionHandler;
import com.ideal402.urban.common.ResourceNotFoundException;
import com.ideal402.urban.config.SecurityConfig;
import com.ideal402.urban.domain.repository.UserRepository;
import com.ideal402.urban.service.MapService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MapController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
public class MapApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MapService mapService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("Map data: 정상 요청 테스트")
    void getMapDataTest() throws Exception {

        MapInfo request = new MapInfo()
                .regionId(1)
                .congestionLevel(MapInfo.CongestionLevelEnum.NUMBER_1)
                .weatherCode(1)
                .airQualityIndex(MapInfo.AirQualityIndexEnum.NUMBER_1)
                .timestamp(OffsetDateTime.now());

        List<MapInfo> responseList = Collections.singletonList(request);

        given(mapService.getMapData(null)).willReturn(responseList);

        mockMvc.perform(get("/map/current").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].regionId").value(1))
                .andExpect(jsonPath("$[0].congestionLevel").value(1))
                .andExpect(jsonPath("$[0].weatherCode").value(1))
                .andExpect(jsonPath("$[0].airQualityIndex").value(1))
                .andDo(print());
    }

    @Test
    @DisplayName("Map Data: 특정 ID 받기 테스트")
    void getMapDataByIdTest() throws Exception {
        MapInfo request = new MapInfo()
                .regionId(1)
                .congestionLevel(MapInfo.CongestionLevelEnum.NUMBER_1)
                .weatherCode(1)
                .airQualityIndex(MapInfo.AirQualityIndexEnum.NUMBER_1)
                .timestamp(OffsetDateTime.now());

        List<MapInfo> responseList = Collections.singletonList(request);

        given(mapService.getMapData(1)).willReturn(responseList);

        mockMvc.perform(get("/map/current")
                        .param("regionId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].regionId").value(1))
                .andDo(print());
    }

    @Test
    @DisplayName("Map Data: 존재하지 않는 ID 테스트 - 404")
    void getMapDataByNonexistIdTest() throws Exception {

        given(mapService.getMapData(1000))
                .willThrow(new ResourceNotFoundException("존재하지않는 지역입니다."));

        mockMvc.perform(get("/map/current")
                        .param("regionId", "1000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Forecast Data: 정상 요청 테스트")
    void getForecastDataTest() throws Exception {

        ForecastInfo request = new ForecastInfo()
                .regionId(1)
                .congestionLevel(ForecastInfo.CongestionLevelEnum.NUMBER_1)
                .timestamp(OffsetDateTime.now());

        List<ForecastInfo> responseList = Collections.singletonList(request);

        given(mapService.getForecastData(null)).willReturn(responseList);

        mockMvc.perform(get("/map/forecast").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].regionId").value(1))
                .andExpect(jsonPath("$[0].congestionLevel").value(1))
                .andDo(print());
    }

    @Test
    @DisplayName("Forecast Data: 특정 ID 받기 테스트")
    void getForecastDataByIdTest() throws Exception {
        ForecastInfo request = new ForecastInfo()
                .regionId(1)
                .congestionLevel(ForecastInfo.CongestionLevelEnum.NUMBER_1)
                .timestamp(OffsetDateTime.now());

        List<ForecastInfo> responseList = Collections.singletonList(request);

        given(mapService.getForecastData(1)).willReturn(responseList);

        mockMvc.perform(get("/map/forecast")
                    .param("regionId", "1")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].regionId").value(1))
                .andDo(print());
    }

    @Test
    @DisplayName("Forecast Data: 존재하지 않는 ID 테스트 - 404")
    void getForecastDataByNonexistIdTest() throws Exception {
        given(mapService.getForecastData(1000))
                .willThrow(new ResourceNotFoundException("존재하지 않는 지역입니다."));

        mockMvc.perform(get("/map/forecast").param("regionId", "1000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Summery Data: 정상 요청 테스트")
    void getSummeryDataTest() throws Exception {

        MapInfo request = new MapInfo()
                .regionId(1)
                .congestionLevel(MapInfo.CongestionLevelEnum.NUMBER_1)
                .weatherCode(1)
                .airQualityIndex(MapInfo.AirQualityIndexEnum.NUMBER_1)
                .timestamp(OffsetDateTime.now());

        List<MapInfo> responseList = Collections.singletonList(request);

        given(mapService.getRegionSummary(1)).willReturn(responseList);

        mockMvc.perform(get("/map/summary/{regionId}", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].regionId").value(1))
                .andExpect(jsonPath("$.length()").value(1))
                .andDo(print());
    }

    @Test
    @DisplayName("Summery Data: 존재하지 않는 ID 테스트 - 404")
    void getSummeryDataByNonexistIdTest() throws Exception {
        given(mapService.getRegionSummary(1000))
                .willThrow(new ResourceNotFoundException("존재하지 않는 지역입니다."));

        mockMvc.perform(get("/map/summary/{regionId}",1000)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Summery Data: 타입 불일치 요청 - 400")
    void getSummeryDataBadRequestTest() throws Exception {

        mockMvc.perform(get("/map/summary/{regionId}", "invalid-id")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}
