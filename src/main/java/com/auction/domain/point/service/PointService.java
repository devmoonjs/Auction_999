package com.auction.domain.point.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.domain.payment.entity.Payment;
import com.auction.domain.payment.service.PaymentService;
import com.auction.domain.point.dto.request.ConvertRequestDto;
import com.auction.domain.point.dto.response.ChargeResponseDto;
import com.auction.domain.point.dto.response.ConvertResponseDto;
import com.auction.domain.point.entity.Point;
import com.auction.domain.point.repository.PointRepository;
import com.auction.domain.pointHistory.enums.PaymentType;
import com.auction.domain.pointHistory.service.PointHistoryService;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {
    @Value("${payment.secret.key}")
    private String API_SECRET_KEY;
    private final PointRepository pointRepository;
    private final PaymentService paymentService;
    private final PointHistoryService pointHistoryService;

    @Transactional
    public ChargeResponseDto confirmPayment(String jsonBody) throws IOException {
        JSONObject response = sendRequest(parseRequestData(jsonBody), API_SECRET_KEY, "https://api.tosspayments.com/v1/payments/confirm");
        if (response.containsKey("error")) {
            throw new ApiException(ErrorStatus._INVALID_PAY_REQUEST);
        }

        String orderId = response.get("orderId").toString();
        Payment payment = paymentService.getPayment(orderId);
        User user = payment.getUser();

        // point history 생성 및 저장
        pointHistoryService.createPointHistory(user, payment.getAmount(), PaymentType.CHARGE);

        // point 보유량 변화
        Point point = pointRepository.findByUser(user).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_USER));
        point.addPoint(payment.getAmount());

        return new ChargeResponseDto(payment.getAmount(), point.getPointAmount());
    }

    @Transactional
    public void createPoint(User user) {
        Point point = new Point(0, user);
        pointRepository.save(point);
    }

    @Transactional
    public ConvertResponseDto convertPoint(AuthUser authUser, ConvertRequestDto convertRequestDto) {
        User user = User.fromAuthUser(authUser);
        Point point = pointRepository.findByUser(user).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_USER));
        if (convertRequestDto.getAmount() > point.getPointAmount()) {
            throw new ApiException(ErrorStatus._INVALID_CONVERT_REQUEST);
        }

        // 계좌 정보로 이체하는 로직 있는 자리

        // point history 생성 및 저장
        pointHistoryService.createPointHistory(user, convertRequestDto.getAmount(), PaymentType.TRANSFER);

        // point 보유량 변화
        point.addPoint(convertRequestDto.getAmount());

        return new ConvertResponseDto(convertRequestDto.getAmount(), point.getPointAmount());
    }

    private Point getPoint(long userId) {
        return pointRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus._INVALID_REQUEST));
    }

    @Transactional
    public void decreasePoint(long userId, int price) {
        Point point = getPoint(userId);
        point.minusPoint(price);
        pointRepository.save(point);
    }

    @Transactional
    public void increasePoint(long userId, int price) {
        Point point = getPoint(userId);
        point.addPoint(price);
        pointRepository.save(point);
    }

    private JSONObject sendRequest(JSONObject requestData, String secretKey, String urlString) throws IOException {
        HttpURLConnection connection = createConnection(secretKey, urlString);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestData.toString().getBytes(StandardCharsets.UTF_8));
        }

        try (InputStream responseStream = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
             Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
            return (JSONObject) new JSONParser().parse(reader);
        } catch (Exception e) {
            log.error("Error reading response", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "Error reading response");
            return errorResponse;
        }
    }

    private HttpURLConnection createConnection(String secretKey, String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        return connection;
    }

    private JSONObject parseRequestData(String jsonBody) {
        try {
            return (JSONObject) new JSONParser().parse(jsonBody);
        } catch (ParseException e) {
            log.error("JSON Parsing Error", e);
            return new JSONObject();
        }
    }
}
