package com.sassveterinaria.inventory.dto;

import java.util.List;

public record ServiceBomReplaceRequest(
    List<ServiceBomItemRequest> items
) {
}
