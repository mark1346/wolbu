package wb.wolbu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wb.wolbu.dto.MemberDTO;
import wb.wolbu.dto.PasswordChangeDTO;
import wb.wolbu.entity.Member;
import wb.wolbu.entity.MemberType;
import wb.wolbu.service.MemberService;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<MemberDTO> register(@Valid @RequestBody MemberDTO memberDTO) {
        Member member = memberService.registerMember(
                memberDTO.getName(),
                memberDTO.getEmail(),
                memberDTO.getPhoneNumber(),
                memberDTO.getPassword(),
                MemberType.valueOf(memberDTO.getMemberType())
        );
        return new ResponseEntity<>(MemberDTO.from(member), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberDTO> getMember(@PathVariable Long id) {
        Member member = memberService.findMemberById(id);
        return new ResponseEntity<>(MemberDTO.from(member), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberDTO> updateMember(@PathVariable Long id, @Valid @RequestBody MemberDTO memberDTO) {
        Member updatedMember = memberService.updateMember(
                id,
                memberDTO.getName(),
                memberDTO.getPhoneNumber(),
                MemberType.valueOf(memberDTO.getMemberType())
        );
        return new ResponseEntity<>(MemberDTO.from(updatedMember), HttpStatus.OK);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id, @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        memberService.changePassword(id, passwordChangeDTO.getCurrentPassword(), passwordChangeDTO.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}
