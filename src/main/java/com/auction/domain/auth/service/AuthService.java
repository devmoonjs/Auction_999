package com.auction.domain.auth.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.common.utils.JwtUtil;
import com.auction.domain.auth.dto.request.LoginRequestDto;
import com.auction.domain.auth.dto.request.SignoutRequest;
import com.auction.domain.auth.dto.request.SignupRequestDto;
import com.auction.domain.auth.dto.response.LoginResponseDto;
import com.auction.domain.auth.dto.response.SignupResponseDto;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public SignupResponseDto createUser(SignupRequestDto signupRequest) {
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
        User user = userRepository.save(new User(encodedPassword, signupRequest));

        return SignupResponseDto.of(user);
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_USER)
        );

        isDeactivateUser(user);
        checkPassword(loginRequestDto.getPassword(), user.getPassword());

        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getAuthority());

        return LoginResponseDto.of(bearerToken);
    }

    @Transactional
    public void deactivateUser(AuthUser authUser, SignoutRequest signoutRequest) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_USER)
        );

        isDeactivateUser(user);
        checkPassword(signoutRequest.getPassword(), user.getPassword());
        user.changeDeactivate();
    }

    private void isDeactivateUser(User user) {
        if (!user.isActivate()) {
            throw new ApiException(ErrorStatus._NOT_FOUND_USER);
        }
    }

    private void checkPassword(String password, String encodedPassword) {
        if (!passwordEncoder.matches(password, encodedPassword)) {
            throw new ApiException(ErrorStatus._PERMISSION_DENIED);
        }
    }
}
