package app.web.mapper;

import app.comment.model.Comment;
import app.web.dto.CommentEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class CommentMapperUTest {

    @Test
    void testMapCommentToCommentEditRequest() {

        Comment comment=Comment.builder()
                .content("Super meal!")
                .rating(5)
                .build();


        CommentEditRequest result=CommentMapper.toEditRequest(comment);

        assertNotNull(result);
        assertEquals("Super meal!", result.getContent());
        assertEquals(5, result.getRating());


    }
}
