package com.ikhwan.mandarinkids.data.scenarios

import com.ikhwan.mandarinkids.data.models.*

fun getScenario11_AfterSchool(): Scenario {
    return Scenario(
        id = "scene11_after_school",
        title = "回家了",
        description = "End of school day — telling your parents what you learned and did",
        characterName = "妈妈",
        characterEmoji = "👩",
        characterRole = "mum",
        dialogues = listOf(
            DialogueStep(
                id = 1,
                speaker = Speaker.CHARACTER,
                textChinese = "宝贝，回来了！今天学校怎么样？",
                textPinyin = "Bǎobèi, huí lái le! Jīntiān xuéxiào zěnmeyàng?",
                textEnglish = "Sweetie, you're back! How was school today?",
                textIndonesian = "Sayang, sudah pulang! Bagaimana sekolah hari ini?",
                pinyinWords = listOf(
                    PinyinWord("bǎobèi", "宝贝", "sweetheart / baby", "sayang"),
                    PinyinWord("jīntiān", "今天", "today", "hari ini"),
                    PinyinWord("xuéxiào", "学校", "school", "sekolah"),
                    PinyinWord("zěnmeyàng", "怎么样", "how was / how is", "bagaimana")
                ),
                responseType = ResponseType.MULTIPLE_OPTIONS,
                options = listOf(
                    ResponseOption(
                        "很好！我今天学了很多中文。",
                        "Hěn hǎo! Wǒ jīntiān xué le hěn duō zhōngwén.",
                        "Very good! I learned a lot of Chinese today.",
                        "Sangat baik! Aku belajar banyak bahasa Mandarin hari ini.",
                        pinyinWords = listOf(
                            PinyinWord("xué", "学", "learn", "belajar"),
                            PinyinWord("hěn duō", "很多", "a lot", "banyak"),
                            PinyinWord("zhōngwén", "中文", "Chinese language", "bahasa Mandarin")
                        )
                    ),
                    ResponseOption(
                        "还好。有点累。",
                        "Hái hǎo. Yǒudiǎn lèi.",
                        "It was okay. A little tired.",
                        "Lumayan. Agak capek.",
                        pinyinWords = listOf(
                            PinyinWord("hái hǎo", "还好", "okay / not bad", "lumayan"),
                            PinyinWord("yǒudiǎn", "有点", "a little", "agak"),
                            PinyinWord("lèi", "累", "tired", "capek")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 2,
                speaker = Speaker.CHARACTER,
                textChinese = "太棒了！你认识了新朋友吗？",
                textPinyin = "Tài bàng le! Nǐ rènshi le xīn péngyou ma?",
                textEnglish = "Wonderful! Did you make any new friends?",
                textIndonesian = "Luar biasa! Kamu kenal teman baru?",
                pinyinWords = listOf(
                    PinyinWord("tài bàng le", "太棒了", "wonderful / amazing", "luar biasa"),
                    PinyinWord("rènshi", "认识", "know / meet", "kenal"),
                    PinyinWord("xīn", "新", "new", "baru"),
                    PinyinWord("péngyou", "朋友", "friends", "teman")
                ),
                responseType = ResponseType.MULTIPLE_OPTIONS,
                options = listOf(
                    ResponseOption(
                        "认识了！我认识了一个叫小明的朋友。",
                        "Rènshi le! Wǒ rènshi le yī gè jiào Xiǎo Míng de péngyou.",
                        "Yes! I made a friend called Xiao Ming.",
                        "Kenal! Aku kenal satu teman bernama Xiao Ming.",
                        pinyinWords = listOf(
                            PinyinWord("rènshi le", "认识了", "made friends with", "berkenalan"),
                            PinyinWord("jiào", "叫", "named / called", "bernama")
                        )
                    ),
                    ResponseOption(
                        "还没有，但是我认识了老师。",
                        "Hái méiyǒu, dànshì wǒ rènshi le lǎoshī.",
                        "Not yet, but I met the teacher.",
                        "Belum, tapi aku sudah kenal gurunya.",
                        pinyinWords = listOf(
                            PinyinWord("hái méiyǒu", "还没有", "not yet", "belum"),
                            PinyinWord("dànshì", "但是", "but", "tapi")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 3,
                speaker = Speaker.CHARACTER,
                textChinese = "真好！你喜欢学校吗？",
                textPinyin = "Zhēn hǎo! Nǐ xǐhuān xuéxiào ma?",
                textEnglish = "How nice! Do you like school?",
                textIndonesian = "Bagus sekali! Kamu suka sekolah?",
                pinyinWords = listOf(
                    PinyinWord("zhēn hǎo", "真好", "how nice / really good", "bagus sekali"),
                    PinyinWord("xǐhuān", "喜欢", "like", "suka"),
                    PinyinWord("xuéxiào", "学校", "school", "sekolah")
                ),
                responseType = ResponseType.MULTIPLE_OPTIONS,
                options = listOf(
                    ResponseOption(
                        "喜欢！我明天还想去！",
                        "Xǐhuān! Wǒ míngtiān hái xiǎng qù!",
                        "Yes! I want to go again tomorrow!",
                        "Suka! Aku mau pergi lagi besok!",
                        pinyinWords = listOf(
                            PinyinWord("xǐhuān", "喜欢", "like", "suka"),
                            PinyinWord("míngtiān", "明天", "tomorrow", "besok"),
                            PinyinWord("xiǎng qù", "想去", "want to go", "mau pergi")
                        )
                    ),
                    ResponseOption(
                        "还可以。我有点想你。",
                        "Hái kěyǐ. Wǒ yǒudiǎn xiǎng nǐ.",
                        "It was okay. I missed you a bit.",
                        "Lumayan. Aku agak kangen kamu.",
                        pinyinWords = listOf(
                            PinyinWord("hái kěyǐ", "还可以", "it was okay", "lumayan"),
                            PinyinWord("xiǎng nǐ", "想你", "miss you", "kangen kamu")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 4,
                speaker = Speaker.CHARACTER,
                textChinese = "妈妈也想你！先去换衣服，然后吃饭，好吗？",
                textPinyin = "Māma yě xiǎng nǐ! Xiān qù huàn yīfu, rán hòu chīfàn, hǎo ma?",
                textEnglish = "Mummy missed you too! First go change clothes, then we'll eat, okay?",
                textIndonesian = "Mama juga kangen kamu! Ganti baju dulu, terus makan, ya?",
                pinyinWords = listOf(
                    PinyinWord("māma", "妈妈", "mummy", "mama"),
                    PinyinWord("xiān", "先", "first", "dulu"),
                    PinyinWord("huàn yīfu", "换衣服", "change clothes", "ganti baju"),
                    PinyinWord("rán hòu", "然后", "then / after that", "terus"),
                    PinyinWord("chīfàn", "吃饭", "eat / have a meal", "makan")
                ),
                responseType = ResponseType.SINGLE_CHOICE,
                options = listOf(
                    ResponseOption(
                        "好的，妈妈！我爱你！",
                        "Hǎo de, māma! Wǒ ài nǐ!",
                        "Okay, mummy! I love you!",
                        "Baik, mama! Aku sayang kamu!",
                        pinyinWords = listOf(
                            PinyinWord("hǎo de", "好的", "okay", "baik"),
                            PinyinWord("ài", "爱", "love", "sayang")
                        )
                    )
                )
            )
        ),
        quizQuestions = listOf(
            QuizQuestion(
                direction = QuizDirection.TRANSLATION_TO_CHINESE,
                questionText = "How do you say 'I love you' in Mandarin?",
                options = listOf(
                    QuizOption("我想你。", "Wǒ xiǎng nǐ.", "I miss you."),
                    QuizOption("我喜欢你。", "Wǒ xǐhuān nǐ.", "I like you."),
                    QuizOption("我爱你。", "Wǒ ài nǐ.", "I love you."),
                    QuizOption("我需要你。", "Wǒ xūyào nǐ.", "I need you.")
                ),
                correctAnswerIndex = 2,
                explanation = "我爱你 (Wǒ ài nǐ) means 'I love you'."
            ),
            QuizQuestion(
                direction = QuizDirection.CHINESE_TO_TRANSLATION,
                questionText = "What does this mean?",
                questionChinese = "太棒了",
                questionPinyin = "tài bàng le",
                options = listOf(
                    QuizOption("", "", "not bad"),
                    QuizOption("", "", "wonderful / amazing"),
                    QuizOption("", "", "good morning"),
                    QuizOption("", "", "goodbye")
                ),
                correctAnswerIndex = 1,
                explanation = "太棒了 (tài bàng le) means 'wonderful' or 'amazing'."
            ),
            QuizQuestion(
                direction = QuizDirection.TRANSLATION_TO_CHINESE,
                questionText = "How do you say 'I like school' in Mandarin?",
                options = listOf(
                    QuizOption("我不喜欢学校。", "Wǒ bù xǐhuān xuéxiào.", "I don't like school."),
                    QuizOption("我去学校。", "Wǒ qù xuéxiào.", "I go to school."),
                    QuizOption("我喜欢学校。", "Wǒ xǐhuān xuéxiào.", "I like school."),
                    QuizOption("学校很大。", "Xuéxiào hěn dà.", "The school is big.")
                ),
                correctAnswerIndex = 2,
                explanation = "我喜欢学校 (Wǒ xǐhuān xuéxiào) means 'I like school'."
            )
        )
    )
}
