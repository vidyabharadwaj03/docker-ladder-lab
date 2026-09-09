package dev.vbharadwaj.dockerladder.orders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class CatalogClient {

    private final RestTemplate restTemplate;
    private final String catalogBaseUrl;

    public CatalogClient(RestTemplate restTemplate,
                          @Value("${catalog.base-url}") String catalogBaseUrl) {
        this.restTemplate = restTemplate;
        this.catalogBaseUrl = catalogBaseUrl;
    }

    public Optional<ProductDto> findProduct(Long productId) {
        try {
            ProductDto product = restTemplate.getForObject(
                    catalogBaseUrl + "/products/{id}", ProductDto.class, productId);
            return Optional.ofNullable(product);
        } catch (RestClientException e) {
            return Optional.empty();
        }
    }
}
