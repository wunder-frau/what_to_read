package org.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.example.client.OpenAiClient;
import org.example.dto.Question;
import org.example.repository.DiscussionRepository;
import org.example.repository.BookQuestionRepository;
import org.example.telegram.Bot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;

@Component
public class VoiceCommand extends Command {

    private static final String QUESTION_PROMPT = """
                Here is the baseline question for our book recommendation session: %s
                Please ask this question creatively by setting up a real-life scenario involving a well-known book or reading platform.
                For example, imagine you’re working at a renowned bookstore or on an online platform like Goodreads or Amazon,
                and a customer is looking for their next great read. Frame your question in a friendly, casual tone that invites
                the customer to share their reading preferences.
                
                Remember, the actual question should relate directly to the topic provided, but use the scenario to make it engaging.
                
                Style of communication:
                Speak like an old friend—warm, relaxed, and to the point. Keep your sentences clear and lively, ensuring the conversation
                remains interesting without overwhelming the customer with too much text.
                """;
    private static final String FEEDBACK_PROMPT = """
                 Analyze the questions asked during this book recommendation session along with the user's responses.
                Provide personalized feedback on their reading preferences by highlighting what you've learned about their tastes.
                
                Begin by summarizing the strengths—mention which preferences were clear and how they can help in selecting the right books.
                Next, point out any areas where the responses were vague or could be expanded, and suggest topics for further exploration.
                Finally, based on these insights, recommend a specific book that matches the user's profile.
                
                Format your feedback into clearly separated sections with the following headers:
                - Strengths
                - Suggestions
                - Recommendations
                - Book Recommendation
                
                Style of communication:
                Keep it friendly and informal—as if you're chatting with a good friend. Your feedback should be concise, engaging, and easy to read.
            """;

    @Value("${book.max-questions}")
    private int maxQuestions;

    private final BookQuestionRepository bookQuestionRepository;
    private final OpenAiClient openAiClient;
    private final DiscussionRepository discussionRepository;

    public VoiceCommand(OpenAiClient openAiClient,
                        DiscussionRepository discussionRepository,
                        BookQuestionRepository bookQuestionRepository) {
        super(bookQuestionRepository, openAiClient, discussionRepository);
        this.bookQuestionRepository = bookQuestionRepository;
        this.openAiClient = openAiClient;
        this.discussionRepository = discussionRepository;
    }

    @Override
    public boolean isApplicable(Update update) {
        return update.getMessage().hasVoice();
    }

    @Override
    public String process(Update update, Bot bot) {
        String answer = transcribeVoiceAnswer(update, bot);
        String userName = update.getMessage().getFrom().getUserName();
        discussionRepository.addAnswer(userName, answer);
        if (discussionRepository.getUserQuestions(userName) == maxQuestions) {
            return provideFeedback(userName);
        } else {
            return askNextQuestion(userName);
        }
    }

    private String transcribeVoiceAnswer(Update update, Bot bot) {
        Voice voice = update.getMessage().getVoice();
        String fileId = voice.getFileId();
        java.io.File audio;
        try {
            GetFile getFileRequest = new GetFile();
            getFileRequest.setFileId(fileId);
            File file = bot.execute(getFileRequest);
            audio = bot.downloadFile(file.getFilePath());
        } catch (TelegramApiException e) {
            throw new IllegalStateException("There's an error when processing Telegram audio", e);
        }
        return openAiClient.transcribe(renameToOgg(audio));
    }

    private java.io.File renameToOgg(java.io.File tmpFile) {
        String fileName = tmpFile.getName();
        String newFileName = fileName.substring(0, fileName.length() - 4) + ".ogg";
        Path sourcePath = tmpFile.toPath();
        Path targetPath = sourcePath.resolveSibling(newFileName);
        try {
            Files.move(sourcePath, targetPath);
        } catch (IOException e) {
            throw new IllegalStateException("There was an error when renaming .tmp audio file to .ogg", e);
        }
        return targetPath.toFile();
    }

    private String askNextQuestion(String userName) {
        String prompt = String.format(QUESTION_PROMPT, bookQuestionRepository.getRandomQuestion());
        String question = openAiClient.promptModel(prompt);
        discussionRepository.addQuestion(userName, question);
        return question;
    }

    private String provideFeedback(String userName) {
        StringBuilder feedbackPrompt = new StringBuilder();
        feedbackPrompt.append(FEEDBACK_PROMPT);
        Deque<Question> questions = discussionRepository.finishInterview(userName);
        questions.forEach(question -> feedbackPrompt.append("Original question: ")
                .append(question.getQuestion()).append("\n")
                .append("User's answer: ")
                .append(question.getAnswer()).append("\n"));
        return openAiClient.promptModel(feedbackPrompt.toString());
    }
}
