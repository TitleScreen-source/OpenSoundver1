package com.opensound.app.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.opensound.app.R
import com.opensound.app.models.Track
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun ReelsShowcaseMiniPlayer(
    track: Track,
    isPlaying: Boolean,
    playbackSeconds: Float,
    onPlayPauseClick: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sceneTime = playbackSeconds.coerceAtLeast(0f)
    val currentSubtitle = showcaseSubtitles.lastOrNull { cue ->
        sceneTime in cue.startSeconds..cue.endSeconds
    }
    val subtitleProgress = currentSubtitle?.let { cueProgress(sceneTime, it) } ?: 1f
    val revealWarp = revealWarp(subtitleProgress)
    val peak = pulseWindow(sceneTime, start = 11.8f, end = 24.0f)
    val bloodProgress = sustainedProgress(sceneTime, start = 13.2f, full = 27.0f)
    val bloodEnergy = maxOf(peak * 0.55f, bloodProgress * 0.72f)
    val finalFade = pulseWindow(sceneTime, start = 31.5f, end = 35.8f)
    val collapse = sustainedProgress(sceneTime, start = 29.5f, full = 35.8f)
    val madness = maxOf(
        peak,
        pulseWindow(sceneTime, start = 21.6f, end = 34.6f),
        collapse * 0.9f
    ).coerceIn(0f, 1f)
    val heartbeat = heartbeatPulse(
        time = sceneTime,
        rate = 1.45f + madness * 0.72f + collapse * 0.35f
    ) * (0.2f + madness * 0.56f + collapse * 0.18f)
    val pressure = pulseWindow(sceneTime, start = 21.2f, end = 34.8f)
    val afterimage = maxOf(heartbeat * 0.78f, peak * 0.48f, pressure * 0.22f)
    val cinematicShakeX =
        sin((sceneTime * 1.15f).toDouble()).toFloat() * 1.25f +
            sin((sceneTime * 2.05f + 0.8f).toDouble()).toFloat() * 0.85f
    val cinematicShakeY =
        sin((sceneTime * 1.42f + 1.3f).toDouble()).toFloat() * 0.65f
    val peakShakeX =
        sin((sceneTime * 5.1f).toDouble()).toFloat() * 4.8f * peak +
            sin((sceneTime * 3.2f + 0.4f).toDouble()).toFloat() * 2.2f * peak
    val peakShakeY =
        sin((sceneTime * 4.4f + 0.5f).toDouble()).toFloat() * 2.0f * peak
    val breathingOffset = sin((sceneTime * 1.35f).toDouble()).toFloat() * 3.5f
    val breathingScale = 1f +
        sin((sceneTime * 1.1f).toDouble()).toFloat() * 0.018f +
        peak * 0.035f +
        heartbeat * 0.018f -
        collapse * 0.018f
    val turbulence = 0.24f + peak * 0.54f + revealWarp * 0.4f + bloodProgress * 0.16f + madness * 0.16f
    val characterWarpX =
        sin((sceneTime * 4.7f).toDouble()).toFloat() * turbulence * 1.7f +
            sin((sceneTime * 17f).toDouble()).toFloat() * revealWarp * 1.4f
    val characterWarpY =
        sin((sceneTime * 3.9f + 1.4f).toDouble()).toFloat() * turbulence * 1.1f +
            sin((sceneTime * 15f + 0.8f).toDouble()).toFloat() * revealWarp * 1.1f
    val panelShape = RoundedCornerShape(24.dp)
    val crimson = Color(0xFFD3132F)
    val violet = Color(0xFF9B5CFF)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(146.dp)
            .graphicsLayer {
                translationX = cinematicShakeX + peakShakeX
                translationY = cinematicShakeY + peakShakeY
                rotationZ = sin((sceneTime * 1.02f).toDouble()).toFloat() * 0.16f * turbulence
                scaleX = 1f +
                    sin((sceneTime * 1.55f).toDouble()).toFloat() * 0.0035f * turbulence +
                    heartbeat * 0.006f
                scaleY = 1f +
                    sin((sceneTime * 1.75f + 0.6f).toDouble()).toFloat() * 0.004f * turbulence +
                    heartbeat * 0.008f -
                    collapse * 0.006f
                alpha = 1f - collapse * 0.08f
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .shadow(24.dp, panelShape, clip = false)
                .clip(panelShape)
                .background(Color(0xFF09070C))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            crimson.copy(alpha = 0.78f + heartbeat * 0.22f),
                            Color.White.copy(alpha = 0.12f + heartbeat * 0.24f),
                            violet.copy(alpha = 0.58f + madness * 0.18f)
                        )
                    ),
                    shape = panelShape
                )
                .clickable { onOpenFullPlayer() }
                .zIndex(1f)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFF060408),
                            Color(0xFF19111C),
                            Color(0xFF07060B)
                        )
                    )
                )

                val glowAlpha = 0.24f + peak * 0.3f + heartbeat * 0.34f + collapse * 0.12f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            crimson.copy(alpha = glowAlpha),
                            violet.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.57f, size.height * 0.45f),
                        radius = size.width * 0.48f
                    ),
                    radius = size.width * 0.48f,
                    center = Offset(size.width * 0.57f, size.height * 0.45f)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            crimson.copy(alpha = heartbeat * 0.28f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.56f, size.height * 0.48f),
                        radius = size.width * (0.22f + heartbeat * 0.16f)
                    ),
                    radius = size.width * (0.22f + heartbeat * 0.16f),
                    center = Offset(size.width * 0.56f, size.height * 0.48f)
                )

                drawSnowDust(sceneTime = sceneTime, alphaBoost = 0.2f + peak * 0.25f + bloodProgress * 0.12f)
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.08f + collapse * 0.08f),
                                Color.Black.copy(alpha = 0.36f),
                                Color.Black.copy(alpha = 0.7f + finalFade * 0.16f + collapse * 0.14f)
                            )
                        )
                    )
                    .zIndex(2f)
            )

            CrimsonDripLayer(
                progress = bloodProgress,
                intensity = bloodEnergy,
                sceneTime = sceneTime,
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(4f)
            )
        }

        Box(
            modifier = Modifier
                .size(148.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
                .graphicsLayer {
                    translationX = characterWarpX
                    translationY = breathingOffset + characterWarpY
                    rotationZ =
                        sin((sceneTime * 2.8f).toDouble()).toFloat() * turbulence * 0.46f +
                            sin((sceneTime * 16f).toDouble()).toFloat() * revealWarp * 0.8f
                    scaleX = breathingScale + revealWarp * 0.018f
                    scaleY = breathingScale - revealWarp * 0.012f
                    alpha = 0.86f + peak * 0.14f
                }
                .zIndex(3f)
        ) {
            PressureFieldLayer(
                intensity = pressure,
                sceneTime = sceneTime,
                modifier = Modifier
                    .size(176.dp)
                    .align(Alignment.Center)
                    .offset(y = 2.dp)
            )

            Box(
                modifier = Modifier
                    .size(124.dp)
                    .align(Alignment.Center)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                crimson.copy(alpha = 0.36f * peak),
                                violet.copy(alpha = 0.22f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Image(
                painter = painterResource(id = R.drawable.rezero_subaru),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 0.09f + afterimage * 0.18f
                        translationX = -3.8f * afterimage
                        translationY = 1.2f * afterimage
                        scaleX = 1.012f + heartbeat * 0.012f
                    }
            )

            Image(
                painter = painterResource(id = R.drawable.rezero_subaru),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 0.07f + afterimage * 0.15f
                        translationX = 3.4f * afterimage
                        translationY = -1.5f * afterimage
                        scaleY = 1.012f + heartbeat * 0.01f
                    }
            )

            Image(
                painter = painterResource(id = R.drawable.rezero_subaru),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = pressure * 0.08f + collapse * 0.08f
                        translationX = sin((sceneTime * 7.5f).toDouble()).toFloat() * 5.2f * madness
                        translationY = sin((sceneTime * 5.3f).toDouble()).toFloat() * 2.2f * madness
                        scaleX = 1f + madness * 0.026f
                        scaleY = 1f - madness * 0.012f
                    }
            )

            Image(
                painter = painterResource(id = R.drawable.rezero_subaru),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 1f - collapse * 0.16f
                    }
            )
        }

        whyTextCues
            .filter { cue -> sceneTime in cue.startSeconds..cue.endSeconds }
            .forEachIndexed { index, cue ->
                WhyQuestionText(
                    cue = cue,
                    progress = whyCueProgress(sceneTime, cue),
                    sceneTime = sceneTime,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = cue.x.dp, y = cue.y.dp)
                        .zIndex(5f + index * 0.01f)
                )
            }

        if (currentSubtitle != null) {
            ShowcaseSubtitleText(
                text = currentSubtitle.text,
                progress = subtitleProgress,
                intensity = maxOf(peak, revealWarp),
                sceneTime = sceneTime,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 20.dp)
                    .zIndex(6f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .zIndex(8f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.62f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(14.dp)
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.rezero_subaru),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer(scaleX = 1.5f, scaleY = 1.5f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = track.artist,
                        color = Color(0xFFCBBED6),
                        maxLines = 1
                    )
                }

                ShowcaseControlButton(
                    label = if (isPlaying) "II" else ">",
                    size = 44.dp,
                    sceneTime = sceneTime,
                    intensity = madness,
                    onClick = onPlayPauseClick
                )

                Spacer(modifier = Modifier.width(8.dp))

                ShowcaseControlButton(
                    label = "EQ",
                    size = 40.dp,
                    sceneTime = sceneTime + 0.7f,
                    intensity = madness * 0.8f,
                    onClick = onOpenFullPlayer
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth((0.08f + sceneTime / 36f + heartbeat * 0.018f).coerceIn(0.08f, 1f))
                    .height((3f + heartbeat * 2.2f).dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                crimson.copy(alpha = 0.86f + heartbeat * 0.14f),
                                Color(0xFFFF5B29).copy(alpha = heartbeat * 0.65f),
                                violet.copy(alpha = 0.82f)
                            )
                        )
                    )
            )
        }

        FinalCollapseLayer(
            progress = collapse,
            heartbeat = heartbeat,
            modifier = Modifier
                .matchParentSize()
                .zIndex(9f)
        )
    }
}

@Composable
private fun ShowcaseSubtitleText(
    text: String,
    progress: Float,
    intensity: Float,
    sceneTime: Float,
    modifier: Modifier = Modifier
) {
    val reveal = smoothStep((progress / 0.48f).coerceIn(0f, 1f))
    val fadeIn = smoothStep((progress / 0.3f).coerceIn(0f, 1f))
    val fadeOut = smoothStep(((1f - progress) / 0.28f).coerceIn(0f, 1f))
    val alpha = minOf(fadeIn, fadeOut)
    val warp = revealWarp(progress)
    val warpX =
        sin((sceneTime * 8.0f).toDouble()).toFloat() * warp * 3.0f +
            sin((sceneTime * 17f).toDouble()).toFloat() * warp * 1.25f
    val warpY =
        sin((sceneTime * 6.5f + 0.8f).toDouble()).toFloat() * warp * 1.7f
    val red = Color(0xFFFF1738)

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = warpX
                translationY = warpY
                scaleX = 1f + intensity * 0.035f
                scaleY = 1f + intensity * 0.025f
            }
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        red.copy(alpha = 0.24f * alpha),
                        Color.Black.copy(alpha = 0.34f * alpha),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.Transparent,
            textAlign = TextAlign.Start,
            maxLines = 2,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 0.sp
        )

        Text(
            text = text,
            color = red.copy(alpha = 0.34f * alpha),
            textAlign = TextAlign.Start,
            maxLines = 2,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 0.sp,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = 1.08f
                    scaleY = 1.2f
                }
                .drawWithContent {
                    clipRect(right = size.width * reveal) {
                        this@drawWithContent.drawContent()
                    }
                }
        )

        Text(
            text = text,
            color = red.copy(alpha = alpha),
            textAlign = TextAlign.Start,
            maxLines = 2,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium.copy(
                shadow = Shadow(
                    color = red.copy(alpha = 0.86f * alpha),
                    offset = Offset.Zero,
                    blurRadius = 18f
                )
            ),
            letterSpacing = 0.sp,
            modifier = Modifier.drawWithContent {
                clipRect(right = size.width * reveal) {
                    this@drawWithContent.drawContent()
                }
            }
        )
    }
}

@Composable
private fun WhyQuestionText(
    cue: WhyTextCue,
    progress: Float,
    sceneTime: Float,
    modifier: Modifier = Modifier
) {
    val fadeIn = smoothStep((progress / 0.24f).coerceIn(0f, 1f))
    val fadeOut = smoothStep(((1f - progress) / 0.3f).coerceIn(0f, 1f))
    val alpha = minOf(fadeIn, fadeOut) * cue.alpha
    val restlessX =
        sin((sceneTime * cue.floatSpeed + cue.phase).toDouble()).toFloat() * cue.drift
    val restlessY =
        sin((sceneTime * (cue.floatSpeed * 0.72f) + cue.phase * 1.7f).toDouble()).toFloat() * cue.drift * 0.52f
    val revealScale = 0.78f + fadeIn * 0.22f
    val red = Color(0xFFFF1738)

    Box(
        modifier = modifier.graphicsLayer {
            translationX = restlessX
            translationY = restlessY
            rotationZ = cue.rotation + sin((sceneTime * 1.6f + cue.phase).toDouble()).toFloat() * 3.5f
            scaleX = cue.scale * revealScale
            scaleY = cue.scale * revealScale
            this.alpha = alpha
        }
    ) {
        Text(
            text = "Why?",
            color = red.copy(alpha = 0.22f),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = cue.fontSize.sp,
            letterSpacing = 0.sp,
            modifier = Modifier.graphicsLayer {
                scaleX = 1.28f
                scaleY = 1.38f
            }
        )

        Text(
            text = "Why?",
            color = red.copy(alpha = 0.86f),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = cue.fontSize.sp,
            letterSpacing = 0.sp,
            style = MaterialTheme.typography.bodyMedium.copy(
                shadow = Shadow(
                    color = red.copy(alpha = 0.95f),
                    offset = Offset.Zero,
                    blurRadius = 22f
                )
            )
        )
    }
}

@Composable
private fun CrimsonDripLayer(
    progress: Float,
    intensity: Float,
    sceneTime: Float,
    modifier: Modifier = Modifier
) {
    if (progress <= 0f) return

    Canvas(modifier = modifier) {
        val mainAlpha = (0.16f + progress * 0.48f + intensity * 0.18f).coerceIn(0f, 0.84f)
        val topHeight = size.height * (0.1f + progress * 0.34f)
        val edgePath = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, topHeight * 0.74f)

            for (step in 18 downTo 0) {
                val x = size.width * (step / 18f)
                val waveA = sin((step * 1.73f + sceneTime * 0.32f).toDouble()).toFloat()
                val waveB = sin((step * 3.21f + progress * 5.4f).toDouble()).toFloat()
                val jaggedY = topHeight * (0.58f + waveA * 0.16f + waveB * 0.1f)
                lineTo(x, jaggedY.coerceIn(size.height * 0.08f, size.height * 0.62f))
            }

            close()
        }

        drawPath(
            path = edgePath,
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF3A0108).copy(alpha = mainAlpha * 0.92f),
                    Color(0xFFB2081E).copy(alpha = mainAlpha),
                    Color(0xFF59000B).copy(alpha = mainAlpha * 0.7f),
                    Color.Transparent
                )
            )
        )

        val drips = listOf(
            BloodDrip(0.14f, 0.78f, 14f, 0.0f),
            BloodDrip(0.29f, 0.46f, 9f, 0.1f),
            BloodDrip(0.44f, 0.64f, 11f, 0.16f),
            BloodDrip(0.59f, 0.96f, 18f, 0.04f),
            BloodDrip(0.75f, 0.58f, 10f, 0.2f),
            BloodDrip(0.89f, 0.42f, 8f, 0.28f)
        )

        drips.forEachIndexed { index, drip ->
            val localProgress = ((progress - drip.delay) / (1f - drip.delay)).coerceIn(0f, 1f)
            if (localProgress > 0f) {
                val eased = smoothStep(localProgress)
                val dripHeight = size.height * (0.18f + 0.72f * eased) * drip.lengthFactor
                val dripWidth = drip.widthPx * (1f + intensity * 0.22f)
                val baseX = size.width * drip.xFraction
                val color = Color(0xFFB70A20).copy(alpha = mainAlpha * (0.7f + eased * 0.24f))
                val dark = Color(0xFF2A0006).copy(alpha = mainAlpha * 0.7f)
                val hotEdge = Color(0xFFFF5B29).copy(alpha = mainAlpha * 0.18f * eased)
                val centerLine = (0..8).map { segment ->
                    val t = segment / 8f
                    val wobbleA = sin((index * 1.9f + segment * 0.9f + progress * 4.2f).toDouble()).toFloat()
                    val wobbleB = sin((index * 0.8f + segment * 2.4f + sceneTime * 0.18f).toDouble()).toFloat()
                    Offset(
                        x = baseX + (wobbleA * 0.7f + wobbleB * 0.36f) * dripWidth * (0.48f + t),
                        y = topHeight * (0.28f + 0.08f * sin((index + segment).toDouble()).toFloat()) +
                            dripHeight * t
                    )
                }
                val flowPath = Path()

                centerLine.forEachIndexed { segment, point ->
                    val t = segment / 8f
                    val leftWidth = dripWidth * (0.78f - t * 0.26f) *
                        (0.82f + 0.18f * sin((segment * 2.1f + index).toDouble()).toFloat())
                    val leftPoint = Offset(point.x - leftWidth, point.y)
                    if (segment == 0) {
                        flowPath.moveTo(leftPoint.x, leftPoint.y)
                    } else {
                        flowPath.lineTo(leftPoint.x, leftPoint.y)
                    }
                }

                centerLine.asReversed().forEachIndexed { reverseIndex, point ->
                    val segment = centerLine.lastIndex - reverseIndex
                    val t = segment / 8f
                    val rightWidth = dripWidth * (0.58f - t * 0.18f) *
                        (0.9f + 0.2f * sin((segment * 1.7f + index * 0.6f).toDouble()).toFloat())
                    flowPath.lineTo(point.x + rightWidth, point.y)
                }
                flowPath.close()

                drawPath(
                    path = flowPath,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF2C0006).copy(alpha = mainAlpha * 0.72f),
                            color,
                            Color(0xFF5B000D).copy(alpha = mainAlpha * 0.78f)
                        )
                    )
                )

                centerLine.zipWithNext().forEachIndexed { segment, (start, end) ->
                    if (segment % 2 == 0) {
                        drawLine(
                            color = dark,
                            start = start.copy(x = start.x - dripWidth * 0.18f),
                            end = end.copy(x = end.x - dripWidth * 0.08f),
                            strokeWidth = dripWidth * 0.18f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                val endPoint = centerLine.last()
                drawCircle(
                    color = Color(0xFF78000F).copy(alpha = mainAlpha * 0.9f),
                    radius = dripWidth * (0.72f + eased * 0.42f),
                    center = endPoint
                )
                drawCircle(
                    color = hotEdge,
                    radius = dripWidth * 0.34f,
                    center = endPoint.copy(x = dripWidth * 0.22f + endPoint.x, y = endPoint.y - dripWidth * 0.12f)
                )
            }
        }

        for (i in 0 until 9) {
            val x = size.width * (((i * 29) % 100) / 100f)
            val y = topHeight * (0.45f + (i % 4) * 0.13f)
            drawRoundRect(
                color = Color(0xFFFF6A2A).copy(alpha = mainAlpha * 0.06f),
                topLeft = Offset(x, y),
                size = Size(18f + (i % 3) * 8f, 2.8f),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
    }
}

@Composable
private fun PressureFieldLayer(
    intensity: Float,
    sceneTime: Float,
    modifier: Modifier = Modifier
) {
    if (intensity <= 0f) return

    Canvas(modifier = modifier) {
        val pulse = 0.68f + heartbeatPulse(sceneTime, rate = 1.9f) * 0.42f
        val center = Offset(size.width * 0.5f, size.height * 0.42f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF190006).copy(alpha = 0.46f * intensity),
                    Color(0xFF4A0012).copy(alpha = 0.24f * intensity),
                    Color.Transparent
                ),
                center = center,
                radius = size.minDimension * (0.46f + intensity * 0.12f) * pulse
            ),
            radius = size.minDimension * (0.46f + intensity * 0.12f) * pulse,
            center = center
        )

        for (i in 0 until 7) {
            val angle = sceneTime * (0.35f + i * 0.03f) + i * 0.92f
            val x = center.x + sin(angle.toDouble()).toFloat() * size.width * (0.24f + i * 0.014f)
            val y = center.y + sin((angle * 1.8f + 0.5f).toDouble()).toFloat() * size.height * 0.24f

            drawCircle(
                color = Color(0xFFFF1738).copy(alpha = intensity * (0.035f + i * 0.006f)),
                radius = 12f + i * 2.8f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun FinalCollapseLayer(
    progress: Float,
    heartbeat: Float,
    modifier: Modifier = Modifier
) {
    if (progress <= 0f) return

    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = progress * 0.18f),
                    Color.Black.copy(alpha = progress * 0.46f)
                )
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFD3132F).copy(alpha = progress * (0.12f + heartbeat * 0.18f)),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.5f, size.height * 0.44f),
                radius = size.width * (0.18f + progress * 0.18f)
            ),
            radius = size.width * (0.18f + progress * 0.18f),
            center = Offset(size.width * 0.5f, size.height * 0.44f)
        )
    }
}

@Composable
private fun ShowcaseControlButton(
    label: String,
    size: Dp,
    sceneTime: Float,
    intensity: Float,
    onClick: () -> Unit
) {
    val glitchPulse = heartbeatPulse(sceneTime, rate = 1.72f) * intensity
    val glitchX =
        sin((sceneTime * 10.5f).toDouble()).toFloat() * glitchPulse * 1.4f
    val glitchY =
        sin((sceneTime * 7.3f + 0.6f).toDouble()).toFloat() * glitchPulse * 0.8f

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                translationX = glitchX
                translationY = glitchY
                rotationZ = sin((sceneTime * 5.8f).toDouble()).toFloat() * glitchPulse * 1.2f
                scaleX = 1f + glitchPulse * 0.035f
                scaleY = 1f - glitchPulse * 0.018f
            }
            .clip(RoundedCornerShape(15.dp))
            .background(Color.Black.copy(alpha = 0.34f + glitchPulse * 0.18f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.16f + glitchPulse * 0.22f),
                shape = RoundedCornerShape(15.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSnowDust(
    sceneTime: Float,
    alphaBoost: Float
) {
    for (i in 0 until 46) {
        val x = size.width * (((i * 37) % 100) / 100f)
        val baseY = size.height * (((i * 61) % 100) / 100f)
        val drift = ((sceneTime * (8f + i % 5) + i * 11f) % size.height)
        val y = (baseY + drift) % size.height
        val radius = 0.65f + (i % 3) * 0.38f
        val twinkle = 0.45f + 0.35f * sin((sceneTime * 1.8f + i.toFloat()).toDouble()).toFloat()

        drawCircle(
            color = Color.White.copy(alpha = (0.08f + alphaBoost * 0.16f) * twinkle),
            radius = radius,
            center = Offset(x, y)
        )
    }
}

private data class ShowcaseSubtitleCue(
    val startSeconds: Float,
    val endSeconds: Float,
    val text: String
)

private data class WhyTextCue(
    val startSeconds: Float,
    val endSeconds: Float,
    val x: Float,
    val y: Float,
    val rotation: Float,
    val scale: Float,
    val fontSize: Float,
    val alpha: Float,
    val drift: Float,
    val floatSpeed: Float,
    val phase: Float
)

private data class BloodDrip(
    val xFraction: Float,
    val lengthFactor: Float,
    val widthPx: Float,
    val delay: Float
)

private val showcaseSubtitles = listOf(
    ShowcaseSubtitleCue(2.0f, 5.0f, "THIS IS WRONG"),
    ShowcaseSubtitleCue(5.0f, 7.0f, "THIS ISN'T WHAT I WANTED"),
    ShowcaseSubtitleCue(7.0f, 11.0f, "WHY DON'T YOU REMEMBER!?"),
    ShowcaseSubtitleCue(10.4f, 15.2f, "WHY DO YOU ALL KEEP LEAVING ME BEHIND"),
    ShowcaseSubtitleCue(15.8f, 20.8f, "WHAT DID I DO TO YOU?"),
    ShowcaseSubtitleCue(28.0f, 30.7f, "KILL HER!"),
    ShowcaseSubtitleCue(30.8f, 35.4f, "I FEEL SICK")
)

private val whyTextCues = listOf(
    WhyTextCue(22.0f, 25.8f, -88f, 16f, -18f, 0.9f, 14f, 0.82f, 3.8f, 1.6f, 0.3f),
    WhyTextCue(22.5f, 27.2f, 74f, 24f, 16f, 0.78f, 12f, 0.72f, 2.8f, 1.35f, 1.1f),
    WhyTextCue(23.4f, 29.8f, -58f, 54f, 9f, 1.05f, 16f, 0.9f, 4.6f, 1.15f, 2.4f),
    WhyTextCue(24.3f, 30.4f, 96f, 58f, -24f, 0.94f, 15f, 0.86f, 3.2f, 1.5f, 3.3f),
    WhyTextCue(25.8f, 32.0f, -112f, 76f, 28f, 0.7f, 11f, 0.66f, 2.4f, 1.85f, 4.0f),
    WhyTextCue(26.6f, 33.4f, 48f, 6f, -8f, 1.16f, 17f, 0.92f, 5.0f, 1.05f, 5.2f),
    WhyTextCue(28.2f, 34.2f, 116f, 88f, 18f, 0.68f, 11f, 0.62f, 2.0f, 1.7f, 6.1f)
)

private fun cueProgress(
    sceneTime: Float,
    cue: ShowcaseSubtitleCue
): Float {
    val duration = (cue.endSeconds - cue.startSeconds).coerceAtLeast(0.1f)
    return ((sceneTime - cue.startSeconds) / duration).coerceIn(0f, 1f)
}

private fun whyCueProgress(
    sceneTime: Float,
    cue: WhyTextCue
): Float {
    val duration = (cue.endSeconds - cue.startSeconds).coerceAtLeast(0.1f)
    return ((sceneTime - cue.startSeconds) / duration).coerceIn(0f, 1f)
}

private fun revealWarp(progress: Float): Float {
    return if (progress < 0.38f) {
        (1f - progress / 0.38f).coerceIn(0f, 1f)
    } else {
        0f
    }
}

private fun sustainedProgress(
    time: Float,
    start: Float,
    full: Float
): Float {
    if (time <= start) return 0f
    val progress = ((time - start) / (full - start)).coerceIn(0f, 1f)
    return smoothStep(progress)
}

private fun pulseWindow(
    time: Float,
    start: Float,
    end: Float
): Float {
    if (time <= start || time >= end) return 0f
    val progress = ((time - start) / (end - start)).coerceIn(0f, 1f)
    return sin(progress.toDouble() * PI).toFloat().coerceIn(0f, 1f)
}

private fun heartbeatPulse(
    time: Float,
    rate: Float
): Float {
    val phase = (time * rate) % 1f
    val firstHit = triangularPulse(phase = phase, center = 0.08f, width = 0.07f)
    val secondHit = triangularPulse(phase = phase, center = 0.24f, width = 0.05f) * 0.62f
    return maxOf(firstHit, secondHit).coerceIn(0f, 1f)
}

private fun triangularPulse(
    phase: Float,
    center: Float,
    width: Float
): Float {
    return (1f - abs(phase - center) / width).coerceIn(0f, 1f)
}

private fun smoothStep(value: Float): Float {
    val t = value.coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}
