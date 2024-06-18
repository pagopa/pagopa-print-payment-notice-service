package it.gov.pagopa.payment.notices.service.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageInfo {

    @JsonProperty("page")
    Integer page;

    @JsonProperty("limit")
    Integer limit;

    @JsonProperty("items_found")
    @JsonAlias("itemsFound")
    Integer itemsFound;

    @JsonProperty("total_pages")
    @JsonAlias("totalPages")
    Integer totalPages;

    @JsonProperty("total_items")
    @JsonAlias("totalItems")
    Long totalItems;
}
