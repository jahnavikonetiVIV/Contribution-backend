package com.jahnavi.contribution.contribution_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class S3EventNotificationDto {

    @JsonProperty("Records")
    private List<Record> records = Collections.emptyList();

    @Data
    public static class Record {
        private S3 s3;

        @Data
        public static class S3 {
            private Bucket bucket;
            private Object object;

            @Data
            public static class Bucket {
                private String name;
            }

            @Data
            public static class Object {
                private String key;
            }
        }
    }
}
