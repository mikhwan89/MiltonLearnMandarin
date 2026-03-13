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

fun getScenario5_Playground(): Scenario {
    return Scenario(
        id = "scene5_playground",
        title = "操场游戏",
        description = "Joining classmates playing on the playground",
        characterName = "同学们",
        characterEmoji = "👥",
        characterRole = "friend",
        dialogues = listOf(
            DialogueStep(
                id = 1,
                speaker = com.ikhwan.mandarinkids.data.models.Speaker.CHARACTER,
                textChinese = "我们在玩躲避球！",
                textPinyin = "Wǒmen zài wán duǒbìqiú!",
                textEnglish = "We're playing dodgeball!",
                textIndonesian = "Kami sedang main bola sodok!",
                pinyinWords = listOf(
                    PinyinWord("Wǒmen", "我们", "We", "Kami"),
                    PinyinWord("zài", "在", "currently", "sedang"),
                    PinyinWord("wán", "玩", "play", "main"),
                    PinyinWord("duǒbìqiú", "躲避球", "dodgeball", "bola sodok")
                ),
                responseType = com.ikhwan.mandarinkids.data.models.ResponseType.MULTIPLE_OPTIONS,
                options = listOf(
                    ResponseOption(
                        "我可以一起玩吗？",
                        "Wǒ kěyǐ yīqǐ wán ma?",
                        "Can I play with you?",
                        "Boleh saya ikut main?",
                        pinyinWords = listOf(
                            PinyinWord("Wǒ", "我", "I", "Saya"),
                            PinyinWord("kěyǐ", "可以", "can", "boleh"),
                            PinyinWord("yīqǐ", "一起", "together", "bersama"),
                            PinyinWord("wán", "玩", "play", "main"),
                            PinyinWord("ma", "吗", "?", "?")
                        )
                    ),
                    ResponseOption(
                        "看起来很好玩！",
                        "Kàn qǐlai hěn hǎowán!",
                        "That looks fun!",
                        "Kelihatan seru!",
                        pinyinWords = listOf(
                            PinyinWord("Kàn", "看", "Look", "Lihat"),
                            PinyinWord("qǐlai", "起来", "seems", "kelihatan"),
                            PinyinWord("hěn", "很", "very", "sangat"),
                            PinyinWord("hǎowán", "好玩", "fun", "seru")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 2,
                speaker = com.ikhwan.mandarinkids.data.models.Speaker.CHARACTER,
                textChinese = "当然可以！你会玩吗？",
                textPinyin = "Dāngrán kěyǐ! Nǐ huì wán ma?",
                textEnglish = "Of course! Do you know how to play?",
                textIndonesian = "Tentu saja! Kamu bisa main?",
                pinyinWords = listOf(
                    PinyinWord("Dāngrán", "当然", "Of course", "Tentu saja"),
                    PinyinWord("kěyǐ", "可以", "can", "boleh"),
                    PinyinWord("Nǐ", "你", "You", "Kamu"),
                    PinyinWord("huì", "会", "know how", "bisa"),
                    PinyinWord("wán", "玩", "play", "main"),
                    PinyinWord("ma", "吗", "?", "?")
                ),
                responseType = com.ikhwan.mandarinkids.data.models.ResponseType.MULTIPLE_OPTIONS,
                options = listOf(
                    ResponseOption(
                        "会！我喜欢玩！",
                        "Huì! Wǒ xǐhuan wán!",
                        "Yes! I love playing!",
                        "Bisa! Saya suka main!",
                        pinyinWords = listOf(
                            PinyinWord("Huì", "会", "Know how", "Bisa"),
                            PinyinWord("Wǒ", "我", "I", "Saya"),
                            PinyinWord("xǐhuan", "喜欢", "like", "suka"),
                            PinyinWord("wán", "玩", "play", "main")
                        )
                    ),
                    ResponseOption(
                        "不太会。你能教我吗？",
                        "Bú tài huì. Nǐ néng jiāo wǒ ma?",
                        "Not really. Can you teach me?",
                        "Tidak terlalu bisa. Bisa ajari saya?",
                        pinyinWords = listOf(
                            PinyinWord("Bú", "不", "Not", "Tidak"),
                            PinyinWord("tài", "太", "too", "terlalu"),
                            PinyinWord("huì", "会", "know how", "bisa"),
                            PinyinWord("Nǐ", "你", "You", "Kamu"),
                            PinyinWord("néng", "能", "can", "bisa"),
                            PinyinWord("jiāo", "教", "teach", "ajari"),
                            PinyinWord("wǒ", "我", "me", "saya"),
                            PinyinWord("ma", "吗", "?", "?")
                        )
                    )
                )
            ),
            DialogueStep(
                id = 3,
                speaker = Speaker.CHARACTER,
                textChinese = "太好了！我们开始吧！",
                textPinyin = "Tài hǎo le! Wǒmen kāishǐ ba!",
                textEnglish = "Great! Let's start!",
                textIndonesian = "Bagus! Ayo mulai!",
                pinyinWords = listOf(
                    PinyinWord("Tài", "太", "Too/Very", "Sangat"),
                    PinyinWord("hǎo", "好", "good", "bagus"),
                    PinyinWord("le", "了", "(particle)", "(partikel)"),
                    PinyinWord("Wǒmen", "我们", "We", "Kita"),
                    PinyinWord("kāishǐ", "开始", "start", "mulai"),
                    PinyinWord("ba", "吧", "(suggestion)", "(ajakan)")
                ),
                responseType = ResponseType.LISTEN_ONLY
            )
        ),
        quizQuestions = listOf(
            QuizQuestion(
                direction = com.ikhwan.mandarinkids.data.models.QuizDirection.CHINESE_TO_TRANSLATION,
                questionText = "What does this mean?",
                questionChinese = "我可以一起玩吗？",
                questionPinyin = "Wǒ kěyǐ yīqǐ wán ma?",
                options = listOf(
                    QuizOption("", "", "I don't want to play"),
                    QuizOption("", "", "Can I play with you?"),
                    QuizOption("", "", "I'm tired"),
                    QuizOption("", "", "What are you playing?")
                ),
                correctAnswerIndex = 1,
                explanation = "我可以一起玩吗？(Wǒ kěyǐ yīqǐ wán ma?) means 'Can I play with you?'"
            ),
            QuizQuestion(
                direction = com.ikhwan.mandarinkids.data.models.QuizDirection.TRANSLATION_TO_CHINESE,
                questionText = "How do you say 'Can you teach me?' in Mandarin?",
                options = listOf(
                    QuizOption("你好吗", "Nǐ hǎo ma", "How are you"),
                    QuizOption("你能教我吗", "Nǐ néng jiāo wǒ ma", "Can you teach me"),
                    QuizOption("你叫什么", "Nǐ jiào shénme", "What's your name"),
                    QuizOption("你喜欢吗", "Nǐ xǐhuan ma", "Do you like it")
                ),
                correctAnswerIndex = 1,
                explanation = "你能教我吗？(Nǐ néng jiāo wǒ ma?) means 'Can you teach me?'"
            ),
            QuizQuestion(
                direction = QuizDirection.CHINESE_TO_TRANSLATION,
                questionText = "What does this mean?",
                questionChinese = "当然可以",
                questionPinyin = "Dāngrán kěyǐ",
                options = listOf(
                    QuizOption("", "", "No, you can't"),
                    QuizOption("", "", "Of course you can"),
                    QuizOption("", "", "I don't know"),
                    QuizOption("", "", "Maybe later")
                ),
                correctAnswerIndex = 1,
                explanation = "当然可以 (Dāngrán kěyǐ) means 'Of course you can'"
            )
        )
    )
}