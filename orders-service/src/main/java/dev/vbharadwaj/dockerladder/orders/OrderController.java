package dev.vbharadwaj.dockerladder.orders;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository repository;
    private final CatalogClient catalogClient;

    public OrderController(OrderRepository repository, CatalogClient catalogClient) {
        this.repository = repository;
        this.catalogClient = catalogClient;
    }

    @GetMapping
    public List<Order> all() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Order one(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        ProductDto product = catalogClient.findProduct(order.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Unknown product id " + order.getProductId()));

        if (product.stock() < order.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock");
        }

        order.setStatus("CONFIRMED");
        Order saved = repository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
