package com.example.chatserver.chat.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class StompHandler implements ChannelInterceptor {

    @Value("${jwt.secretKey}")
    private String secretKey;

    //사용자의 요청정보에서 토큰을 꺼내서 검증
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        //access안에서 토큰을 꺼낼 수 있음
        //사용자의 요청이 message
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String bearerToken = accessor.getFirstNativeHeader("Authorization");

        //CONNECT시에만 TOKEN검증
        if(StompCommand.CONNECT == accessor.getCommand()) {
            System.out.println("Connect요청시 토큰 유효성 검증");
            String token = bearerToken.substring(7); //토큰 꺼내기
            //토큰 검증
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            System.out.println("토큰 검증 완료");
        }

        return message;
    }

}