package com.mureung.config;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;
import org.apache.ibatis.executor.Executor;

@Component
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class LoginUserInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object parameter = invocation.getArgs()[1];
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String userId = auth.getName();

            if (parameter instanceof Map) {
                ((Map<String, Object>) parameter).put("_loginUserId", userId);
            } else if (parameter != null) {
                // 수정: 부모 클래스(BaseDto)의 필드까지 찾기 위해 while문 사용
                Class<?> clazz = parameter.getClass();
                while (clazz != null) {
                    try {
                        Field field = clazz.getDeclaredField("_loginUserId");
                        field.setAccessible(true);
                        if (field.get(parameter) == null) {
                            field.set(parameter, userId);
                        }
                        break; // 필드를 찾았으므로 종료
                    } catch (NoSuchFieldException e) {
                        clazz = clazz.getSuperclass(); // 부모 클래스로 이동하여 재탐색
                    }
                }
            }
        }
        return invocation.proceed(); //쿼리 실행
    }
}