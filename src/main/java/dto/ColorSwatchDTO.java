package dto;

public record ColorSwatchDTO(
        String color, // full encoded value - "#FFFFFF-White"
        String hex,  // "#FFFFFF" - for CSS background-color
        String name,  // "White" - for title attribute and labels
        String slug // "white" - for JS image path construction
         ){

}
