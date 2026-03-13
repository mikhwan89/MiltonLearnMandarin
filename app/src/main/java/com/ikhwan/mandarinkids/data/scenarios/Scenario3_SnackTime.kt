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

fun getScenario3_SnackTime(): Scenario {
    return Scenario(
            id = "scene3_snack_time",
            title = "零食时间",
            description = "Sharing snacks with Liu Ming",
            characterName = "Liu Ming",
            characterEmoji = "👦",
            characterRole = "friend",
            dialogues = listOf(
                DialogueStep(
                    id = 1,
                    speaker = com.ikhwan.mandarinkids.data.models.Speaker.CHARACTER,
                    textChinese = "你今天带了什么零食？",
                    textPinyin = "Nǐ jīntiān dài le shénme língshí?",
                    textEnglish = "What snack did you bring today?",
                    textIndonesian = "Kamu bawa cemilan apa hari ini?",
                    pinyinWords = listOf(
                        PinyinWord("Nǐ", "你", "You", "Kamu"),
                        PinyinWord("jīntiān", "今天", "today", "hari ini"),
                        PinyinWord("dài", "带", "bring", "bawa"),
                        PinyinWord("le", "了", "(particle)", "(partikel)"),
                        PinyinWord("shénme", "什么", "what", "apa"),
                        PinyinWord("língshí", "零食", "snack", "cemilan")
                    ),
                    responseType = com.ikhwan.mandarinkids.data.models.ResponseType.MULTIPLE_OPTIONS,
                    options = listOf(
                        ResponseOption(
                            "我带了饼干。你要吗？",
                            "Wǒ dài le bǐnggān. Nǐ yào ma?",
                            "I brought cookies. Would you like one?",
                            "Saya bawa biskuit. Kamu mau?",
                            pinyinWords = listOf(
                                PinyinWord("Wǒ", "我", "I", "Saya"),
                                PinyinWord("dài", "带", "brought", "bawa"),
                                PinyinWord("le", "了", "(particle)", "(partikel)"),
                                PinyinWord("bǐnggān", "饼干", "cookies", "biskuit"),
                                PinyinWord("Nǐ", "你", "You", "Kamu"),
                                PinyinWord("yào", "要", "want", "mau"),
                                PinyinWord("ma", "吗", "?", "?")
                            )
                        ),
                        ResponseOption(
                            "我带了苹果。你呢？",
                            "Wǒ dài le píngguǒ. Nǐ ne?",
                            "I have an apple. What about you?",
                            "Saya punya apel. Kamu?",
                            pinyinWords = listOf(
                                PinyinWord("Wǒ", "我", "I", "Saya"),
                                PinyinWord("dài", "带", "brought", "bawa"),
                                PinyinWord("le", "了", "(particle)", "(partikel)"),
                                PinyinWord("píngguǒ", "苹果", "apple", "apel"),
                                PinyinWord("Nǐ", "你", "You", "Kamu"),
                                PinyinWord("ne", "呢", "?", "?")
                            )
                        )
                    )
                ),
                DialogueStep(
                    id = 2,
                    speaker = Speaker.CHARACTER,
                    textChinese = "谢谢你！你真好！",
                    textPinyin = "Xièxie nǐ! Nǐ zhēn hǎo!",
                    textEnglish = "Thank you! You're so nice!",
                    textIndonesian = "Terima kasih! Kamu baik sekali!",
                    pinyinWords = listOf(
                        PinyinWord("Xièxie", "谢谢", "Thank you", "Terima kasih"),
                        PinyinWord("nǐ", "你", "you", "kamu"),
                        PinyinWord("Nǐ", "你", "You", "Kamu"),
                        PinyinWord("zhēn", "真", "really", "benar-benar"),
                        PinyinWord("hǎo", "好", "good/nice", "baik")
                    ),
                    responseType = ResponseType.MULTIPLE_OPTIONS,
                    options = listOf(
                        ResponseOption(
                            "不客气！",
                            "Bú kèqi!",
                            "You're welcome!",
                            "Sama-sama!",
                            pinyinWords = listOf(
                                PinyinWord("Bú", "不", "not", "tidak"),
                                PinyinWord("kèqi", "客气", "polite", "sopan")
                            )
                        ),
                        ResponseOption(
                            "我们是朋友！",
                            "Wǒmen shì péngyou!",
                            "We're friends!",
                            "Kita berteman!",
                            pinyinWords = listOf(
                                PinyinWord("Wǒmen", "我们", "We", "Kita"),
                                PinyinWord("shì", "是", "are", "adalah"),
                                PinyinWord("péngyou", "朋友", "friends", "teman")
                            )
                        )
                    )
                )
            ),
            quizQuestions = listOf(
                QuizQuestion(
                    direction = com.ikhwan.mandarinkids.data.models.QuizDirection.TRANSLATION_TO_CHINESE,
                    questionText = "How do you say 'Thank you' in Mandarin?",
                    options = listOf(
                        QuizOption("你好", "Nǐ hǎo", "Hello"),
                        QuizOption("谢谢", "Xièxie", "Thank you"),
                        QuizOption("对不起", "Duìbuqǐ", "Sorry"),
                        QuizOption("再见", "Zàijiàn", "Goodbye")
                    ),
                    correctAnswerIndex = 1,
                    explanation = "谢谢 (Xièxie) means 'Thank you'"
                ),
                QuizQuestion(
                    direction = com.ikhwan.mandarinkids.data.models.QuizDirection.CHINESE_TO_TRANSLATION,
                    questionText = "What does this mean?",
                    questionChinese = "不客气",
                    questionPinyin = "Bú kèqi",
                    options = listOf(
                        QuizOption("", "", "Thank you"),
                        QuizOption("", "", "You're welcome"),
                        QuizOption("", "", "Sorry"),
                        QuizOption("", "", "Goodbye")
                    ),
                    correctAnswerIndex = 1,
                    explanation = "不客气 (Bú kèqi) means 'You're welcome'"
                ),
                QuizQuestion(
                    direction = QuizDirection.TRANSLATION_TO_CHINESE,
                    questionText = "How do you say 'snack' in Mandarin?",
                    options = listOf(
                        QuizOption("水果", "Shuǐguǒ", "fruit"),
                        QuizOption("零食", "Língshí", "snack"),
                        QuizOption("饼干", "Bǐnggān", "cookie"),
                        QuizOption("苹果", "Píngguǒ", "apple")
                    ),
                    correctAnswerIndex = 1,
                    explanation = "零食 (Língshí) means 'snack'"
                )
            )
        )
}