package org.example.service;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.example.client.OpenAiClient;
import org.example.repository.DiscussionRepository;
import org.example.repository.BookQuestionRepository;
import org.example.telegram.Bot;

@RequiredArgsConstructor
public abstract class Command {

    protected final BookQuestionRepository bookQuestionRepository;
    protected final OpenAiClient openAiClient;
    protected final DiscussionRepository discussionRepository;

    public abstract boolean isApplicable(Update update);

    public abstract String process(Update update, Bot bot);
}
