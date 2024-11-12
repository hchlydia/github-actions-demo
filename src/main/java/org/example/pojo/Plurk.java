package org.example.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Plurk {

    private Long plurk_id;
    private String posted;
    private String content;
    private Integer response_count;
    private Integer favorite_count;

    private List<Response> responses;
}
