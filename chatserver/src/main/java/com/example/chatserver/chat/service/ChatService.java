package com.example.chatserver.chat.service;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.domain.ChatParticipant;
import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.domain.ReadStatus;
import com.example.chatserver.chat.dto.ChatMessageDto;
import com.example.chatserver.chat.dto.ChatRoomListResDto;
import com.example.chatserver.chat.dto.MyChatListResDto;
import com.example.chatserver.chat.repository.ChatMessageRepository;
import com.example.chatserver.chat.repository.ChatParticipantRepository;
import com.example.chatserver.chat.repository.ChatRoomRepository;
import com.example.chatserver.chat.repository.ReadStatusRepository;
import com.example.chatserver.member.domain.Member;
import com.example.chatserver.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MemberRepository memberRepository;

    public ChatService(ChatRoomRepository chatRoomRepository, ChatParticipantRepository chatParticipantRepository, ChatMessageRepository chatMessageRepository, ReadStatusRepository readStatusRepository, MemberRepository memberRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.readStatusRepository = readStatusRepository;
        this.memberRepository = memberRepository;
    }

    public void saveMessage(Long roomId, ChatMessageDto chatMessageDto) {
        //채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("Room cannot found"));
        
        //보낸사람 조회
        Member sender = memberRepository.findByEmail(chatMessageDto.getSenderEmail()).orElseThrow(()->new EntityNotFoundException("Member not found"));
        
        //메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(chatMessageDto.getMessage())
                .build();
        chatMessageRepository.save(chatMessage);

        //사용자별로 읽음 여부 저장(변경)
         List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
         for (ChatParticipant c : chatParticipants) {
             ReadStatus readStatus = ReadStatus.builder()
                     .chatRoom(chatRoom)
                     .member(c.getMember())
                     .chatMessage(chatMessage)
                     .isRead(c.getMember().equals(sender))
                     .build();
             readStatusRepository.save(readStatus);
         }

    }
    
    //그룹채팅방 개설
    public void createGroupRoom(String CharRoomName){
        
        //멤버 조회
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(()->new EntityNotFoundException("Member cannot found"));

        //채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(CharRoomName)
                .isGroupChat("Y")
                .build();
        chatRoomRepository.save(chatRoom);

        //채팅 참여자로 개설자를 추가(최초)
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }
    
    
    //단체 채팅방 조회
    public List<ChatRoomListResDto> getGroupchatRoom(){
        List<ChatRoom> chatRooms =  chatRoomRepository.findByIsGroupChat("Y");
        List<ChatRoomListResDto> dtos = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms){
            ChatRoomListResDto dto = ChatRoomListResDto
                    .builder()
                    .roomId(chatRoom.getId())
                    .roomName(chatRoom.getName())
                    .build();
            dtos.add(dto);
        }
        return dtos;
    }
    
    //참여자 추가
    public void addParticipantToGroupChat(Long roomId){
        //채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("Room cannot be found"));

        //멤버 조회
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(()->new EntityNotFoundException("Member cannot found"));

        //그룹챗이 아니면 참여자 추가 X
        if(chatRoom.getIsGroupChat().equals("N")){
            throw new IllegalArgumentException("그룹채팅이 아닙니다");
        }

        //이미 참여자 인지 검증
        Optional<ChatParticipant> participant =  chatParticipantRepository.findByChatRoomAndMember(chatRoom, member);
        if(!participant.isPresent()){
            addParticipantToRoom(chatRoom, member);
        }
    }

    //ChatParticipant객체 생성 후 저장
    public void addParticipantToRoom(ChatRoom chatRoom, Member member){
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }
    
    
    //이전 내역(메시지) 조회
    public List<ChatMessageDto> getChatHistory(Long roomId){
        //내가 해당 채팅방의 참여자가 아닐 경우 에러(채팅방, 멤버 조회, 채팅방의 참여자조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("Room cannot be found"));
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(()->new EntityNotFoundException("Member cannot found"));
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        boolean check = false;
        for(ChatParticipant chatParticipant : chatParticipants){
            if(chatParticipant.getMember().equals(member)){
                check = true;
            }
        }

        if(!check) throw new IllegalArgumentException("본인이 속하지 않은 채팅방 입니다");

        //특정 room에 대한 message 조회(생성순)
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);

        List<ChatMessageDto>  chatMessageDtos = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessages) {
            ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                    .message(chatMessage.getContent())
                    .senderEmail(chatMessage.getMember().getEmail())
                    .build();
            chatMessageDtos.add(chatMessageDto);
        }
        return chatMessageDtos;
    }

    //해당 채팅방에 해당 참여자가 있는지
    public boolean isRoomParticipant(String email, Long roomId){
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("Room cannot be found"));
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new EntityNotFoundException("Member cannot found"));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        for(ChatParticipant chatParticipant : chatParticipants){
            if(chatParticipant.getMember().equals(member)){
                return true;
            }
        }
        //해당 방에 해당 참여자가 아닌 경우
        return false;
    }
    
    //메시지 읽음처리
    public void messageRead(Long roomId){
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("Room cannot be found"));
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(()->new EntityNotFoundException("Member cannot found"));

        //메시지 읽음
        List<ReadStatus> readStatuses = readStatusRepository.findByChatRoomAndMember(chatRoom,member);
        for(ReadStatus readStatus : readStatuses){
            readStatus.updateIsRead(true);
        }
    }
    
    //내 채팅방 목록 조회
    public List<MyChatListResDto> getMyChatRooms(){
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(()->new EntityNotFoundException("Member cannot found"));
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByMember(member);
        List<MyChatListResDto> chatListResDtos = new ArrayList<>();
        for(ChatParticipant chatParticipant : chatParticipants){
            Long count = readStatusRepository.countByChatRoomAndMemberAndIsReadFalse(chatParticipant.getChatRoom(),member);
            MyChatListResDto dto = MyChatListResDto.builder()
                    .roomId(chatParticipant.getChatRoom().getId())
                    .roomName(chatParticipant.getChatRoom().getName())
                    .isGroupChat(chatParticipant.getChatRoom().getIsGroupChat())
                    .unReadCount(count)
                    .build();
            chatListResDtos.add(dto);
        }
        return chatListResDtos;
    }

    //채팅방 나가기
    public void leaveGroupChatRoom(Long roomId){
        //참여자 객체 지우기
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("Room cannot be found"));
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(()->new EntityNotFoundException("Member cannot found"));
        //단체 채팅방인지 확인
        if(chatRoom.getIsGroupChat().equals("N")){
            throw new IllegalArgumentException("단체 채팅방이 아닙니다");
        }
        ChatParticipant c= chatParticipantRepository.findByChatRoomAndMember(chatRoom, member).orElseThrow(()->new EntityNotFoundException("참여자 찾을 수 없습니다."));
        chatParticipantRepository.delete(c);

        //남은 참여자 수 체크
        //모두가 나가면 채팅방, 메세지, 읽음여부 모두 삭제
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        if(chatParticipants.isEmpty()){
            chatRoomRepository.delete(chatRoom); //나머지는 cascading으로 다 삭제됨
        }

    }

    //개인 채팅방 가져오거나 만들거나
    public Long GetOrCreatePrivateRoom(Long otherMemberId){
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(()->new EntityNotFoundException("Member cannot found"));
        Member otherMember =  memberRepository.findById(otherMemberId).orElseThrow(()->new EntityNotFoundException("OtherMember cannot found"));

        //나와 상대방이 1:1채팅에 이미 참석하고 있다면 해당 roomId바로 return
        Optional<ChatRoom> chatRoom =  chatParticipantRepository.findExistingPrivateRoom(member.getId(), otherMember.getId());
        if(chatRoom.isPresent()){
            return chatRoom.get().getId();
        }

        //만약에 1:!채팅방이 업승ㄹ 경우 기존채팅방 개설
        ChatRoom newRoom = ChatRoom.builder()
                .isGroupChat("N")
                .name(member.getName() + "-" + otherMember.getName())
                .build();
        chatRoomRepository.save(newRoom);

        //두사람 모두 참여자로 새롭게 추가
        addParticipantToRoom(newRoom, member);
        addParticipantToRoom(newRoom, otherMember);

        return newRoom.getId();
    }
}