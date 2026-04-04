package dev.jino.tripbasketnew.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import dev.jino.tripbasketnew.member.controller.api.MemberApi;
import dev.jino.tripbasketnew.member.dto.MyInfoResponseDto;
import dev.jino.tripbasketnew.member.service.MemberService;
import dev.jino.tripbasketnew.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberController implements MemberApi {

    private final MemberService memberService;

    @Override
    public ResponseEntity<MyInfoResponseDto> getMyInfo(UserPrincipal userPrincipal) {
        MyInfoResponseDto response = memberService.getMyInfo(userPrincipal.getMemberId());
        return ResponseEntity.ok(response);
    }
}
