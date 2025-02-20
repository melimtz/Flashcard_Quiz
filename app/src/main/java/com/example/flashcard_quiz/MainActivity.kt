package com.example.flashcard_quiz

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcard_quiz.ui.theme.Flashcard_QuizTheme
import kotlinx.coroutines.delay
import org.xmlpull.v1.XmlPullParser

//Create a flashcard quiz app where questions and answers are loaded from an XML
//file. Load questions and answers from an XML file (res/xml/flashcards.xml). Use
//LazyRow to display flashcards that can be swiped horizontally. Clicking a flashcard
//should flip it to reveal the answer. Use coroutines to shuWle flashcards every 15
//seconds. The flascards.xml file should be in this format:
//<flashcards>
//<card>
//<question>What is the capital of France?</question>
//<answer>Paris</answer>
//</card>
//<card>
//<question>Who wrote '1984'?</question>
//<answer>George Orwell</answer>
//</card>
//</flashcards>


//class holds q & a
data class Flashcard(
    val question: String,
    val answer: String
)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashcardScreen(context = this)
        }
    }
}

@Composable
fun FlashcardScreen(context: Context) {

    //call parser to get all flashcards
    var allFlashcards = remember { getFlashcards(context) }

    //while running, shuffle flashcards every 15 secs
    LaunchedEffect(Unit) {
        while(true) {
            allFlashcards.shuffled()
            delay(15000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.linearGradient(
                colors = listOf(Color(0xFFe6f0e6), Color(0xFFb3d1b3)))
            ),
        contentAlignment = Alignment.Center
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            //call flashcard setup for all items in LazyRow
            items(allFlashcards) { flashcard ->
                FlashcardSetup(flashcard)
            }
        }
    }
}

fun getFlashcards(context: Context): List<Flashcard>{
    //initialize flashcard list of class Flashcard, parser for xml file, and class elements
    val flashcards = mutableListOf<Flashcard>()
    val parser = context.resources.getXml(R.xml.flashcards)

    var eventType = parser.next()
    var question: String? = null
    var answer: String? = null

    //until the file ends, assign questions and answer for each card tag
    while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG) {
            if (parser.name == "question") {
                question = parser.nextText()
            } else if (parser.name == "answer"){
                answer = parser.nextText()
            }
        // append q & a to flashcard list
        } else if (eventType == XmlPullParser.END_TAG) {
            if (parser.name == "card" && question != null && answer != null) {
                flashcards.add(Flashcard(question, answer))
                question = null
                answer = null
            }
        }
        //move on to next card tag
        eventType = parser.next()
    }
    return flashcards
}

@Composable
fun FlashcardSetup(flashcard: Flashcard) {
    //initialize var for card side and flipping animation
    var flipped by remember { mutableStateOf(false) }
    val rotationY by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    //card visuals
    Card(
        modifier = Modifier
            .size(350.dp, 300.dp)
            .clickable {flipped = !flipped}
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 8 * density
            },
        elevation = CardDefaults.cardElevation(12.dp),
        border = BorderStroke(2.dp, Color.LightGray),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFfff0f0))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (rotationY < 90f){
                QText(flashcard.question)
            }else{
                //make sure flipped card does not read flipped
                Box(
                    modifier = Modifier.graphicsLayer {
                        scaleX = -1f
                    }
                ) {
                    AText(flashcard.answer)
                }
            }
        }
    }
}

//text for answers
@Composable
fun AText(text: String){
    Text(
        text = text,
        fontSize = 24.sp, 
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

//text for questions
@Composable
fun QText(text: String) {
    Text(
        text = text,
        fontSize = 24.sp,
        fontStyle = FontStyle.Italic,
        textAlign = TextAlign.Center
    )
}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    FlashcardScreen()
//}