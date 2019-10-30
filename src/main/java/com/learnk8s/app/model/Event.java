package com.learnk8s.app.model;

import lombok.*;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Document(collection = "events")
public class Event {

    @JsonIgnore
    private String _id;
    private String eventId;
    private String name;
    private Boolean consumed;
    private Long createTime;

}
