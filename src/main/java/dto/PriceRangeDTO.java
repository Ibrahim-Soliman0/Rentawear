package dto;

public class PriceRangeDTO {
    public Double min;
    public Double max;

    public PriceRangeDTO() {}

    public PriceRangeDTO(Double min, Double max) {
        this.min = min;
        this.max = max;
    }
}
