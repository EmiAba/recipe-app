package app.comment;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.comment.service.CommentService;
import app.exception.UnauthorizedAccessException;
import app.recipe.model.Recipe;
import app.recipe.service.RecipeService;
import app.user.model.User;
import app.web.dto.CommentCreateRequest;
import app.web.dto.CommentEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceUTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private  RecipeService recipeService;

    @InjectMocks
    private CommentService commentService;

    @Test
    public void whenCreateComment_thenSaveComment() {

        UUID recipeId = UUID.randomUUID();
        User author = User.builder().username("Emi").build();
        Recipe recipe = Recipe.builder().title("Apple Pie").build();

        CommentCreateRequest request = CommentCreateRequest.builder()
                .content("Great recipe!")
                .rating(5)
                .build();

        Comment savedComment = Comment.builder()
                .content("Great recipe!")
                .rating(5)
                .build();


        when(recipeService.getById(recipeId)).thenReturn(recipe);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        Comment result = commentService.createComment(request, recipeId, author);



        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Great recipe!");
        assertThat(result.getRating()).isEqualTo(5);
        verify(commentRepository).save(any(Comment.class));
        verify(recipeService).getById(recipeId);
    }

    @Test
    public void whenUpdateComment_andUserIsNotAuthor_thenThrowException() {
        UUID commentId = UUID.randomUUID();
        User author = User.builder().id(UUID.randomUUID()).build();
        User differentUser = User.builder().id(UUID.randomUUID()).build();

        Comment existingComment = Comment.builder()
                .author(author)
                .build();

        CommentEditRequest editRequest = CommentEditRequest.builder()
                .content("Trying to edit").build();

        when(commentRepository.findByIdWithDetails(commentId)).thenReturn(Optional.of(existingComment));


        assertThrows(UnauthorizedAccessException.class, () -> commentService.updateComment(commentId, editRequest, differentUser));
    }


    @Test
    public void whenUpdateComment_thenReturnUpdatedComment() {
        UUID commentId = UUID.randomUUID();
        User author = User.builder().id(UUID.randomUUID()).build();

        Comment existingComment = Comment.builder()
                .content("Old content")
                .rating(3)
                .author(author)
                .build();

        CommentEditRequest editRequest = CommentEditRequest.builder()
                .content("New content")
                .rating(5)
                .build();

        when(commentRepository.findByIdWithDetails(commentId)) .thenReturn(Optional.of(existingComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(existingComment);

        Comment result = commentService.updateComment(commentId, editRequest, author);

        assertThat(result.getContent()).isEqualTo("New content");
        assertThat(result.getRating()).isEqualTo(5);
        verify(commentRepository).save(any(Comment.class));
    }



    @Test
    public void  whenGetCommentsByRecipe_thenReturnListOfComments() {
        UUID recipeId = UUID.randomUUID();
        Comment comment = Comment.builder().build();

        when(commentRepository.findByRecipeIdWithAuthor(recipeId)).thenReturn(List.of(comment));

        List<Comment> comments = commentService.getCommentsByRecipe(recipeId);


        assertThat(comments).isNotNull();
        assertThat(comments).hasSize(1);
        assertThat(comments).contains(comment);

    }

    @Test
    public void whenDeleteComment_andUserIsNotAuthor_thenThrowException() {
        UUID commentId = UUID.randomUUID();
        User author = User.builder().id(UUID.randomUUID()).build();
        User differentUser = User.builder().id(UUID.randomUUID()).build();

        Comment comment = Comment.builder().author(author).build();

        when(commentRepository.findByIdWithDetails(commentId)).thenReturn(Optional.of(comment));

        assertThrows(UnauthorizedAccessException.class, () -> commentService.deleteComment(commentId, differentUser));
    }


    @Test
    public void whenDeleteComment_thenDeleteComment() {
        UUID commentId = UUID.randomUUID();
        User author = User.builder().id(UUID.randomUUID()).build();

        Comment comment = Comment.builder()
                .author(author)
                .build();

        when(commentRepository.findByIdWithDetails(commentId)).thenReturn(Optional.of(comment));

        commentService.deleteComment(commentId, author);

        verify(commentRepository).delete(comment);
    }





}
