package wb.wolbu.controller;

import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Member Management", description = "APIs for managing members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    @Operation(summary = "Register a new member", description = "Creates a new member account")
    @ApiResponse(responseCode = "201", description = "Successfully registered",
            content = @Content(schema = @Schema(implementation = MemberDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
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
    @Operation(summary = "Get a member", description = "Retrieves a member by their ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved member",
            content = @Content(schema = @Schema(implementation = MemberDTO.class)))
    @ApiResponse(responseCode = "404", description = "Member not found")
    public ResponseEntity<MemberDTO> getMember(@PathVariable Long id) {
        Member member = memberService.findMemberById(id);
        return new ResponseEntity<>(MemberDTO.from(member), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a member", description = "Updates an existing member's information")
    @ApiResponse(responseCode = "200", description = "Successfully updated member",
            content = @Content(schema = @Schema(implementation = MemberDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Member not found")
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
    @Operation(summary = "Change member password", description = "Changes the password of an existing member")
    @ApiResponse(responseCode = "204", description = "Successfully changed password")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Member not found")
    public ResponseEntity<Void> changePassword(@PathVariable Long id, @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        memberService.changePassword(id, passwordChangeDTO.getCurrentPassword(), passwordChangeDTO.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a member", description = "Deletes an existing member")
    @ApiResponse(responseCode = "204", description = "Successfully deleted member")
    @ApiResponse(responseCode = "404", description = "Member not found")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}
