package dto;

import util.ColorUtil;

public class SwatchDTO {
    public String color;  // full encoded value - "#FFFFFF-White"
    public String hex;    // "#FFFFFF" - for CSS background-color
    public String name;   // "White" - for title attribute and labels
    public String slug;   // "white" - for JS image path construction

    public static SwatchDTO from(String encodedColor) {
        SwatchDTO s = new SwatchDTO();
        s.color = encodedColor;
        s.hex   = ColorUtil.hex(encodedColor);
        s.name  = ColorUtil.name(encodedColor);
        s.slug  = ColorUtil.slug(encodedColor);
        return s;
    }
}