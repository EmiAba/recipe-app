package app.recipe.model;


import app.category.model.Category;
import app.comment.model.Comment;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String instructions;

    @Column(name = "prep_time_minutes")
    private Integer prepTimeMinutes;

    @Column(name = "cook_time_minutes")
    private Integer cookTimeMinutes;

    @Column(name = "serving_size")
    private Integer servingSize;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    private String imageUrl;

    @Column(nullable = false)
    private boolean isPublic = true;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(columnDefinition = "TEXT")
    private String ingredients;

    private Integer calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private Double fiber;
    private Double sugar;
    private Double sodium;
    @ManyToMany
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdOn DESC")
    private List<Comment> comments = new ArrayList<>();

    @ManyToMany
    private Set<User> favoriteBy = new HashSet<>();

    @Column(nullable = false)
    private boolean deleted = false;

}