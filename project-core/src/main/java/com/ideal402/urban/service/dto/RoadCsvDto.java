package com.ideal402.urban.service.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoadCsvDto {

    @CsvBindByName(column = "link_id")
    private String linkId;

    @CsvBindByName(column = "road_nm")
    private String roadNm;

    @CsvBindByName(column = "start_nd_cd")
    private String startNdCd;

    @CsvBindByName(column = "start_nd_nm")
    private String startNdNm;

    @CsvBindByName(column = "start_lng")
    private BigDecimal startLng;

    @CsvBindByName(column = "start_lat")
    private BigDecimal startLat;

    @CsvBindByName(column = "end_nd_cd")
    private String endNdCd;

    @CsvBindByName(column = "end_nd_nm")
    private String endNdNm;

    @CsvBindByName(column = "end_lng")
    private BigDecimal endLng;

    @CsvBindByName(column = "end_lat")
    private BigDecimal endLat;

    @CsvBindByName(column = "dist")
    private BigDecimal dist;

    @CsvBindByName(column = "xy_list")
    private String xyList;

    @CsvBindByName(column = "h3_index")
    private String h3Index;
}