package com.ikhwan.mandarinkids.data.scenarios

import com.ikhwan.mandarinkids.data.models.Scenario
import com.ikhwan.mandarinkids.data.models.DialogueStep
import com.ikhwan.mandarinkids.data.models.Speaker
import com.ikhwan.mandarinkids.data.models.ResponseType
import com.ikhwan.mandarinkids.data.models.ResponseOption
import com.ikhwan.mandarinkids.data.models.PinyinWord
import com.ikhwan.mandarinkids.data.models.QuizQuestion
import com.ikhwan.mandarinkids.data.models.QuizDirection
import com.ikhwan.mandarinkids.data.models.QuizOption
import com.ikhwan.mandarinkids.data.models.*

fun getScenario2_MeetingMing(): Scenario {
    return Scenario(
            id = "scene2_meeting_classmate",
            title = "认识新朋友",
            description = "Meeting Liu Ming, your new classmate",
            characterName = "Liu Ming",
            characterEmoji = "👦",
            characterRole = "friend",
            dialogues = listOf(
                DialogueStep(
                    id = 1,
                    speaker = com.ikhwan.mandarinkids.data.models.Speaker.CHARACTER,
                    textChinese = "你好！你坐在我旁边。我叫刘明。",
                    textPinyin = "Nǐ hǎo! Nǐ zuò zài wǒ pángbiān. Wǒ jiào Liú Míng.",
                    textEnglish = "Hi! You're sitting next to me. I'm Liu Ming.",
                    textIndonesian = "Hai! Kamu duduk di sebelah saya. Nama saya Liu Ming.",
                    pinyinWords = listOf(
                        PinyinWord("Nǐ", "你", "You", "Kamu"),
                        PinyinWord("hǎo", "好", "hello", "halo"),
                        PinyinWord("zuò", "坐", "sit", "duduk"),
                        PinyinWord("zài", "在", "at", "di"),
                        PinyinWord("wǒ", "我", "my", "saya"),
                        PinyinWord("pángbiān", "旁边", "next to", "sebelah"),
                        PinyinWord("Wǒ", "我", "I", "Saya"),
                        PinyinWord("jiào", "叫", "am called", "bernama"),
                        PinyinWord("Liú", "刘", "Liu", "Liu"),
                        PinyinWord("Míng", "明", "Ming", "Ming")
                    ),
                    responseType = com.ikhwan.mandarinkids.data.models.ResponseType.LISTEN_ONLY
                ),
                DialogueStep(
                    id = 2,
                    speaker = com.ikhwan.mandarinkids.data.models.Speaker.CHARACTER,
                    textChinese = "你叫什么名字？",
                    textPinyin = "Nǐ jiào shénme míngzi?",
                    textEnglish = "What's your name?",
                    textIndonesian = "Siapa nama kamu?",
                    pinyinWords = listOf(
                        PinyinWord("Nǐ", "你", "You", "Kamu"),
                        PinyinWord("jiào", "叫", "are called", "bernama"),
                        PinyinWord("shénme", "什么", "what", "apa"),
                        PinyinWord("míngzi", "名字", "name", "nama")
                    ),
                    responseType = com.ikhwan.mandarinkids.data.models.ResponseType.SINGLE_CHOICE,
                    userNameInput = true,
                    options = listOf(
                        ResponseOption(
                            "你好，刘明！我叫",
                            "Nǐ hǎo, Liú Míng! Wǒ jiào",
                            "Hi Liu Ming! I'm",
                            "Hai Liu Ming! Nama saya",
                            pinyinWords = listOf(
                                PinyinWord("Nǐ", "你", "You", "Kamu"),
                                PinyinWord("hǎo", "好", "hello", "halo"),
                                PinyinWord("Liú", "刘", "Liu", "Liu"),
                                PinyinWord("Míng", "明", "Ming", "Ming"),
                                PinyinWord("Wǒ", "我", "I", "Saya"),
                                PinyinWord("jiào", "叫", "am called", "bernama")
                            )
                        )
                    )
                ),
                DialogueStep(
                    id = 3,
                    speaker = Speaker.CHARACTER,
                    textChinese = "很高兴认识你！你想做朋友吗？",
                    textPinyin = "Hěn gāoxìng rènshi nǐ! Nǐ xiǎng zuò péngyou ma?",
                    textEnglish = "Nice to meet you! Do you want to be friends?",
                    textIndonesian = "Senang bertemu denganmu! Mau jadi teman?",
                    pinyinWords = listOf(
                        PinyinWord("Hěn", "很", "Very", "Sangat"),
                        PinyinWord("gāoxìng", "高兴", "happy", "senang"),
                        PinyinWord("rènshi", "认识", "to meet", "bertemu"),
                        PinyinWord("nǐ", "你", "you", "kamu"),
                        PinyinWord("Nǐ", "你", "You", "Kamu"),
                        PinyinWord("xiǎng", "想", "want", "mau"),
                        PinyinWord("zuò", "做", "be/become", "jadi"),
                        PinyinWord("péngyou", "朋友", "friends", "teman"),
                        PinyinWord("ma", "吗", "?", "?")
                    ),
                    responseType = ResponseType.SINGLE_CHOICE,
                    options = listOf(
                        ResponseOption(
                            "好的！我很想！",
                            "Hǎo de! Wǒ hěn xiǎng!",
                            "Yes! I'd like that!",
                            "Ya! Saya mau!",
                            pinyinWords = listOf(
                                PinyinWord("Hǎo", "好", "Good", "Baik"),
                                PinyinWord("de", "的", "(particle)", "(partikel)"),
                                PinyinWord("Wǒ", "我", "I", "Saya"),
                                PinyinWord("hěn", "很", "very", "sangat"),
                                PinyinWord("xiǎng", "想", "want", "mau")
                            )
                        )
                    )
                )
            ),
            quizQuestions = listOf(
                QuizQuestion(
                    direction = com.ikhwan.mandarinkids.data.models.QuizDirection.CHINESE_TO_TRANSLATION,
                    questionText = "What does this mean?",
                    questionChinese = "很高兴认识你",
                    questionPinyin = "Hěn gāoxìng rènshi nǐ",
                    options = listOf(
                        QuizOption("", "", "Goodbye"),
                        QuizOption("", "", "Nice to meet you"),
                        QuizOption("", "", "What's your name?"),
                        QuizOption("", "", "Thank you")
                    ),
                    correctAnswerIndex = 1,
                    explanation = "很高兴认识你 (Hěn gāoxìng rènshi nǐ) means 'Nice to meet you'"
                ),
                QuizQuestion(
                    direction = com.ikhwan.mandarinkids.data.models.QuizDirection.TRANSLATION_TO_CHINESE,
                    questionText = "How do you say 'friend' in Mandarin?",
                    options = listOf(
                        QuizOption("老师", "Lǎoshī", "teacher"),
                        QuizOption("朋友", "Péngyou", "friend"),
                        QuizOption("同学", "Tóngxué", "classmate"),
                        QuizOption("名字", "Míngzi", "name")
                    ),
                    correctAnswerIndex = 1,
                    explanation = "朋友 (Péngyou) means 'friend'"
                ),
                QuizQuestion(
                    direction = QuizDirection.CHINESE_TO_TRANSLATION,
                    questionText = "What does this mean?",
                    questionChinese = "你叫什么名字？",
                    questionPinyin = "Nǐ jiào shénme míngzi?",
                    options = listOf(
                        QuizOption("", "", "How are you?"),
                        QuizOption("", "", "What's your name?"),
                        QuizOption("", "", "Where are you from?"),
                        QuizOption("", "", "Do you want to be friends?")
                    ),
                    correctAnswerIndex = 1,
                    explanation = "你叫什么名字？(Nǐ jiào shénme míngzi?) means 'What's your name?'"
                )
            )
        )
}