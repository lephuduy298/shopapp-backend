package com.project.shopapp.services.iservice;

import com.project.shopapp.dto.ProductCommentDTO;
import com.project.shopapp.dto.res.ResProductComment;
import com.project.shopapp.error.DataNotFoundException;
import com.project.shopapp.models.ProductComment;

import java.util.List;

public interface IProductCommentService {
    ProductComment insertComment(ProductCommentDTO comment);

    void deleteComment(Long commentId);
    void updateComment(Long id, ProductCommentDTO commentDTO) throws DataNotFoundException;

    List<ResProductComment> getCommentsByUserAndProduct(Long userId, Long productId);
    List<ResProductComment> getCommentsByProduct(Long productId);

    ResProductComment getCommentById(Long commentId) throws DataNotFoundException;
}
