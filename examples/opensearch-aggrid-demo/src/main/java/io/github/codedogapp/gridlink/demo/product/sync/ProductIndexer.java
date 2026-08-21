package io.github.codedogapp.gridlink.demo.product.sync;

import io.github.codedogapp.gridlink.demo.product.model.Product;
import io.github.codedogapp.gridlink.demo.product.model.ProductEntity;
import io.github.codedogapp.gridlink.demo.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

import java.util.List;


@RequiredArgsConstructor
@Slf4j
@Component
public class ProductIndexer implements ApplicationRunner {

    private static final int BATCH_SIZE = 500;

    private final ProductRepository repository;
    private final ElasticsearchOperations operations;

    @Override
    public void run(final @NonNull ApplicationArguments args) {
        final IndexOperations index = operations.indexOps(Product.class);
        if (index.exists()) {
            index.delete();
        }
        index.createWithMapping();

        final List<Product> documents = repository.findAll().stream()
            .map(ProductIndexer::toDocument)
            .toList();
        saveInBatches(documents);
        index.refresh();

        log.info("Mirrored {} products from SQLite into the OpenSearch index", documents.size());
    }

    private static Product toDocument(final ProductEntity entity) {
        return new Product(
            entity.getId(),
            entity.getName(),
            entity.getCategory(),
            entity.getPrice(),
            entity.getCreatedAt()
        );
    }

    private void saveInBatches(final List<Product> documents) {
        for (int from = 0; from < documents.size(); from += BATCH_SIZE) {
            final int to = Math.min(from + BATCH_SIZE, documents.size());
            operations.save(documents.subList(from, to));
        }
    }

}
