package com.example.chatserver.chat.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class StompController {


    //메시지 받아서 전달해줌
    @MessageMapping("/{roomId}") //클라이언트에서 특정 publish/roomId형태로 메세지 발행시 MessageMapping이 수신
    @SendTo("/topic/{roomId}")   //해당 roomId에 메세지를 발행하여 구독중인 클라이언트에게 메시지 전송
    // DestinationVariable: @MessageMapping 어노테이션으로 정의된 Websocket Controller 내에서만 사용됨.
    public  String sendMessage(@DestinationVariable Long roomId, @Payload String message){
        System.out.printf(message);
        return message;
    }

}
