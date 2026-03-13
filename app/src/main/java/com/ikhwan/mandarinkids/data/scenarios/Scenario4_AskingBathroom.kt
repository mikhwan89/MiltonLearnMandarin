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

fun getScenario4_AskingBathroom(): Scenario {
    return Scenario(
        id = "scene4_asking_help",
        title = "请求帮助",
        description = "Asking teacher for permission politely",
        characterName = "Wang 老师",
        characterEmoji = "👨‍🏫",
        characterRole = "teacher",
        dialogues = listOf(
            DialogueStep(
                id = 1,
                speaker = com.ikhwan.mandarinkids.data.models.Speaker.CHARACTER,
                textChinese = "有什么事吗？",
                textPinyin = "Yǒu shénme shì ma?",
                textEnglish = "Yes? What do you need?",
                textIndonesian = "Ya? Ada apa?",
                pinyinWords = listOf(
                    PinyinWord("Yǒu", "有", "Have", "Ada"),
                    PinyinWord("shénme", "什么", "what", "apa"),
                    PinyinWord("shì", "事", "matter/thing", "hal"),
                    PinyinWord("ma", "吗", "?", "?")
                ),
                responseType = com.ikhwan.mandarinkids.data.models.ResponseType.MULTIPLE_OPTIONS,
                options = listOf(
                    ResponseOption(
                        "老师，我可以去洗手间吗？",
                        "Lǎoshī, wǒ kěyǐ qù xǐshǒujiān ma?",
                        "Teacher, may I go to the restroom?",
                        "Guru, boleh saya ke toilet?",
                        pinyinWords = listOf(
                            PinyinWord("Lǎoshī", "老师", "Teacher", "Guru"),
                            PinyinWord("wǒ", "我", "I", "saya"),
                            PinyinWord("kěyǐ", "可以", "may/can", "boleh"),
                            PinyinWord("qù", "去", "go", "pergi"),
                            PinyinWord("xǐshǒujiān", "洗手间", "restroom", "toilet"),
                            PinyinWord("ma", "吗", "?", "?")
                        )
                    ),
                    ResponseOption(
                        "老师，我要去厕所",
                        "Lǎoshī, wǒ yào qù cèsuǒ",
                        "Teacher, I need to go to the bathroom",
                        "Guru, saya mau ke kamar mandi",
                        pinyinWords = listOf(
                            PinyinWord("Lǎoshī", "老师", "Teacher", "Guru"),
                            PinyinWord("wǒ", "我", "I", "saya"),
                            PinyinWord("yào", "要", "need/want", "mau"),
                            PinyinWord("qù", "去", "go", "pergi"),
                            PinyinWord("cèsuǒ", "厕所", "bathroom", "kamar mandi")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 2,
                speaker = Speaker.CHARACTER,
                textChinese = "可以。快去快回。",
                textPinyin = "Kěyǐ. Kuài qù kuài huí.",
                textEnglish = "Yes, you may. Go quickly and come back quickly.",
                textIndonesian = "Boleh. Cepat pergi cepat kembali.",
                pinyinWords = listOf(
                    PinyinWord("Kěyǐ", "可以", "Can/May", "Boleh"),
                    PinyinWord("Kuài", "快", "Quick", "Cepat"),
                    PinyinWord("qù", "去", "go", "pergi"),
                    PinyinWord("kuài", "快", "quick", "cepat"),
                    PinyinWord("huí", "回", "return", "kembali")
                ),
                responseType = ResponseType.SINGLE_CHOICE,
                options = listOf(
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
                questionText = "How do you say 'May I...' (asking permission) in Mandarin?",
                options = listOf(
                    QuizOption("我要", "Wǒ yào", "I want"),
                    QuizOption("我可以", "Wǒ kěyǐ", "May I"),
                    QuizOption("我有", "Wǒ yǒu", "I have"),
                    QuizOption("我是", "Wǒ shì", "I am")
                ),
                correctAnswerIndex = 1,
                explanation = "我可以 (Wǒ kěyǐ) means 'May I' - more polite than 我要"
            ),
            QuizQuestion(
                direction = com.ikhwan.mandarinkids.data.models.QuizDirection.CHINESE_TO_TRANSLATION,
                questionText = "What does this mean?",
                questionChinese = "洗手间",
                questionPinyin = "Xǐshǒujiān",
                options = listOf(
                    QuizOption("", "", "classroom"),
                    QuizOption("", "", "restroom"),
                    QuizOption("", "", "cafeteria"),
                    QuizOption("", "", "playground")
                ),
                correctAnswerIndex = 1,
                explanation = "洗手间 (Xǐshǒujiān) means 'restroom'"
            ),
            QuizQuestion(
                direction = QuizDirection.TRANSLATION_TO_CHINESE,
                questionText = "How do you say 'teacher' in Mandarin?",
                options = listOf(
                    QuizOption("同学", "Tóngxué", "classmate"),
                    QuizOption("老师", "Lǎoshī", "teacher"),
                    QuizOption("朋友", "Péngyou", "friend"),
                    QuizOption("学生", "Xuésheng", "student")
                ),
                correctAnswerIndex = 1,
                explanation = "老师 (Lǎoshī) means 'teacher'"
            )
        )
    )
}