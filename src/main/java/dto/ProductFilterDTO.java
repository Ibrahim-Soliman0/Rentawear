package dto;

import java.util.List;

public record ProductFilterDTO(
        String searchQuery,          // null = browse mode, present = search mode
        String gender,
        List<Integer> categoryIds,
        Double minPrice,
        Double maxPrice,
        Boolean newOnly,             // drives findNew path
        List<Integer> interestIds,   // drives findByInterests path
        int page,
        int pageSize
) {
    public int offset() {
        return page * pageSize;
    }

    public boolean isSearch() {
        return searchQuery != null && !searchQuery.isBlank();
    }

    public boolean isNewOnly() {
        return newOnly != null && newOnly;
    }

    public boolean isInterestBased() {
        return interestIds != null && !interestIds.isEmpty();
    }
}