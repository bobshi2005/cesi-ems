package com.cesi.consumptionanalysis.domain.vo;

import lombok.Data;

/**
 * description todu
 *
 * @author cesi
 */
@Data
public class ProductEnergyAnalysisData {
    
    private String dateTime;
    private double productCount;
    private double energyCount;
    private double average;
}
