package com.ikhwan.mandarinkids.data.scenarios

import com.ikhwan.mandarinkids.data.models.*

fun getScenario10_SayingSorry(): Scenario {
    return Scenario(
        id = "scene10_saying_sorry",
        title = "解决争吵",
        description = "Resolving a disagreement — saying sorry and making up with a friend",
        characterName = "朋友小红",
        characterEmoji = "🧒",
        characterRole = "friend",
        dialogues = listOf(
            DialogueStep(
                id = 1,
                speaker = Speaker.CHARACTER,
                textChinese = "你弄坏了我的铅笔！我很不高兴！",
                textPinyin = "Nǐ nòng huài le wǒ de qiānbǐ! Wǒ hěn bù gāoxìng!",
                textEnglish = "You broke my pencil! I'm very unhappy!",
                textIndonesian = "Kamu merusak pensilku! Aku sangat tidak senang!",
                pinyinWords = listOf(
                    PinyinWord("nòng huài", "弄坏", "broke / damaged", "merusak"),
                    PinyinWord("bù gāoxìng", "不高兴", "unhappy", "tidak senang")
                ),
                responseType = ResponseType.LISTEN_ONLY
            ),
            DialogueStep(
                id = 2,
                speaker = Speaker.STUDENT,
                textChinese = "对不起！我不是故意的。",
                textPinyin = "Duìbuqǐ! Wǒ bù shì gùyì de.",
                textEnglish = "Sorry! I didn't do it on purpose.",
                textIndonesian = "Maaf! Aku tidak sengaja.",
                pinyinWords = listOf(
                    PinyinWord("duìbuqǐ", "对不起", "sorry", "maaf"),
                    PinyinWord("gùyì", "故意", "on purpose", "sengaja")
                ),
                responseType = ResponseType.LISTEN_ONLY
            ),
            DialogueStep(
                id = 3,
                speaker = Speaker.CHARACTER,
                textChinese = "你会赔我一支新铅笔吗？",
                textPinyin = "Nǐ huì péi wǒ yī zhī xīn qiānbǐ ma?",
                textEnglish = "Will you get me a new pencil?",
                textIndonesian = "Kamu akan mengganti pensilku yang baru?",
                pinyinWords = listOf(
                    PinyinWord("huì", "会", "will", "akan"),
                    PinyinWord("péi", "赔", "compensate / replace", "mengganti"),
                    PinyinWord("zhī", "支", "measure word for pencils", "satuan untuk pensil"),
                    PinyinWord("xīn", "新", "new", "baru")
                ),
                responseType = ResponseType.MULTIPLE_OPTIONS,
                options = listOf(
                    ResponseOption(
                        "好的，我明天带一支新的来。再一次对不起！",
                        "Hǎo de, wǒ míngtiān dài yī zhī xīn de lái. Zài yī cì duìbuqǐ!",
                        "Okay, I'll bring a new one tomorrow. I'm really sorry!",
                        "Baik, aku bawa yang baru besok. Sekali lagi maaf!",
                        pinyinWords = listOf(
                            PinyinWord("míngtiān", "明天", "tomorrow", "besok"),
                            PinyinWord("dài", "带", "bring", "bawa"),
                            PinyinWord("zài yī cì", "再一次", "once more / again", "sekali lagi")
                        )
                    ),
                    ResponseOption(
                        "我不知道。对不起。",
                        "Wǒ bù zhīdào. Duìbuqǐ.",
                        "I don't know. Sorry.",
                        "Aku tidak tahu. Maaf.",
                        pinyinWords = listOf(
                            PinyinWord("bù zhīdào", "不知道", "don't know", "tidak tahu"),
                            PinyinWord("duìbuqǐ", "对不起", "sorry", "maaf")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 4,
                speaker = Speaker.CHARACTER,
                textChinese = "好吧，没关系。我们还是好朋友！",
                textPinyin = "Hǎo ba, méi guānxi. Wǒmen háishì hǎo péngyou!",
                textEnglish = "Alright, it's okay. We're still good friends!",
                textIndonesian = "Baiklah, tidak apa-apa. Kita masih teman baik!",
                pinyinWords = listOf(
                    PinyinWord("hǎo ba", "好吧", "alright", "baiklah"),
                    PinyinWord("méi guānxi", "没关系", "it's okay", "tidak apa-apa"),
                    PinyinWord("háishì", "还是", "still", "masih"),
                    PinyinWord("hǎo péngyou", "好朋友", "good friends", "teman baik")
                ),
                responseType = ResponseType.SINGLE_CHOICE,
                options = listOf(
                    ResponseOption(
                        "谢谢你！我们是最好的朋友！",
                        "Xièxie nǐ! Wǒmen shì zuì hǎo de péngyou!",
                        "Thank you! We are the best friends!",
                        "Terima kasih! Kita teman terbaik!",
                        pinyinWords = listOf(
                            PinyinWord("Xièxie", "谢谢", "Thank you", "Terima kasih"),
                            PinyinWord("zuì hǎo", "最好", "best", "terbaik"),
                            PinyinWord("péngyou", "朋友", "friends", "teman")
                        )
                    )
                )
            )
        ),
        quizQuestions = listOf(
            QuizQuestion(
                direction = QuizDirection.TRANSLATION_TO_CHINESE,
                questionText = "How do you say 'I didn't do it on purpose' in Mandarin?",
                options = listOf(
                    QuizOption("我知道。", "Wǒ zhīdào.", "I know."),
                    QuizOption("我不是故意的。", "Wǒ bù shì gùyì de.", "I didn't do it on purpose."),
                    QuizOption("对不起！", "Duìbuqǐ!", "Sorry!"),
                    QuizOption("没关系。", "Méi guānxi.", "It's okay.")
                ),
                correctAnswerIndex = 1,
                explanation = "我不是故意的 (Wǒ bù shì gùyì de) means 'I didn't do it on purpose'."
            ),
            QuizQuestion(
                direction = QuizDirection.CHINESE_TO_TRANSLATION,
                questionText = "What does this mean?",
                questionChinese = "明天",
                questionPinyin = "míngtiān",
                options = listOf(
                    QuizOption("", "", "today"),
                    QuizOption("", "", "yesterday"),
                    QuizOption("", "", "tomorrow"),
                    QuizOption("", "", "now")
                ),
                correctAnswerIndex = 2,
                explanation = "明天 (míngtiān) means tomorrow."
            ),
            QuizQuestion(
                direction = QuizDirection.TRANSLATION_TO_CHINESE,
                questionText = "How do you say 'best friends' in Mandarin?",
                options = listOf(
                    QuizOption("好朋友", "hǎo péngyou", "good friends"),
                    QuizOption("最好的朋友", "zuì hǎo de péngyou", "best friends"),
                    QuizOption("新朋友", "xīn péngyou", "new friends"),
                    QuizOption("老朋友", "lǎo péngyou", "old friends")
                ),
                correctAnswerIndex = 1,
                explanation = "最好的朋友 (zuì hǎo de péngyou) means 'best friends'. 最 = most/best."
            )
        )
    )
}
