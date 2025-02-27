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
        
        Let's chat like old friends—I’ll keep things relaxed and fun, and if I need a bit more info, I'll ask follow-up questions. My goal is to give you spot-on recommendations without overwhelming you.
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
        String userName = update.getMessage().getFrom().getUserName();
        String question = bookQuestionRepository.getRandomQuestion();
        discussionRepository.addQuestion(userName, question);
        String prompt = String.format(READING_PROMPT, question);
        return openAiClient.promptModel(prompt);
    }
}