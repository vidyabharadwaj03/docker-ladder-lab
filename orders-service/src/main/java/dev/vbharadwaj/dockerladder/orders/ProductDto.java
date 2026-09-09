package dev.vbharadwaj.dockerladder.orders;

import java.math.BigDecimal;

public record ProductDto(Long id, String name, BigDecimal price, Integer stock) {
}
