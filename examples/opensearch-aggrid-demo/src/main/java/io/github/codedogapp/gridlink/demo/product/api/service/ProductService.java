package io.github.codedogapp.gridlink.demo.product.api.service;

import io.github.codedogapp.gridlink.core.grid.GridRequest;
import io.github.codedogapp.gridlink.core.grid.GridResponse;
import io.github.codedogapp.gridlink.demo.product.model.Product;
import io.github.codedogapp.gridlink.demo.product.api.dto.ProductFilterModel;
import io.github.codedogapp.gridlink.elasticsearch.ElasticsearchQueries;

import lombok.RequiredArgsConstructor;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;


@RequiredArgsConstructor
@Service
public class ProductService {

    private final ElasticsearchOperations operations;

    public GridResponse<Product> query(final GridRequest<ProductFilterModel> request) {
        final SearchHits<Product> hits = operations.search(ElasticsearchQueries.toQuery(request), Product.class);
        final List<Product> rows = hits.getSearchHits().stream().map(SearchHit::getContent).toList();
        return new GridResponse<>(rows, hits.getTotalHits());
    }

}
