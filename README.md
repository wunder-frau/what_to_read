# Book Recommendation Bot

A friendly Telegram bot that provides personalized book recommendations based on your reading preferences. It uses Spring Boot, the Telegram Bots API, and OpenAI services to interact with users—both through text and voice commands.

## Features

- **Personalized Recommendations:**  
  Ask a series of questions to understand the user’s reading tastes and suggest books tailored to their preferences.

- **Voice Command Support:**  
  Use voice messages to interact with the bot. The bot transcribes the voice input and processes it as part of the recommendation session.

- **OpenAI Integration:**  
  Leverages the OpenAI API for generating creative prompts, analyzing user responses, and formulating book recommendations.

- **Interactive Conversation:**  
  Communicates in a warm, friendly manner—like chatting with an old friend.

## Requirements

- **Java 21** (or higher)
- **Gradle 8+** (Gradle wrapper is provided)
- A valid **Telegram Bot Token** (from [BotFather](https://t.me/BotFather))
- A valid **OpenAI API Key**

## Installation

1. **Clone the Repository:**

2. **Configure Application Properties:**

Create a file at src/main/resources/application.yaml with the following content. Replace the placeholder values with your actual tokens and API keys:

```yaml
bot:
  token: YOUR_TELEGRAM_BOT_TOKEN

openai:
  api:
    key: YOUR_OPENAI_API_KEY
    transcription:
      url: https://api.openai.com/v1/audio/transcriptions
      model: whisper-1
      language: en
    chat:
      url: https://api.openai.com/v1/chat/completions
      model: gpt-4o
      system_role: "You are a knowledgeable and friendly book recommendation assistant. Your task is to provide personalized book suggestions based on users' interests, reading habits, and preferred genres. In addition to popular titles, you should also advise unique and lesser-known books that offer fresh perspectives and hidden gems. Ask clarifying questions when needed, and offer diverse recommendations that cater to different tastes and literary styles."
book:
  max-questions: 3
```

3. **Build and Run:**

   Use the Gradle wrapper to build and run the application:

   ```bash
   ./gradlew bootRun
   ```

## Usage

- **Telegram Interaction:**  
  Once the bot is running, search for it on Telegram (using the username you configured) and start a conversation by sending `/start` or using voice commands.

- **Voice Commands:**  
  The bot will transcribe your voice message, ask a series of questions about your reading preferences, and ultimately provide personalized book recommendations based on your responses.
