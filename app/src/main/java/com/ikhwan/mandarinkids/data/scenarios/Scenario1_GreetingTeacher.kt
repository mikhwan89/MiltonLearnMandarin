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

fun getScenario1_GreetingTeacher(): Scenario {
    return Scenario(
            id = "scene1_greeting_teacher",
            title = "第一天：问候老师",
            description = "First day at school - greeting your teacher",
            characterName = "Wang 老师",
            characterEmoji = "👨‍🏫",
            characterRole = "teacher",
            dialogues = listOf(
                DialogueStep(
                    id = 1,
                    speaker = com.ikhwan.mandarinkids.data.models.Speaker.CHARACTER,
                    textChinese = "早上好！你一定是新同学。欢迎！",
                    textPinyin = "Zǎoshang hǎo! Nǐ yīdìng shì xīn tóngxué. Huānyíng!",
                    textEnglish = "Good morning! You must be the new student. Welcome!",
                    textIndonesian = "Selamat pagi! Kamu pasti siswa baru. Selamat datang!",
                    pinyinWords = listOf(
                        PinyinWord("Zǎoshang", "早上", "Morning", "Pagi"),
                        PinyinWord("hǎo", "好", "good", "baik"),
                        PinyinWord("Nǐ", "你", "You", "Kamu"),
                        PinyinWord("yīdìng", "一定", "must", "pasti"),
                        PinyinWord("shì", "是", "are", "adalah"),
                        PinyinWord("xīn", "新", "new", "baru"),
                        PinyinWord("tóngxué", "同学", "student", "siswa"),
                        PinyinWord("Huānyíng", "欢迎", "Welcome", "Selamat datang")
                    ),
                    responseType = com.ikhwan.mandarinkids.data.models.ResponseType.LISTEN_ONLY
                ),
                DialogueStep(
                    id = 2,
                    speaker = com.ikhwan.mandarinkids.data.models.Speaker.CHARACTER,
                    textChinese = "你叫什么名字？",
                    textPinyin = "Nǐ jiào shénme míngzi?",
                    textEnglish = "What is your name?",
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
                            "早上好，老师！我叫",
                            "Zǎoshang hǎo, lǎoshī! Wǒ jiào",
                            "Good morning, teacher! My name is",
                            "Selamat pagi, guru! Nama saya",
                            pinyinWords = listOf(
                                PinyinWord("Zǎoshang", "早上", "Morning", "Pagi"),
                                PinyinWord("hǎo", "好", "good", "baik"),
                                PinyinWord("lǎoshī", "老师", "teacher", "guru"),
                                PinyinWord("Wǒ", "我", "I", "Saya"),
                                PinyinWord("jiào", "叫", "am called", "bernama")
                            )
                        )
                    )
                ),
                DialogueStep(
                    id = 3,
                    speaker = Speaker.CHARACTER,
                    textChinese = "你很有礼貌！很高兴认识你！",
                    textPinyin = "Nǐ hěn yǒu lǐmào! Hěn gāoxìng rènshi nǐ!",
                    textEnglish = "You're very polite! Nice to meet you!",
                    textIndonesian = "Kamu sangat sopan! Senang bertemu denganmu!",
                    pinyinWords = listOf(
                        PinyinWord("Nǐ", "你", "You", "Kamu"),
                        PinyinWord("hěn", "很", "very", "sangat"),
                        PinyinWord("yǒu", "有", "have", "punya"),
                        PinyinWord("lǐmào", "礼貌", "polite", "sopan"),
                        PinyinWord("Hěn", "很", "Very", "Sangat"),
                        PinyinWord("gāoxìng", "高兴", "happy", "senang"),
                        PinyinWord("rènshi", "认识", "to meet", "bertemu"),
                        PinyinWord("nǐ", "你", "you", "kamu")
                    ),
                    responseType = ResponseType.MULTIPLE_OPTIONS,
                    options = listOf(
                        ResponseOption(
                            "很高兴认识你！",
                            "Hěn gāoxìng rènshi nǐ!",
                            "Nice to meet you!",
                            "Senang bertemu denganmu!",
                            pinyinWords = listOf(
                                PinyinWord("Hěn", "很", "Very", "Sangat"),
                                PinyinWord("gāoxìng", "高兴", "happy", "senang"),
                                PinyinWord("rènshi", "认识", "to meet", "bertemu"),
                                PinyinWord("nǐ", "你", "you", "kamu")
                            )
                        ),
                        ResponseOption(
                            "谢谢老师！",
                            "Xièxie lǎoshī!",
                            "Thank you, teacher!",
                            "Terima kasih, guru!",
                            pinyinWords = listOf(
                                PinyinWord("Xièxie", "谢谢", "Thank you", "Terima kasih"),
                                PinyinWord("lǎoshī", "老师", "teacher", "guru")
                            )
                        )
                    )
                )
            ),
            quizQuestions = listOf(
                QuizQuestion(
                    direction = com.ikhwan.mandarinkids.data.models.QuizDirection.TRANSLATION_TO_CHINESE,
                    questionText = "How do you say 'Good morning, teacher' in Mandarin?",
                    options = listOf(
                        QuizOption("早上好，老师", "Zǎoshang hǎo, lǎoshī", "Good morning, teacher"),
                        QuizOption("你好", "Nǐ hǎo", "Hello"),
                        QuizOption("再见", "Zàijiàn", "Goodbye"),
                        QuizOption("谢谢", "Xièxie", "Thank you")
                    ),
                    correctAnswerIndex = 0,
                    explanation = "早上好，老师 (Zǎoshang hǎo, lǎoshī) means 'Good morning, teacher'"
                ),
                QuizQuestion(
                    direction = com.ikhwan.mandarinkids.data.models.QuizDirection.CHINESE_TO_TRANSLATION,
                    questionText = "What does this mean?",
                    questionChinese = "很高兴认识你",
                    questionPinyin = "Hěn gāoxìng rènshi nǐ",
                    options = listOf(
                        QuizOption("", "", "Goodbye"),
                        QuizOption("", "", "Nice to meet you"),
                        QuizOption("", "", "Thank you"),
                        QuizOption("", "", "Good morning")
                    ),
                    correctAnswerIndex = 1,
                    explanation = "很高兴认识你 (Hěn gāoxìng rènshi nǐ) means 'Nice to meet you'"
                ),
                QuizQuestion(
                    direction = QuizDirection.TRANSLATION_TO_CHINESE,
                    questionText = "How do you say 'Thank you' in Mandarin?",
                    options = listOf(
                        QuizOption("再见", "Zàijiàn", "Goodbye"),
                        QuizOption("谢谢", "Xièxie", "Thank you"),
                        QuizOption("对不起", "Duìbuqǐ", "Sorry"),
                        QuizOption("你好", "Nǐ hǎo", "Hello")
                    ),
                    correctAnswerIndex = 1,
                    explanation = "谢谢 (Xièxie) means 'Thank you'"
                )
            )
        )
}