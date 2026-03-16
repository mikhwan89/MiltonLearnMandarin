package com.ikhwan.mandarinkids.data.scenarios

import com.ikhwan.mandarinkids.data.models.*

fun getScenario12_RaisingHand(): Scenario {
    return Scenario(
        id = "scene12_raising_hand",
        title = "举手提问",
        description = "Raising your hand in class — asking and answering the teacher's questions",
        characterName = "Chen 老师",
        characterEmoji = "👩‍🏫",
        characterRole = "teacher",
        dialogues = listOf(
            DialogueStep(
                id = 1,
                speaker = Speaker.CHARACTER,
                textChinese = "同学们，谁知道答案？请举手！",
                textPinyin = "Tóngxuémen, shéi zhīdào dá'àn? Qǐng jǔshǒu!",
                textEnglish = "Students, who knows the answer? Please raise your hand!",
                textIndonesian = "Anak-anak, siapa yang tahu jawabannya? Tolong angkat tangan!",
                pinyinWords = listOf(
                    PinyinWord("tóngxuémen", "同学们", "students (plural)", "anak-anak / para siswa"),
                    PinyinWord("shéi", "谁", "who", "siapa"),
                    PinyinWord("dá'àn", "答案", "answer", "jawaban"),
                    PinyinWord("jǔshǒu", "举手", "raise hand", "angkat tangan")
                ),
                responseType = ResponseType.SINGLE_CHOICE,
                options = listOf(
                    ResponseOption(
                        "（举手）老师，我知道！",
                        "(Jǔshǒu) Lǎoshī, wǒ zhīdào!",
                        "(Raises hand) Teacher, I know!",
                        "(Angkat tangan) Guru, saya tahu!",
                        pinyinWords = listOf(
                            PinyinWord("zhīdào", "知道", "know", "tahu")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 2,
                speaker = Speaker.CHARACTER,
                textChinese = "很好！请你回答。",
                textPinyin = "Hěn hǎo! Qǐng nǐ huídá.",
                textEnglish = "Very good! Please answer.",
                textIndonesian = "Bagus! Tolong jawab.",
                pinyinWords = listOf(
                    PinyinWord("qǐng", "请", "please", "tolong"),
                    PinyinWord("huídá", "回答", "answer / respond", "jawab")
                ),
                responseType = ResponseType.MULTIPLE_OPTIONS,
                options = listOf(
                    ResponseOption(
                        "答案是三！",
                        "Dá'àn shì sān!",
                        "The answer is three!",
                        "Jawabannya adalah tiga!",
                        pinyinWords = listOf(
                            PinyinWord("dá'àn", "答案", "answer", "jawaban"),
                            PinyinWord("sān", "三", "three", "tiga")
                        )
                    ),
                    ResponseOption(
                        "老师，我不知道。",
                        "Lǎoshī, wǒ bù zhīdào.",
                        "Teacher, I don't know.",
                        "Guru, saya tidak tahu.",
                        pinyinWords = listOf(
                            PinyinWord("bù zhīdào", "不知道", "don't know", "tidak tahu")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 3,
                speaker = Speaker.CHARACTER,
                textChinese = "答对了！你真厉害！有问题要问吗？",
                textPinyin = "Dá duì le! Nǐ zhēn lìhài! Yǒu wèntí yào wèn ma?",
                textEnglish = "Correct! You're amazing! Do you have any questions?",
                textIndonesian = "Benar! Kamu hebat sekali! Ada pertanyaan?",
                pinyinWords = listOf(
                    PinyinWord("dá duì le", "答对了", "answered correctly", "jawaban benar"),
                    PinyinWord("lìhài", "厉害", "amazing / impressive", "hebat"),
                    PinyinWord("wèntí", "问题", "question / problem", "pertanyaan"),
                    PinyinWord("wèn", "问", "ask", "tanya")
                ),
                responseType = ResponseType.MULTIPLE_OPTIONS,
                options = listOf(
                    ResponseOption(
                        "老师，我有一个问题。",
                        "Lǎoshī, wǒ yǒu yī gè wèntí.",
                        "Teacher, I have a question.",
                        "Guru, saya punya satu pertanyaan.",
                        pinyinWords = listOf(
                            PinyinWord("yǒu", "有", "have", "punya"),
                            PinyinWord("yī gè", "一个", "one", "satu"),
                            PinyinWord("wèntí", "问题", "question", "pertanyaan")
                        )
                    ),
                    ResponseOption(
                        "没有问题，谢谢老师！",
                        "Méiyǒu wèntí, xièxie lǎoshī!",
                        "No questions, thank you teacher!",
                        "Tidak ada pertanyaan, terima kasih guru!",
                        pinyinWords = listOf(
                            PinyinWord("méiyǒu", "没有", "don't have / no", "tidak ada"),
                            PinyinWord("wèntí", "问题", "question", "pertanyaan")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 4,
                speaker = Speaker.CHARACTER,
                textChinese = "好，下课了！明天见！",
                textPinyin = "Hǎo, xià kè le! Míngtiān jiàn!",
                textEnglish = "Alright, class is over! See you tomorrow!",
                textIndonesian = "Baik, kelas selesai! Sampai besok!",
                pinyinWords = listOf(
                    PinyinWord("xià kè", "下课", "class is over", "kelas selesai"),
                    PinyinWord("míngtiān jiàn", "明天见", "see you tomorrow", "sampai besok")
                ),
                responseType = ResponseType.SINGLE_CHOICE,
                options = listOf(
                    ResponseOption(
                        "明天见，老师！",
                        "Míngtiān jiàn, lǎoshī!",
                        "See you tomorrow, teacher!",
                        "Sampai besok, guru!",
                        pinyinWords = listOf(
                            PinyinWord("míngtiān jiàn", "明天见", "see you tomorrow", "sampai besok")
                        )
                    )
                )
            )
        ),
        quizQuestions = listOf(
            QuizQuestion(
                direction = QuizDirection.TRANSLATION_TO_CHINESE,
                questionText = "How do you say 'raise your hand' in Mandarin?",
                options = listOf(
                    QuizOption("举手", "jǔshǒu", "raise hand"),
                    QuizOption("回答", "huídá", "answer"),
                    QuizOption("提问", "tíwèn", "ask a question"),
                    QuizOption("站起来", "zhàn qǐlái", "stand up")
                ),
                correctAnswerIndex = 0,
                explanation = "举手 (jǔshǒu) means 'raise your hand'. 举 = lift, 手 = hand."
            ),
            QuizQuestion(
                direction = QuizDirection.CHINESE_TO_TRANSLATION,
                questionText = "What does this mean?",
                questionChinese = "答对了",
                questionPinyin = "dá duì le",
                options = listOf(
                    QuizOption("", "", "wrong answer"),
                    QuizOption("", "", "correct answer"),
                    QuizOption("", "", "no answer"),
                    QuizOption("", "", "ask a question")
                ),
                correctAnswerIndex = 1,
                explanation = "答对了 (dá duì le) means 'answered correctly'. 答 = answer, 对 = correct."
            ),
            QuizQuestion(
                direction = QuizDirection.TRANSLATION_TO_CHINESE,
                questionText = "How do you say 'I have a question' in Mandarin?",
                options = listOf(
                    QuizOption("我没有问题。", "Wǒ méiyǒu wèntí.", "I have no questions."),
                    QuizOption("我不知道。", "Wǒ bù zhīdào.", "I don't know."),
                    QuizOption("我有一个问题。", "Wǒ yǒu yī gè wèntí.", "I have a question."),
                    QuizOption("请回答。", "Qǐng huídá.", "Please answer.")
                ),
                correctAnswerIndex = 2,
                explanation = "我有一个问题 (Wǒ yǒu yī gè wèntí) means 'I have a question'."
            )
        )
    )
}
