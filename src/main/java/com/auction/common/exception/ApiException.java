package com.auction.common.exception;

import com.auction.common.apipayload.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiException extends RuntimeException {

    private final BaseCode errorCode;
}
