package io.github.codedogapp.gridlink.demo.product.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;


@Document(indexName = "products")
@Setting(settingPath = "/opensearch/product-settings.json")
public record Product(

    @Id
    String id,

    @Field(type = FieldType.Keyword, normalizer = "lowercase_normalizer")
    String name,

    @Field(type = FieldType.Keyword, normalizer = "lowercase_normalizer")
    String category,

    @Field(type = FieldType.Double)
    Double price,

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    LocalDateTime createdAt

) {

}
