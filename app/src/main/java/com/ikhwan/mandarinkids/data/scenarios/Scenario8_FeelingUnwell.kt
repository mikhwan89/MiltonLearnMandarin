package com.ikhwan.mandarinkids.data.scenarios

import com.ikhwan.mandarinkids.data.models.*

fun getScenario8_FeelingUnwell(): Scenario {
    return Scenario(
        id = "scene8_feeling_unwell",
        title = "我不舒服",
        description = "Telling the teacher you feel unwell — stomachache or headache",
        characterName = "Li 老师",
        characterEmoji = "👩‍🏫",
        characterRole = "teacher",
        dialogues = listOf(
            DialogueStep(
                id = 1,
                speaker = Speaker.STUDENT,
                textChinese = "老师，我不舒服。",
                textPinyin = "Lǎoshī, wǒ bù shūfu.",
                textEnglish = "Teacher, I don't feel well.",
                textIndonesian = "Guru, saya tidak enak badan.",
                pinyinWords = listOf(
                    PinyinWord("lǎoshī", "老师", "teacher", "guru"),
                    PinyinWord("bù", "不", "not", "tidak"),
                    PinyinWord("shūfu", "舒服", "comfortable / well", "nyaman / sehat")
                ),
                responseType = ResponseType.LISTEN_ONLY
            ),
            DialogueStep(
                id = 2,
                speaker = Speaker.CHARACTER,
                textChinese = "哪里不舒服？是头疼还是肚子疼？",
                textPinyin = "Nǎlǐ bù shūfu? Shì tóuténg háishì dùzi téng?",
                textEnglish = "Where does it hurt? Is it a headache or stomachache?",
                textIndonesian = "Di mana sakitnya? Kepala atau perut?",
                pinyinWords = listOf(
                    PinyinWord("nǎlǐ", "哪里", "where", "di mana"),
                    PinyinWord("tóuténg", "头疼", "headache", "sakit kepala"),
                    PinyinWord("háishì", "还是", "or", "atau"),
                    PinyinWord("dùzi téng", "肚子疼", "stomachache", "sakit perut")
                ),
                responseType = ResponseType.MULTIPLE_OPTIONS,
                options = listOf(
                    ResponseOption(
                        "我肚子疼。",
                        "Wǒ dùzi téng.",
                        "I have a stomachache.",
                        "Perut saya sakit.",
                        pinyinWords = listOf(
                            PinyinWord("dùzi téng", "肚子疼", "stomachache", "sakit perut")
                        )
                    ),
                    ResponseOption(
                        "我头疼。",
                        "Wǒ tóuténg.",
                        "I have a headache.",
                        "Kepala saya sakit.",
                        pinyinWords = listOf(
                            PinyinWord("tóuténg", "头疼", "headache", "sakit kepala")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 3,
                speaker = Speaker.CHARACTER,
                textChinese = "你需要去医务室吗？",
                textPinyin = "Nǐ xūyào qù yīwùshì ma?",
                textEnglish = "Do you need to go to the clinic?",
                textIndonesian = "Kamu perlu ke UKS?",
                pinyinWords = listOf(
                    PinyinWord("xūyào", "需要", "need", "perlu"),
                    PinyinWord("qù", "去", "go", "pergi"),
                    PinyinWord("yīwùshì", "医务室", "clinic / sick bay", "UKS")
                ),
                responseType = ResponseType.MULTIPLE_OPTIONS,
                options = listOf(
                    ResponseOption(
                        "是的，我想去医务室。",
                        "Shì de, wǒ xiǎng qù yīwùshì.",
                        "Yes, I'd like to go to the clinic.",
                        "Iya, saya mau ke UKS.",
                        pinyinWords = listOf(
                            PinyinWord("shì de", "是的", "yes", "iya"),
                            PinyinWord("xiǎng", "想", "want to", "mau"),
                            PinyinWord("yīwùshì", "医务室", "clinic", "UKS")
                        )
                    ),
                    ResponseOption(
                        "没关系，我喝点水就好了。",
                        "Méi guānxi, wǒ hē diǎn shuǐ jiù hǎo le.",
                        "It's okay, I just need some water.",
                        "Tidak apa-apa, minum air saja sudah cukup.",
                        pinyinWords = listOf(
                            PinyinWord("méi guānxi", "没关系", "it's okay", "tidak apa-apa"),
                            PinyinWord("hē", "喝", "drink", "minum"),
                            PinyinWord("shuǐ", "水", "water", "air")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 4,
                speaker = Speaker.CHARACTER,
                textChinese = "好的，我陪你去。希望你快点好起来！",
                textPinyin = "Hǎo de, wǒ péi nǐ qù. Xīwàng nǐ kuài diǎn hǎo qǐlái!",
                textEnglish = "Okay, I'll take you there. I hope you feel better soon!",
                textIndonesian = "Baik, saya antar. Semoga cepat sembuh!",
                pinyinWords = listOf(
                    PinyinWord("péi", "陪", "accompany", "menemani"),
                    PinyinWord("xīwàng", "希望", "hope", "semoga"),
                    PinyinWord("kuài diǎn", "快点", "quickly / soon", "cepat"),
                    PinyinWord("hǎo qǐlái", "好起来", "get better", "sembuh")
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
                direction = QuizDirection.TRANSLATION_TO_CHINESE,
                questionText = "How do you say 'I don't feel well' in Mandarin?",
                options = listOf(
                    QuizOption("我很好。", "Wǒ hěn hǎo.", "I'm fine."),
                    QuizOption("我不舒服。", "Wǒ bù shūfu.", "I don't feel well."),
                    QuizOption("我很累。", "Wǒ hěn lèi.", "I'm very tired."),
                    QuizOption("我饿了。", "Wǒ è le.", "I'm hungry.")
                ),
                correctAnswerIndex = 1,
                explanation = "我不舒服 (Wǒ bù shūfu) means 'I don't feel well'."
            ),
            QuizQuestion(
                direction = QuizDirection.CHINESE_TO_TRANSLATION,
                questionText = "What does this mean?",
                questionChinese = "头疼",
                questionPinyin = "tóuténg",
                options = listOf(
                    QuizOption("", "", "stomachache"),
                    QuizOption("", "", "headache"),
                    QuizOption("", "", "toothache"),
                    QuizOption("", "", "fever")
                ),
                correctAnswerIndex = 1,
                explanation = "头疼 (tóuténg) means headache. 头 = head, 疼 = pain."
            ),
            QuizQuestion(
                direction = QuizDirection.TRANSLATION_TO_CHINESE,
                questionText = "How do you say 'stomachache' in Mandarin?",
                options = listOf(
                    QuizOption("头疼", "tóuténg", "headache"),
                    QuizOption("肚子疼", "dùzi téng", "stomachache"),
                    QuizOption("发烧", "fāshāo", "fever"),
                    QuizOption("咳嗽", "késou", "cough")
                ),
                correctAnswerIndex = 1,
                explanation = "肚子疼 (dùzi téng) means stomachache. 肚子 = stomach, 疼 = pain."
            )
        )
    )
}
