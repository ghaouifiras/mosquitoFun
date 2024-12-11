package com.firas.animationtest

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.firas.animationtest.ui.theme.AnimationTestTheme
import kotlinx.coroutines.launch
import kotlin.math.atan2

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnimationTestTheme {
                GreenBallAnimation(this)

            }
        }
    }
}

@Composable
fun GreenBallAnimation(context: Context) {

    var mosquitoPosition by remember { mutableStateOf(Offset(100f, 100f)) }
    val pathPoints = remember { mutableStateListOf(mosquitoPosition) }
    val mosquitoAnimatable = remember { Animatable(mosquitoPosition, Offset.VectorConverter) }
    var mosquitoRotation by remember { mutableStateOf(0.0) } // Angle of rotation


    val coroutineScope = rememberCoroutineScope()

    // Initialize SoundPool
    val soundPool = remember {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    val soundIds = remember {
        listOf(
            soundPool.load(context, R.raw.rapmechmteek, 1),
            soundPool.load(context, R.raw.ijamennadhoukelbenna, 1),
             soundPool.load(context, R.raw.matrawahkenfarhan, 1),
             soundPool.load(context, R.raw.yachaabhhhhhhh, 1),
             soundPool.load(context, R.raw.betbiafhemt, 1)
        )
    }

    var currentSoundIndex by remember { mutableStateOf(0) } // Track which sound to play

    DisposableEffect(Unit) {
        onDispose {
            soundPool.release()
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White)
                .pointerInput(Unit) {
                    detectTapGestures { tapPosition ->
                        pathPoints.add(tapPosition)
                        val dx = tapPosition.x - mosquitoPosition.x
                        val dy = tapPosition.y - mosquitoPosition.y
                        mosquitoRotation = Math.toDegrees(atan2(dy, dx).toDouble()) + 90f


                        coroutineScope.launch {
                            //    streamId = soundPool.play(soundId, 1f, 1f, 1, 0, 1f) // -1 loops the sound
                            // Play the current sound
                            val soundId = soundIds[currentSoundIndex]
                            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)

                            currentSoundIndex = (currentSoundIndex + 1) % soundIds.size


                            mosquitoAnimatable.animateTo(
                                targetValue = tapPosition,
                                animationSpec = tween(durationMillis = 1000)
                            )
                            //  soundPool.stop(streamId)
                            mosquitoPosition = tapPosition
                        }
                    }
                }
        ) {
            MosquitoDrawing(pathPoints, mosquitoAnimatable, mosquitoRotation)
        }
        Text(
            text = "Tap anywhere to move the mosquito!\n made by firas with ♡",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
fun MosquitoDrawing(
    pathPoints: SnapshotStateList<Offset>,
    mosquitoAnimatable: Animatable<Offset, AnimationVector2D>,
    mosquitoRotation: Double
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
       /* for (i in 0 until pathPoints.size - 1) {
            drawLine(
                color = Color.Gray,
                start = pathPoints[i],
                end = pathPoints[i + 1],
                strokeWidth = 5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 20f), phase = 0f)
            )
        }*/
        val centerX = mosquitoAnimatable.value.x
        val centerY = mosquitoAnimatable.value.y

        rotate(degrees = mosquitoRotation.toFloat(), pivot = Offset(centerX, centerY)) {
            // Draw Body
            drawOval(
                color = Color.DarkGray,
                topLeft = Offset(centerX - 20, centerY - 40),
                size = androidx.compose.ui.geometry.Size(40f, 80f)
            )

            // Draw Head
            drawCircle(
                color = Color.Black,
                center = Offset(centerX, centerY - 50),
                radius = 15f
            )

            // Draw Wings
            drawOval(
                color = Color.LightGray.copy(alpha = 0.5f),
                topLeft = Offset(centerX - 60, centerY - 80),
                size = androidx.compose.ui.geometry.Size(50f, 100f),
                style = Stroke(width = 2f)
            )
            drawOval(
                color = Color.LightGray.copy(alpha = 0.5f),
                topLeft = Offset(centerX + 10, centerY - 80),
                size = androidx.compose.ui.geometry.Size(50f, 100f),
                style = Stroke(width = 2f)
            )

            // Draw Legs
            val legOffsets = listOf(
                Offset(centerX - 20, centerY - 10) to Offset(centerX - 60, centerY + 30),
                Offset(centerX - 20, centerY + 10) to Offset(centerX - 60, centerY + 60),
                Offset(centerX - 20, centerY + 30) to Offset(centerX - 60, centerY + 90),
                Offset(centerX + 20, centerY - 10) to Offset(centerX + 60, centerY + 30),
                Offset(centerX + 20, centerY + 10) to Offset(centerX + 60, centerY + 60),
                Offset(centerX + 20, centerY + 30) to Offset(centerX + 60, centerY + 90)
            )
            for ((start, end) in legOffsets) {
                drawLine(
                    color = Color.Black,
                    start = start,
                    end = end,
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                )
            }

            // Draw Antennae
            drawLine(
                color = Color.Black,
                start = Offset(centerX - 10, centerY - 65),
                end = Offset(centerX - 30, centerY - 100),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.Black,
                start = Offset(centerX + 10, centerY - 65),
                end = Offset(centerX + 30, centerY - 100),
                strokeWidth = 2f
            )

            // Draw Proboscis
            drawLine(
                color = Color.Black,
                start = Offset(centerX, centerY - 50),
                end = Offset(centerX, centerY - 80),
                strokeWidth = 4f
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AnimationTestTheme {
    }
}