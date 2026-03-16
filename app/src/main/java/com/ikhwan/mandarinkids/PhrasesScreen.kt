package com.ikhwan.mandarinkids

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikhwan.mandarinkids.tts.TtsManager
import com.ikhwan.mandarinkids.tts.rememberTtsManager

data class Phrase(
    val chinese: String,
    val pinyin: String,
    val english: String,
    val indonesian: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhrasesScreen(category: String, onBack: () -> Unit) {
    val tts = rememberTtsManager()
    val phrases = getPhrasesByCategory(category)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(phrases) { phrase ->
                PhraseCard(phrase = phrase, tts = tts)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun PhraseCard(phrase: Phrase, tts: TtsManager) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = phrase.chinese, fontSize = 24.sp)
                Text(text = phrase.pinyin, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "🇬🇧 ${phrase.english}", fontSize = 14.sp)
                Text(text = "🇮🇩 ${phrase.indonesian}", fontSize = 14.sp)
            }

            IconButton(onClick = {
                tts.speak(phrase.chinese)
            }) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

fun getPhrasesByCategory(category: String): List<Phrase> {
    return when (category) {
        "👋 Say Hello" -> listOf(
            Phrase("你好", "Nǐ hǎo", "Hello", "Halo"),
            Phrase("我叫小明", "Wǒ jiào Xiǎo Míng", "My name is Xiao Ming", "Nama saya Xiao Ming"),
            Phrase("很高兴认识你", "Hěn gāoxìng rènshi nǐ", "Nice to meet you", "Senang bertemu denganmu"),
            Phrase("你好吗？", "Nǐ hǎo ma?", "How are you?", "Apa kabar?"),
            Phrase("我很好", "Wǒ hěn hǎo", "I'm good", "Saya baik"),
            Phrase("谢谢", "Xièxie", "Thank you", "Terima kasih"),
            Phrase("不客气", "Bú kèqi", "You're welcome", "Sama-sama"),
            Phrase("再见", "Zàijiàn", "Goodbye", "Sampai jumpa"),
            Phrase("我五岁", "Wǒ wǔ suì", "I'm 5 years old", "Saya 5 tahun"),
            Phrase("对不起", "Duìbùqǐ", "Sorry", "Maaf")
        )
        "😊 My Feelings" -> listOf(
            Phrase("我很开心", "Wǒ hěn kāixīn", "I'm happy", "Saya senang"),
            Phrase("我很难过", "Wǒ hěn nánguò", "I'm sad", "Saya sedih"),
            Phrase("我饿了", "Wǒ è le", "I'm hungry", "Saya lapar"),
            Phrase("我渴了", "Wǒ kě le", "I'm thirsty", "Saya haus"),
            Phrase("我累了", "Wǒ lèi le", "I'm tired", "Saya lelah"),
            Phrase("我很生气", "Wǒ hěn shēngqì", "I'm angry", "Saya marah"),
            Phrase("我害怕", "Wǒ hàipà", "I'm scared", "Saya takut"),
            Phrase("帮帮我", "Bāng bāng wǒ", "Help me please", "Tolong bantu saya"),
            Phrase("我想回家", "Wǒ xiǎng huí jiā", "I want to go home", "Saya ingin pulang"),
            Phrase("我很兴奋", "Wǒ hěn xīngfèn", "I'm excited", "Saya bersemangat")
        )
        "🏫 At School" -> listOf(
            Phrase("老师好", "Lǎoshī hǎo", "Hello teacher", "Halo guru"),
            Phrase("我不懂", "Wǒ bù dǒng", "I don't understand", "Saya tidak mengerti"),
            Phrase("可以再说一次吗？", "Kěyǐ zài shuō yīcì ma?", "Can you say it again?", "Bisa ulangi?"),
            Phrase("我要上厕所", "Wǒ yào shàng cèsuǒ", "I need the bathroom", "Saya mau ke toilet"),
            Phrase("我做完了", "Wǒ zuò wán le", "I'm finished", "Saya sudah selesai"),
            Phrase("请帮我", "Qǐng bāng wǒ", "Please help me", "Tolong bantu saya"),
            Phrase("我会", "Wǒ huì", "I can do it", "Saya bisa"),
            Phrase("我不会", "Wǒ bú huì", "I can't do it", "Saya tidak bisa"),
            Phrase("这是什么？", "Zhè shì shénme?", "What is this?", "Ini apa?"),
            Phrase("我明白了", "Wǒ míngbái le", "I understand", "Saya mengerti")
        )
        "🎮 Let's Play" -> listOf(
            Phrase("我们一起玩", "Wǒmen yīqǐ wán", "Let's play together", "Ayo main bersama"),
            Phrase("这是我的", "Zhè shì wǒ de", "This is mine", "Ini punya saya"),
            Phrase("轮到你了", "Lún dào nǐ le", "It's your turn", "Giliranmu"),
            Phrase("可以吗？", "Kěyǐ ma?", "Can I?", "Boleh?"),
            Phrase("好的", "Hǎo de", "Okay", "Baik"),
            Phrase("不要", "Bú yào", "I don't want", "Tidak mau"),
            Phrase("给我", "Gěi wǒ", "Give me", "Kasih saya"),
            Phrase("我赢了", "Wǒ yíng le", "I won", "Saya menang"),
            Phrase("再来一次", "Zài lái yīcì", "One more time", "Sekali lagi"),
            Phrase("真好玩", "Zhēn hǎowán", "So fun", "Seru sekali")
        )
        else -> emptyList()
    }
}
