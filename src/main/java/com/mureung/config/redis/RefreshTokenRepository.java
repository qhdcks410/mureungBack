package com.mureung.config.redis;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByAuthKey(String authKey);
    Optional<RefreshToken> findRefreshTokenByJwtRefreshToken(String refreshToken);
}
