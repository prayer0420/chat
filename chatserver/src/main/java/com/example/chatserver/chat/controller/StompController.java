package com.example.chatserver.chat.controller;

import com.example.chatserver.chat.dto.ChatMessageDto;
import com.example.chatserver.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class StompController {

    private final SimpMessageSendingOperations messageTemplate;
    private final ChatService chatService;

    public StompController(SimpMessageSendingOperations messageTemplate, ChatService chatService) {
        this.messageTemplate = messageTemplate;
        this.chatService = chatService;
    }

//    //방법 1. MessageMapping(수신)과 SendTo(topic에 메시지 전달)한꺼번에 처리
//    //메시지 받아서 전달해줌
//    @MessageMapping("/{roomId}") //클라이언트에서 특정 publish/roomId형태로 메세지 발행시 MessageMapping이 수신
//    @SendTo("/topic/{roomId}")   //해당 roomId에 메세지를 발행하여 구독중인 클라이언트에게 메시지 전송
//    // DestinationVariable: @MessageMapping 어노테이션으로 정의된 Websocket Controller 내에서만 사용됨.
//    public  String sendMessage(@DestinationVariable Long roomId, @Payload String message){
//        System.out.printf(message);
//        return message;
//    }
    
    //방법2. MessageMapping어노테이션만 활용
    @MessageMapping("/{roomId}") //클라이언트에서 특정 publish/roomId형태로 메세지 발행시 MessageMapping이 수신
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto chatMessageDto) {
        System.out.printf(chatMessageDto.getMessage());

        //메시지 저장(db)
        chatService.saveMessage(roomId, chatMessageDto);

        //직접 메시지 발행
        messageTemplate.convertAndSend("/topic/"+roomId, chatMessageDto);
    }
}
