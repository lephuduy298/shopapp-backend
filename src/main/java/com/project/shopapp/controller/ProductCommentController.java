package com.project.shopapp.controller;

import com.project.shopapp.dto.ProductCommentDTO;
import com.project.shopapp.dto.res.ResProductComment;
import com.project.shopapp.models.ProductComment;
import com.project.shopapp.models.User;
import com.project.shopapp.services.ProductCommentService;
import com.project.shopapp.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("${api.prefix}/comments")
//@Validated
//Dependency Injection
@RequiredArgsConstructor
public class ProductCommentController {
    private final ProductCommentService productCommentService;
    private final UserService userService;

    @GetMapping("/{productId}")
    public ResponseEntity<List<ResProductComment>> getAllComments(
            @PathVariable Long productId
    ) {
        List<ResProductComment> resProductComments;
        resProductComments = productCommentService.getCommentsByProduct(productId);
        return ResponseEntity.ok(resProductComments);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> updateComment(
            @PathVariable("id") Long commentId,
            @Valid @RequestBody ProductCommentDTO productCommentDTO
    ) {
            User loginUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!Objects.equals(loginUser.getId(), productCommentDTO.getUserId())) {
                return ResponseEntity.badRequest().body("You cannot update another user's comment");
            }
            productCommentService.updateComment(commentId, productCommentDTO);
            return ResponseEntity.ok("Update comment successfully");
    }


    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> createComment(
            @Valid @RequestBody ProductCommentDTO productCommentDTO
    ) {
            // Validate that the user is logged in and matches the userId in the comment
            if(productCommentDTO.getContent().isEmpty() || productCommentDTO.getContent().length() > 500) {
                return ResponseEntity.badRequest().body("Comment content must be between 1 and 500 characters");
            }
            // Insert the new comment
            User loginUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(loginUser.getId() != productCommentDTO.getUserId()) {
                return ResponseEntity.badRequest().body("You cannot comment as another user");
            }
            ProductComment productComment = productCommentService.insertComment(productCommentDTO);
            return ResponseEntity.ok(ResProductComment.fromComment(productComment));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteComment(
            @PathVariable("id") Long commentId
    ) {
        User loginUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ResProductComment resProductComment = productCommentService.getCommentById(commentId);

        User currentCommentUser = userService.getUserById(resProductComment.getResUser().getId());

        if(!loginUser.getRole().getName().equalsIgnoreCase("ADMIN") && !Objects.equals(loginUser.getId(), currentCommentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You cannot delete another user's comment");
        }

        productCommentService.deleteComment(commentId);
        return ResponseEntity.ok("Delete comment successfully");
    }
}

