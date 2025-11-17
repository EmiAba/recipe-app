package app.web.mapper;

import app.comment.model.Comment;
import app.web.dto.CommentEditRequest;
import lombok.experimental.UtilityClass;


@UtilityClass
public class CommentMapper {

    public static CommentEditRequest toEditRequest(Comment comment) {
        return CommentEditRequest.builder()
                .content(comment.getContent())
                .rating(comment.getRating())
                .build();
    }
}