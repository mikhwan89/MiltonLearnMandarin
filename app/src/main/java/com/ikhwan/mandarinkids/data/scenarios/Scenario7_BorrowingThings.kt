package com.ikhwan.mandarinkids.data.scenarios

import com.ikhwan.mandarinkids.data.models.*

fun getScenario7_BorrowingThings(): Scenario {
    return Scenario(
        id = "scene7_borrowing_things",
        title = "借东西",
        description = "Borrowing things from a classmate — pencil, eraser, ruler",
        characterName = "同学小明",
        characterEmoji = "🧒",
        characterRole = "classmate",
        dialogues = listOf(
            DialogueStep(
                id = 1,
                speaker = Speaker.STUDENT,
                textChinese = "小明，我可以借你的铅笔吗？",
                textPinyin = "Xiǎo Míng, wǒ kěyǐ jiè nǐ de qiānbǐ ma?",
                textEnglish = "Xiao Ming, may I borrow your pencil?",
                textIndonesian = "Xiao Ming, boleh aku pinjam pensilmu?",
                pinyinWords = listOf(
                    PinyinWord("kěyǐ", "可以", "may / can", "boleh"),
                    PinyinWord("jiè", "借", "borrow", "pinjam"),
                    PinyinWord("qiānbǐ", "铅笔", "pencil", "pensil"),
                    PinyinWord("ma", "吗", "question particle", "partikel tanya")
                ),
                responseType = ResponseType.LISTEN_ONLY
            ),
            DialogueStep(
                id = 2,
                speaker = Speaker.CHARACTER,
                textChinese = "当然可以！给你。你还需要别的吗？",
                textPinyin = "Dāngrán kěyǐ! Gěi nǐ. Nǐ hái xūyào bié de ma?",
                textEnglish = "Of course! Here you go. Do you need anything else?",
                textIndonesian = "Tentu saja! Ini untukmu. Kamu butuh yang lain?",
                pinyinWords = listOf(
                    PinyinWord("dāngrán", "当然", "of course", "tentu saja"),
                    PinyinWord("gěi", "给", "give / here", "beri / ini"),
                    PinyinWord("hái", "还", "also / still", "juga"),
                    PinyinWord("xūyào", "需要", "need", "butuh"),
                    PinyinWord("bié de", "别的", "other things", "yang lain")
                ),
                responseType = ResponseType.MULTIPLE_OPTIONS,
                options = listOf(
                    ResponseOption(
                        "谢谢你！我还需要橡皮。",
                        "Xièxie nǐ! Wǒ hái xūyào xiàngpí.",
                        "Thank you! I also need an eraser.",
                        "Terima kasih! Aku juga butuh penghapus.",
                        pinyinWords = listOf(
                            PinyinWord("Xièxie", "谢谢", "Thank you", "Terima kasih"),
                            PinyinWord("xiàngpí", "橡皮", "eraser", "penghapus")
                        )
                    ),
                    ResponseOption(
                        "不用了，谢谢！",
                        "Bù yòng le, xièxie!",
                        "No need, thank you!",
                        "Tidak perlu, terima kasih!",
                        pinyinWords = listOf(
                            PinyinWord("bù yòng", "不用", "no need", "tidak perlu"),
                            PinyinWord("Xièxie", "谢谢", "Thank you", "Terima kasih")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 3,
                speaker = Speaker.CHARACTER,
                textChinese = "给你橡皮。用完了还给我，好吗？",
                textPinyin = "Gěi nǐ xiàngpí. Yòng wán le huán gěi wǒ, hǎo ma?",
                textEnglish = "Here's the eraser. Return it when you're done, okay?",
                textIndonesian = "Ini penghapusnya. Kembalikan kalau sudah selesai, ya?",
                pinyinWords = listOf(
                    PinyinWord("yòng wán", "用完", "finished using", "selesai pakai"),
                    PinyinWord("huán", "还", "return", "kembalikan"),
                    PinyinWord("hǎo ma", "好吗", "okay?", "ya?")
                ),
                responseType = ResponseType.SINGLE_CHOICE,
                options = listOf(
                    ResponseOption(
                        "好的，用完我还给你。谢谢！",
                        "Hǎo de, yòng wán wǒ huán gěi nǐ. Xièxie!",
                        "Okay, I'll return it when I'm done. Thank you!",
                        "Baik, aku kembalikan kalau sudah selesai. Terima kasih!",
                        pinyinWords = listOf(
                            PinyinWord("hǎo de", "好的", "okay", "baik"),
                            PinyinWord("huán gěi", "还给", "return to", "kembalikan ke"),
                            PinyinWord("Xièxie", "谢谢", "Thank you", "Terima kasih")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 4,
                speaker = Speaker.CHARACTER,
                textChinese = "不客气！我们是好朋友嘛。",
                textPinyin = "Bù kèqi! Wǒmen shì hǎo péngyou ma.",
                textEnglish = "You're welcome! We're good friends after all.",
                textIndonesian = "Sama-sama! Kita kan teman baik.",
                pinyinWords = listOf(
                    PinyinWord("bù kèqi", "不客气", "you're welcome", "sama-sama"),
                    PinyinWord("wǒmen", "我们", "we", "kita"),
                    PinyinWord("hǎo péngyou", "好朋友", "good friends", "teman baik")
                ),
                responseType = ResponseType.LISTEN_ONLY
            )
        ),
        quizQuestions = listOf(
            QuizQuestion(
                direction = QuizDirection.TRANSLATION_TO_CHINESE,
                questionText = "How do you say 'May I borrow your pencil?' in Mandarin?",
                options = listOf(
                    QuizOption("我可以借你的铅笔吗？", "Wǒ kěyǐ jiè nǐ de qiānbǐ ma?", "May I borrow your pencil?"),
                    QuizOption("我要买铅笔。", "Wǒ yào mǎi qiānbǐ.", "I want to buy a pencil."),
                    QuizOption("给我铅笔！", "Gěi wǒ qiānbǐ!", "Give me the pencil!"),
                    QuizOption("我有铅笔。", "Wǒ yǒu qiānbǐ.", "I have a pencil.")
                ),
                correctAnswerIndex = 0,
                explanation = "我可以借你的铅笔吗 (Wǒ kěyǐ jiè nǐ de qiānbǐ ma) is the polite way to ask to borrow something."
            ),
            QuizQuestion(
                direction = QuizDirection.CHINESE_TO_TRANSLATION,
                questionText = "What does this mean?",
                questionChinese = "橡皮",
                questionPinyin = "xiàngpí",
                options = listOf(
                    QuizOption("", "", "pencil"),
                    QuizOption("", "", "ruler"),
                    QuizOption("", "", "eraser"),
                    QuizOption("", "", "book")
                ),
                correctAnswerIndex = 2,
                explanation = "橡皮 (xiàngpí) means eraser."
            ),
            QuizQuestion(
                direction = QuizDirection.TRANSLATION_TO_CHINESE,
                questionText = "How do you say 'You're welcome' in Mandarin?",
                options = listOf(
                    QuizOption("谢谢", "Xièxie", "Thank you"),
                    QuizOption("不用", "Bù yòng", "No need"),
                    QuizOption("不客气", "Bù kèqi", "You're welcome"),
                    QuizOption("对不起", "Duìbuqǐ", "Sorry")
                ),
                correctAnswerIndex = 2,
                explanation = "不客气 (Bù kèqi) means 'You're welcome'."
            )
        )
    )
}
