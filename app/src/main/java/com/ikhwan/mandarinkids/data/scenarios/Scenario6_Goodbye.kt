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

fun getScenario6_Goodbye(): Scenario {
    return Scenario(
        id = "scene6_saying_goodbye",
        title = "放学了",
        description = "End of the school day - saying goodbye",
        characterName = "Wang 老师 & Liu Ming",
        characterEmoji = "👨‍🏫",
        characterRole = "teacher",
        dialogues = listOf(
            DialogueStep(
                id = 1,
                speaker = com.ikhwan.mandarinkids.data.models.Speaker.CHARACTER,
                textChinese = "下课了！别忘了作业。明天见！",
                textPinyin = "Xiàkè le! Bié wàng le zuòyè. Míngtiān jiàn!",
                textEnglish = "Class is over! Don't forget your homework. See you tomorrow!",
                textIndonesian = "Kelas selesai! Jangan lupa PR. Sampai besok!",
                pinyinWords = listOf(
                    PinyinWord("Xiàkè", "下课", "Class over", "Kelas selesai"),
                    PinyinWord("le", "了", "(particle)", "(partikel)"),
                    PinyinWord("Bié", "别", "Don't", "Jangan"),
                    PinyinWord("wàng", "忘", "forget", "lupa"),
                    PinyinWord("le", "了", "(particle)", "(partikel)"),
                    PinyinWord("zuòyè", "作业", "homework", "PR"),
                    PinyinWord("Míngtiān", "明天", "Tomorrow", "Besok"),
                    PinyinWord("jiàn", "见", "see", "jumpa")
                ),
                responseType = com.ikhwan.mandarinkids.data.models.ResponseType.MULTIPLE_OPTIONS,
                options = listOf(
                    ResponseOption(
                        "再见，老师！明天见！",
                        "Zàijiàn, lǎoshī! Míngtiān jiàn!",
                        "Goodbye, teacher! See you tomorrow!",
                        "Sampai jumpa, guru! Sampai besok!",
                        pinyinWords = listOf(
                            PinyinWord("Zàijiàn", "再见", "Goodbye", "Sampai jumpa"),
                            PinyinWord("lǎoshī", "老师", "teacher", "guru"),
                            PinyinWord("Míngtiān", "明天", "Tomorrow", "Besok"),
                            PinyinWord("jiàn", "见", "see", "jumpa")
                        )
                    ),
                    ResponseOption(
                        "谢谢老师！祝你有美好的一天！",
                        "Xièxie lǎoshī! Zhù nǐ yǒu měihǎo de yītiān!",
                        "Thank you, teacher! Have a good day!",
                        "Terima kasih, guru! Semoga harimu menyenangkan!",
                        pinyinWords = listOf(
                            PinyinWord("Xièxie", "谢谢", "Thank you", "Terima kasih"),
                            PinyinWord("lǎoshī", "老师", "teacher", "guru"),
                            PinyinWord("Zhù", "祝", "Wish", "Semoga"),
                            PinyinWord("nǐ", "你", "you", "kamu"),
                            PinyinWord("yǒu", "有", "have", "punya"),
                            PinyinWord("měihǎo", "美好", "wonderful", "menyenangkan"),
                            PinyinWord("de", "的", "(particle)", "(partikel)"),
                            PinyinWord("yītiān", "一天", "day", "hari")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 2,
                speaker = Speaker.CHARACTER,
                textChinese = "你今天表现很好！刘明也在等你。",
                textPinyin = "Nǐ jīntiān biǎoxiàn hěn hǎo! Liú Míng yě zài děng nǐ.",
                textEnglish = "You did great today! Liu Ming is waiting for you too.",
                textIndonesian = "Kamu hebat hari ini! Liu Ming juga menunggumu.",
                pinyinWords = listOf(
                    PinyinWord("Nǐ", "你", "You", "Kamu"),
                    PinyinWord("jīntiān", "今天", "today", "hari ini"),
                    PinyinWord("biǎoxiàn", "表现", "performance", "penampilan"),
                    PinyinWord("hěn", "很", "very", "sangat"),
                    PinyinWord("hǎo", "好", "good", "baik"),
                    PinyinWord("Liú", "刘", "Liu", "Liu"),
                    PinyinWord("Míng", "明", "Ming", "Ming"),
                    PinyinWord("yě", "也", "also", "juga"),
                    PinyinWord("zài", "在", "currently", "sedang"),
                    PinyinWord("děng", "等", "wait", "menunggu"),
                    PinyinWord("nǐ", "你", "you", "kamu")
                ),
                responseType = ResponseType.SINGLE_CHOICE,
                options = listOf(
                    ResponseOption(
                        "明天见，刘明！谢谢你做我的朋友！",
                        "Míngtiān jiàn, Liú Míng! Xièxie nǐ zuò wǒ de péngyou!",
                        "See you tomorrow, Liu Ming! Thanks for being my friend!",
                        "Sampai besok, Liu Ming! Terima kasih sudah jadi temanku!",
                        pinyinWords = listOf(
                            PinyinWord("Míngtiān", "明天", "Tomorrow", "Besok"),
                            PinyinWord("jiàn", "见", "see", "jumpa"),
                            PinyinWord("Liú", "刘", "Liu", "Liu"),
                            PinyinWord("Míng", "明", "Ming", "Ming"),
                            PinyinWord("Xièxie", "谢谢", "Thank you", "Terima kasih"),
                            PinyinWord("nǐ", "你", "you", "kamu"),
                            PinyinWord("zuò", "做", "be", "jadi"),
                            PinyinWord("wǒ", "我", "my", "aku"),
                            PinyinWord("de", "的", "(possessive)", "(kepunyaan)"),
                            PinyinWord("péngyou", "朋友", "friend", "teman")
                        )
                    )
                )
            )
        ),
        quizQuestions = listOf(
            QuizQuestion(
                direction = com.ikhwan.mandarinkids.data.models.QuizDirection.TRANSLATION_TO_CHINESE,
                questionText = "How do you say 'Goodbye' in Mandarin?",
                options = listOf(
                    QuizOption("你好", "Nǐ hǎo", "Hello"),
                    QuizOption("再见", "Zàijiàn", "Goodbye"),
                    QuizOption("谢谢", "Xièxie", "Thank you"),
                    QuizOption("早上好", "Zǎoshang hǎo", "Good morning")
                ),
                correctAnswerIndex = 1,
                explanation = "再见 (Zàijiàn) means 'Goodbye'"
            ),
            QuizQuestion(
                direction = com.ikhwan.mandarinkids.data.models.QuizDirection.CHINESE_TO_TRANSLATION,
                questionText = "What does this mean?",
                questionChinese = "明天见",
                questionPinyin = "Míngtiān jiàn",
                options = listOf(
                    QuizOption("", "", "Good morning"),
                    QuizOption("", "", "See you tomorrow"),
                    QuizOption("", "", "Thank you"),
                    QuizOption("", "", "You're welcome")
                ),
                correctAnswerIndex = 1,
                explanation = "明天见 (Míngtiān jiàn) means 'See you tomorrow'"
            ),
            QuizQuestion(
                direction = QuizDirection.TRANSLATION_TO_CHINESE,
                questionText = "How do you say 'homework' in Mandarin?",
                options = listOf(
                    QuizOption("教室", "Jiàoshì", "classroom"),
                    QuizOption("作业", "Zuòyè", "homework"),
                    QuizOption("老师", "Lǎoshī", "teacher"),
                    QuizOption("同学", "Tóngxué", "classmate")
                ),
                correctAnswerIndex = 1,
                explanation = "作业 (Zuòyè) means 'homework'"
            )
        )
    )
}