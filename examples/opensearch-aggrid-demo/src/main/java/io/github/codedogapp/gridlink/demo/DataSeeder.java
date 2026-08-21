package io.github.codedogapp.gridlink.demo;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Recreates the {@code products} index (with the mapping + normalizer derived from {@link Product})
 * and seeds a small, varied catalogue on startup so the grid has something to filter and sort.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private final ElasticsearchOperations operations;

    public DataSeeder(final ElasticsearchOperations operations) {
        this.operations = operations;
    }

    @Override
    public void run(final @NonNull ApplicationArguments args) {
        final IndexOperations index = operations.indexOps(Product.class);
        if (index.exists()) {
            index.delete();
        }
        index.createWithMapping();
        operations.save(sampleProducts());
        index.refresh();
    }

    private List<Product> sampleProducts() {
        record Seed(String name, String category, double price) {
        }
        final List<Seed> seeds = List.of(
            new Seed("MacBook Pro 16", "Electronics", 2499.0),
            new Seed("iPhone 15 Pro", "Electronics", 1199.0),
            new Seed("Sony WH-1000XM5", "Electronics", 399.0),
            new Seed("Dell XPS 13", "Electronics", 1299.0),
            new Seed("iPad Air", "Electronics", 599.0),
            new Seed("Kindle Paperwhite", "Electronics", 149.0),
            new Seed("Clean Code", "Books", 39.0),
            new Seed("Effective Java", "Books", 45.0),
            new Seed("The Pragmatic Programmer", "Books", 42.0),
            new Seed("Designing Data-Intensive Applications", "Books", 55.0),
            new Seed("Refactoring", "Books", 47.0),
            new Seed("Domain-Driven Design", "Books", 52.0),
            new Seed("Running Shoes", "Sports", 120.0),
            new Seed("Yoga Mat", "Sports", 35.0),
            new Seed("Dumbbell Set", "Sports", 89.0),
            new Seed("Mountain Bike", "Sports", 850.0),
            new Seed("Tennis Racket", "Sports", 160.0),
            new Seed("Winter Jacket", "Clothing", 180.0),
            new Seed("Denim Jeans", "Clothing", 70.0),
            new Seed("Cotton T-Shirt", "Clothing", 25.0),
            new Seed("Wool Sweater", "Clothing", 95.0),
            new Seed("Leather Boots", "Clothing", 210.0),
            new Seed("Espresso Machine", "Home", 449.0),
            new Seed("Vacuum Cleaner", "Home", 299.0),
            new Seed("Air Fryer", "Home", 129.0),
            new Seed("Standing Desk", "Home", 379.0),
            new Seed("Office Chair", "Home", 249.0),
            new Seed("LEGO Millennium Falcon", "Toys", 159.0),
            new Seed("Rubik's Cube", "Toys", 15.0),
            new Seed("Board Game Catan", "Toys", 49.0),
            new Seed("RC Drone", "Toys", 299.0),
            new Seed("Puzzle 1000pc", "Toys", 22.0)
        );

        final LocalDateTime base = LocalDateTime.of(2023, 1, 5, 9, 0, 0);
        final List<Product> products = new ArrayList<>();
        for (int i = 0; i < seeds.size(); i++) {
            final Seed seed = seeds.get(i);
            final LocalDateTime createdAt = base.plusDays(i * 11L).plusHours(i % 7);
            products.add(new Product(
                UUID.randomUUID().toString(),
                seed.name(),
                seed.category(),
                seed.price(),
                createdAt
            ));
        }
        return products;
    }
}
