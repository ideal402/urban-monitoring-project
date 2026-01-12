//package com.ideal402.urdan;
//
//import com.ideal402.urban.service.MapService;
//import com.ideal402.urban.api.dto.MapInfo;
//import com.ideal402.urban.domain.entity.RegionStatus;
//import com.ideal402.urban.domain.repository.RegionStatusRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//
//import java.time.OffsetDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.verify;
//
//@ExtendWith(MockitoExtension.class)
//public class MapServiceTest {
//
//    @InjectMocks
//    private MapService mapService;
//
//    @Mock
//    private RegionStatusRepository regionStatusRepository;
//
//    @Test
//    @DisplayName("")
//    void getMapData_all_test() {
//        RegionStatus info1 = new RegionStatus(1L, 1L, 1L, 1L, OffsetDateTime.now());
//
//        List<RegionStatus> mockEntities = List.of(info1,info1);
//
//        given(regionStatusRepository.findAll()).willReturn(mockEntities);
//
//        List<MapInfo> result = mapService.getMapData();
//
//        assertThat(result).hasSize(2);
//        assertThat(result.getFirst().getRegionId()).isEqualTo(info1.getRegionId());
//    }
//
//}
