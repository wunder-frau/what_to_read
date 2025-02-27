package org.example.repository;

import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class BookQuestionRepository {
    private final List<String> questions = List.of(
        "What genres do you usually enjoy reading?",
        "Are you in the mood for fiction or non-fiction?",
        "Do you prefer fast-paced thrillers or more reflective, character-driven stories?",
        "Would you like a book that offers escapism or one that reflects real-life issues?",
        "Are you looking for a light-hearted read or something more serious?",
        "Do you enjoy classic literature or modern writing styles?",
        "Are you open to exploring genres you haven't read before?",
        "What’s your favorite book of all time, and why?",
        "Do you prefer standalone novels or a series?",
        "How important is character development to you in a story?",
        "Do you enjoy books with complex plot twists?",
        "Are you interested in books that explore historical events?",
        "Would you like recommendations that include diverse voices and cultures?",
        "Do you enjoy books that incorporate elements of fantasy or magic realism?",
        "Are you interested in books that challenge your perspective?",
        "How long do you typically like your books to be?",
        "Do you have a preference for narrative style, such as first-person or third-person perspectives?",
        "Do you prefer books with a strong romantic subplot?",
        "Are you in the mood for a mystery or detective story?",
        "Do you enjoy speculative fiction, like dystopian or utopian novels?",
        "Would you like a recommendation for a book that has been critically acclaimed?",
        "Are you interested in books that blend genres, such as romance with fantasy or mystery with science fiction?",
        "Do you enjoy humorous or satirical books?",
        "How do you feel about experimental writing styles?",
        "Are you looking for a book that provides deep philosophical insights?",
        "Do you enjoy memoirs or autobiographies?",
        "Would you prefer a book that is fast-paced or one that is slow and contemplative?",
        "Are you interested in books that focus on personal growth or self-help themes?",
        "Do you enjoy literature that explores complex family dynamics?",
        "Would you like to read a book that offers social or political commentary?",
        "How important is the setting in a book for you (e.g., historical, futuristic, real-world)?",
        "Are you interested in reading books that offer diverse cultural perspectives?",
        "Do you have a favorite author whose work you admire?",
        "Are you open to reading translated works from other languages?",
        "Do you prefer a narrative that is plot-driven or character-driven?",
        "Would you like to try a book that’s been popular on bestseller lists?",
        "Do you enjoy books with multiple viewpoints or narrators?",
        "Are you interested in literature that addresses contemporary issues?",
        "How much does the writing style influence your enjoyment of a book?",
        "Do you prefer a story that is emotionally moving or intellectually stimulating?",
        "Would you enjoy a book that incorporates humor even when dealing with serious topics?",
        "Do you like books that build suspense gradually or those with immediate action?",
        "Are you interested in books that explore the human condition in depth?",
        "Would you prefer books with a clear resolution or those that leave some questions unanswered?",
        "How do you feel about reading books with non-linear narratives?",
        "Do you enjoy richly descriptive language and immersive settings?",
        "Are you looking for a recommendation that suits a particular mood or moment in your life?",
        "Would you be interested in a book that has a strong underlying theme or moral lesson?",
        "Do you enjoy books that incorporate elements of mythology or folklore?",
        "How important is originality to you in a book recommendation?"
    );

    public List<String> getAllQuestions() {
        return questions;
    }

    public String getRandomQuestion () {
        return questions.get((int) (Math.random() * questions.size()));
    }
}
