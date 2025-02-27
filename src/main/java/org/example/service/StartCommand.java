package org.example.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.example.client.OpenAiClient;
import org.example.repository.DiscussionRepository;
import org.example.repository.BookQuestionRepository;
import org.example.telegram.Bot;

@Component
public class StartCommand extends Command {

    private static final String READING_PROMPT = """
        Hey there! I'm your book buddy, here to help you find your next favorite read.
        
        I'd love to get to know your taste in books, so I'll ask you 3 quick questions:
        1. What kinds of stories or genres do you really love? (For example, are you into fantasy, mystery, romance, or maybe non-fiction?)
        2. Do you usually go for books that are fun and light, or do you enjoy something a bit more deep and thought-provoking?
        3. Are you in the mood for a popular bestseller or would you prefer to discover a hidden gem that not many have heard of?
        
        Let's chat like old friends—I’ll keep things relaxed and fun, and if I need a bit more info, I'll ask follow-up questions. My goal is to give you spot-on recommendations without overwhelming you.
        
        So, let's get started! Here's your first question: %s
        """;

    public StartCommand(BookQuestionRepository bookQuestionRepository,
                        OpenAiClient openAiClient,
                        DiscussionRepository discussionRepository) {
        super(bookQuestionRepository, openAiClient, discussionRepository);
    }

    public boolean isApplicable(Update update) {
        Message message = update.getMessage();
        return message.hasText() && "/start".equals(message.getText());
    }

    public String process(Update update, Bot bot) {
        String question = bookQuestionRepository.getRandomQuestion();
        String prompt = String.format(READING_PROMPT, question);
        return openAiClient.promptModel(prompt);
    }
}
