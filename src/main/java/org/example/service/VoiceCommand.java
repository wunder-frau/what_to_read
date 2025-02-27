package org.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(VoiceCommand.class);

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

    public VoiceCommand(OpenAiClient openAiClient,
                        DiscussionRepository discussionRepository,
                        BookQuestionRepository bookQuestionRepository) {
        super(bookQuestionRepository, openAiClient, discussionRepository);
    }

    @Override
    public boolean isApplicable(Update update) {
        boolean applicable = update.getMessage().hasVoice();
        logger.info("VoiceCommand.isApplicable: update has voice = {}", applicable);
        return applicable;
    }

    @Override
    public String process(Update update, Bot bot) {
        String userName = update.getMessage().getFrom().getUserName();
        logger.info("Processing voice command for user: {}", userName);
        
        // Transcribe the voice message
        String answer = transcribeVoiceAnswer(update, bot);
        logger.info("Transcribed answer for user {}: {}", userName, answer);
        
        // Attempt to add the answer to the active session
        try {
            discussionRepository.addAnswer(userName, answer);
            logger.info("Answer added for user: {}", userName);
        } catch (IllegalStateException ex) {
            logger.error("Error adding answer for user {}: {}", userName, ex.getMessage());
            // Inform user to start a session
            return "It seems you haven't started a session yet. Please send /start to begin a book recommendation session.";
        }
        
        int currentCount = discussionRepository.getUserQuestions(userName);
        logger.info("Current question count for user {}: {}", userName, currentCount);
        
        if (currentCount == maxQuestions) {
            logger.info("Maximum questions reached for user {}. Providing feedback.", userName);
            return provideFeedback(userName);
        } else {
            logger.info("Asking next question for user {}.", userName);
            return askNextQuestion(userName);
        }
    }

    private String transcribeVoiceAnswer(Update update, Bot bot) {
        logger.info("Starting transcription process for voice message.");
        Voice voice = update.getMessage().getVoice();
        String fileId = voice.getFileId();
        java.io.File audio;
        try {
            GetFile getFileRequest = new GetFile();
            getFileRequest.setFileId(fileId);
            File file = bot.execute(getFileRequest);
            logger.info("Retrieved file path: {}", file.getFilePath());
            audio = bot.downloadFile(file.getFilePath());
            logger.info("Downloaded audio file: {}", audio.getAbsolutePath());
        } catch (TelegramApiException e) {
            logger.error("Error processing Telegram audio", e);
            throw new IllegalStateException("Error processing Telegram audio", e);
        }
        java.io.File oggFile = renameToOgg(audio);
        logger.info("Renamed audio file to OGG: {}", oggFile.getAbsolutePath());
        String transcript = openAiClient.transcribe(oggFile);
        logger.info("Transcription result: {}", transcript);
        return transcript;
    }

    private java.io.File renameToOgg(java.io.File tmpFile) {
        logger.info("Renaming file {} to OGG format.", tmpFile.getName());
        String fileName = tmpFile.getName();
        String newFileName = fileName.substring(0, fileName.length() - 4) + ".ogg";
        Path sourcePath = tmpFile.toPath();
        Path targetPath = sourcePath.resolveSibling(newFileName);
        try {
            Files.move(sourcePath, targetPath);
            logger.info("File renamed successfully to: {}", targetPath.toString());
        } catch (IOException e) {
            logger.error("Error renaming file to .ogg", e);
            throw new IllegalStateException("Error renaming .tmp audio file to .ogg", e);
        }
        return targetPath.toFile();
    }

    private String askNextQuestion(String userName) {
        String randomQuestion = bookQuestionRepository.getRandomQuestion();
        logger.info("Random question retrieved: {}", randomQuestion);
        String prompt = String.format(QUESTION_PROMPT, randomQuestion);
        logger.info("Generated prompt: {}", prompt);
        String question = openAiClient.promptModel(prompt);
        logger.info("OpenAI response for question: {}", question);
        discussionRepository.addQuestion(userName, question);
        logger.info("Question added for user: {}", userName);
        return question;
    }

    private String provideFeedback(String userName) {
        StringBuilder feedbackPrompt = new StringBuilder();
        feedbackPrompt.append(FEEDBACK_PROMPT);
        Deque<Question> questions = discussionRepository.finishInterview(userName);
        logger.info("Finishing session for user {}. Total questions answered: {}", userName, questions.size());
        questions.forEach(question -> {
            feedbackPrompt.append("Original question: ")
                .append(question.getQuestion()).append("\n")
                .append("User's answer: ")
                .append(question.getAnswer()).append("\n");
        });
        logger.info("Feedback prompt constructed: {}", feedbackPrompt.toString());
        String feedback = openAiClient.promptModel(feedbackPrompt.toString());
        logger.info("Received feedback from OpenAI: {}", feedback);
        return feedback;
    }
}
