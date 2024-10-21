package com.auction.common.apipayload;

import com.auction.common.apipayload.status.SuccessStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"success", "statusCode", "message", "data"})
public class ApiResponse<T> {

    @JsonProperty("success")
    private final Boolean success;

    private final String statusCode;

    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, SuccessStatus._OK.getStatusCode(), SuccessStatus._OK.getMessage(), data);
    }

    public static ApiResponse<String> fail(BaseCode errorCode) {
        return new ApiResponse<>(false, errorCode.getReasonHttpStatus().getStatusCode(), errorCode.getReasonHttpStatus().getMessage(), null);
    }
}
