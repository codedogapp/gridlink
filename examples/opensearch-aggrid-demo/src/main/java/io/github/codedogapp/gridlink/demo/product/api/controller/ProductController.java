package io.github.codedogapp.gridlink.demo.product.api.controller;

import io.github.codedogapp.gridlink.core.grid.GridRequest;
import io.github.codedogapp.gridlink.core.grid.GridResponse;
import io.github.codedogapp.gridlink.demo.Product;
import io.github.codedogapp.gridlink.demo.product.api.dto.ProductFilterModel;
import io.github.codedogapp.gridlink.demo.product.api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RequestMapping("/api/products")
@RestController
public class ProductController {

    private final ProductService productService;

    @PostMapping("/query")
    public GridResponse<Product> query(@RequestBody final GridRequest<ProductFilterModel> request) {
        return productService.query(request);
    }

}
