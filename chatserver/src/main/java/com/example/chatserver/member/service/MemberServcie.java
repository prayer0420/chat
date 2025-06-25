package com.example.chatserver.member.service;

import com.example.chatserver.member.domain.Member;
import com.example.chatserver.member.dto.MemberListResDto;
import com.example.chatserver.member.dto.MemberLoginReqDto;
import com.example.chatserver.member.dto.MemberSaveReqDto;
import com.example.chatserver.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional

public class MemberServcie {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    public MemberServcie(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Member create(MemberSaveReqDto memberSaveReqDto){
        //이미 가입되어있는 이메일 검증
        if(memberRepository.findByEmail(memberSaveReqDto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다");
        }

        Member newMember = Member.builder()
                .name(memberSaveReqDto.getName())
                .email(memberSaveReqDto.getEmail())
                //비밀번호 암호화
                .password(passwordEncoder.encode(memberSaveReqDto.getPassword()))
                .build();

        Member member = memberRepository.save(newMember);

        return member;
    }

    public  Member login(MemberLoginReqDto memberLoginReqDto){
        //객체 조회
        Member member = memberRepository.findByEmail(memberLoginReqDto.getEmail())
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 이메일입니다"));
        
        //비밀번호 비교
        if(passwordEncoder.matches(memberLoginReqDto.getPassword(), member.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
        return member;
    }

    public List<MemberListResDto> findAll(){
            List<Member> members = memberRepository.findAll();
            List<MemberListResDto> memberListResDtos = new ArrayList<>();
            for(Member member : members){
                MemberListResDto memberListResDto = new MemberListResDto();
                memberListResDto.setId(member.getId());
                memberListResDto.setName(member.getName());
                memberListResDto.setEmail(member.getEmail());
                memberListResDtos.add(memberListResDto);
            }
            return memberListResDtos;
    }
}
