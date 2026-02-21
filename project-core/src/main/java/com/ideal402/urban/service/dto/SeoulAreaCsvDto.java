package com.ideal402.urban.service.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeoulAreaCsvDto {
    @CsvBindByName(column = "CATEGORY")
    private String category;

    @CsvBindByName(column = "AREA_CD")
    private String areaCode;

    @CsvBindByName(column = "AREA_NM")
    private String areaName;

    @CsvBindByName(column = "LAT")
    private Double latitude;

    @CsvBindByName(column = "LNG")
    private Double longitude;
}