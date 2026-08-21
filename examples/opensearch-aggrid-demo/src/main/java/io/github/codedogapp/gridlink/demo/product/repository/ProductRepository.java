package io.github.codedogapp.gridlink.demo.product.repository;

import io.github.codedogapp.gridlink.demo.product.model.ProductEntity;

import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductRepository extends JpaRepository<ProductEntity, String> {
}
