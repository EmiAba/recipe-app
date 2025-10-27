package app.web.mapper;

import app.comment.model.Comment;
import app.web.dto.CommentEditRequest;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public static CommentEditRequest toEditRequest(Comment comment) {
        return CommentEditRequest.builder()
                .content(comment.getContent())
                .rating(comment.getRating())
                .build();
    }
}