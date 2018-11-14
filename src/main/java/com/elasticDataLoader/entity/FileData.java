package com.elasticDataLoader.entity;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "filedatafrequency", type = "fileData")
public class FileData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Field(type= FieldType.Long, index = true)
    private Long id;

    @Field(type= FieldType.Long, index = true)
    private Long timestampInEpoch;

    @Field(type= FieldType.Long, index = true)
    private Long auditTime;

    @Field(type= FieldType.Text, index = true)
    private String content;

}
