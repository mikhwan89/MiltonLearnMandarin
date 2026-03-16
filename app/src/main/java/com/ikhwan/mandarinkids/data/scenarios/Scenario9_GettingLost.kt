package com.ikhwan.mandarinkids.data.scenarios

import com.ikhwan.mandarinkids.data.models.*

fun getScenario9_GettingLost(): Scenario {
    return Scenario(
        id = "scene9_getting_lost",
        title = "迷路了",
        description = "Getting lost at school — asking a teacher for directions to class or toilet",
        characterName = "陌生老师",
        characterEmoji = "👨‍🏫",
        characterRole = "teacher",
        dialogues = listOf(
            DialogueStep(
                id = 1,
                speaker = Speaker.STUDENT,
                textChinese = "老师，对不起，我找不到我的教室。",
                textPinyin = "Lǎoshī, duìbuqǐ, wǒ zhǎo bù dào wǒ de jiàoshì.",
                textEnglish = "Teacher, sorry, I can't find my classroom.",
                textIndonesian = "Guru, maaf, saya tidak bisa menemukan kelas saya.",
                pinyinWords = listOf(
                    PinyinWord("duìbuqǐ", "对不起", "sorry", "maaf"),
                    PinyinWord("zhǎo bù dào", "找不到", "can't find", "tidak bisa menemukan"),
                    PinyinWord("jiàoshì", "教室", "classroom", "ruang kelas")
                ),
                responseType = ResponseType.LISTEN_ONLY
            ),
            DialogueStep(
                id = 2,
                speaker = Speaker.CHARACTER,
                textChinese = "没关系！你是几年级的同学？",
                textPinyin = "Méi guānxi! Nǐ shì jǐ niánjí de tóngxué?",
                textEnglish = "No problem! What grade are you in?",
                textIndonesian = "Tidak apa-apa! Kamu kelas berapa?",
                pinyinWords = listOf(
                    PinyinWord("méi guānxi", "没关系", "no problem", "tidak apa-apa"),
                    PinyinWord("jǐ", "几", "what / how many", "berapa"),
                    PinyinWord("niánjí", "年级", "grade / year", "kelas")
                ),
                responseType = ResponseType.SINGLE_CHOICE,
                options = listOf(
                    ResponseOption(
                        "我是一年级的。我的教室在哪里？",
                        "Wǒ shì yī niánjí de. Wǒ de jiàoshì zài nǎlǐ?",
                        "I'm in grade one. Where is my classroom?",
                        "Saya kelas satu. Di mana kelas saya?",
                        pinyinWords = listOf(
                            PinyinWord("yī niánjí", "一年级", "grade one", "kelas satu"),
                            PinyinWord("zài nǎlǐ", "在哪里", "where is", "di mana")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 3,
                speaker = Speaker.CHARACTER,
                textChinese = "一年级的教室在二楼，左转就到了。",
                textPinyin = "Yī niánjí de jiàoshì zài èr lóu, zuǒ zhuǎn jiù dào le.",
                textEnglish = "Grade one classrooms are on the second floor, turn left and you're there.",
                textIndonesian = "Kelas satu ada di lantai dua, belok kiri langsung sampai.",
                pinyinWords = listOf(
                    PinyinWord("èr lóu", "二楼", "second floor", "lantai dua"),
                    PinyinWord("zuǒ", "左", "left", "kiri"),
                    PinyinWord("zhuǎn", "转", "turn", "belok"),
                    PinyinWord("jiù dào le", "就到了", "then you're there", "langsung sampai")
                ),
                responseType = ResponseType.MULTIPLE_OPTIONS,
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
                    ),
                    ResponseOption(
                        "老师，厕所在哪里？",
                        "Lǎoshī, cèsuǒ zài nǎlǐ?",
                        "Teacher, where is the toilet?",
                        "Guru, di mana toilet?",
                        pinyinWords = listOf(
                            PinyinWord("cèsuǒ", "厕所", "toilet", "toilet"),
                            PinyinWord("zài nǎlǐ", "在哪里", "where is", "di mana")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 4,
                speaker = Speaker.CHARACTER,
                textChinese = "厕所就在楼梯旁边。加油！",
                textPinyin = "Cèsuǒ jiù zài lóutī pángbiān. Jiāyóu!",
                textEnglish = "The toilet is right next to the stairs. You can do it!",
                textIndonesian = "Toilet ada di sebelah tangga. Semangat!",
                pinyinWords = listOf(
                    PinyinWord("lóutī", "楼梯", "stairs", "tangga"),
                    PinyinWord("pángbiān", "旁边", "next to / beside", "di sebelah"),
                    PinyinWord("jiāyóu", "加油", "you can do it!", "semangat!")
                ),
                responseType = ResponseType.SINGLE_CHOICE,
                options = listOf(
                    ResponseOption(
                        "谢谢老师，再见！",
                        "Xièxie lǎoshī, zàijiàn!",
                        "Thank you teacher, goodbye!",
                        "Terima kasih guru, selamat tinggal!",
                        pinyinWords = listOf(
                            PinyinWord("Xièxie", "谢谢", "Thank you", "Terima kasih"),
                            PinyinWord("zàijiàn", "再见", "goodbye", "selamat tinggal")
                        )
                    )
                )
            )
        ),
        quizQuestions = listOf(
            QuizQuestion(
                direction = QuizDirection.TRANSLATION_TO_CHINESE,
                questionText = "How do you say 'Where is the toilet?' in Mandarin?",
                options = listOf(
                    QuizOption("厕所在哪里？", "Cèsuǒ zài nǎlǐ?", "Where is the toilet?"),
                    QuizOption("教室在哪里？", "Jiàoshì zài nǎlǐ?", "Where is the classroom?"),
                    QuizOption("我找不到了。", "Wǒ zhǎo bù dào le.", "I can't find it."),
                    QuizOption("我迷路了。", "Wǒ mí lù le.", "I'm lost.")
                ),
                correctAnswerIndex = 0,
                explanation = "厕所在哪里 (Cèsuǒ zài nǎlǐ) means 'Where is the toilet?'"
            ),
            QuizQuestion(
                direction = QuizDirection.CHINESE_TO_TRANSLATION,
                questionText = "What does this mean?",
                questionChinese = "左转",
                questionPinyin = "zuǒ zhuǎn",
                options = listOf(
                    QuizOption("", "", "turn right"),
                    QuizOption("", "", "go straight"),
                    QuizOption("", "", "turn left"),
                    QuizOption("", "", "go upstairs")
                ),
                correctAnswerIndex = 2,
                explanation = "左转 (zuǒ zhuǎn) means 'turn left'. 左 = left, 转 = turn."
            ),
            QuizQuestion(
                direction = QuizDirection.TRANSLATION_TO_CHINESE,
                questionText = "How do you say 'second floor' in Mandarin?",
                options = listOf(
                    QuizOption("一楼", "yī lóu", "first floor"),
                    QuizOption("三楼", "sān lóu", "third floor"),
                    QuizOption("二楼", "èr lóu", "second floor"),
                    QuizOption("楼梯", "lóutī", "stairs")
                ),
                correctAnswerIndex = 2,
                explanation = "二楼 (èr lóu) means second floor. 二 = two, 楼 = floor/building."
            )
        )
    )
}
