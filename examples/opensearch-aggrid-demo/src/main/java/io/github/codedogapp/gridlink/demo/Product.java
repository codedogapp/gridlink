package io.github.codedogapp.gridlink.demo;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;

/**
 * A product document stored in OpenSearch.
 *
 * <p>{@code name} and {@code category} are keyword fields with a lowercase normalizer so that the
 * case-insensitive, lowercased term/wildcard queries produced by the gridlink adapter match
 * regardless of the stored casing. {@code createdAt} uses the {@code date_hour_minute_second}
 * format that gridlink emits for date range bounds.
 */
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
