package com.example.gymbro.ui
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Hiking
import androidx.compose.material.icons.rounded.Pool
import androidx.compose.material.icons.rounded.Snowboarding
import androidx.compose.material.icons.rounded.SportsGymnastics
import androidx.compose.material.icons.rounded.SportsKabaddi
import androidx.compose.material.icons.rounded.SportsMartialArts
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import kotlin.math.sqrt
import kotlin.random.Random

data class Particle(
    val xState: MutableFloatState,
    val yState: MutableFloatState,
    val rotationState: MutableFloatState,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    var rotSpeed: Float,
    val icon: ImageVector
) {
    var x: Float
        get() = xState.floatValue
        set(value) { xState.floatValue = value }
    var y: Float
        get() = yState.floatValue
        set(value) { yState.floatValue = value }
    var rotation: Float
        get() = rotationState.floatValue
        set(value) { rotationState.floatValue = value }
}

@Composable
fun DynamicBackground(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val iconColor = if (isDarkTheme) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black
        val iconAlpha = if (isDarkTheme) 0.15f else 0.15f
        
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()
            val density = LocalContext.current.resources.displayMetrics.density
            val paddingPx = 2f * density
            val repelRadius = 80f * density 
            
            if (width > 0 && height > 0) {
                val numSilhouettes = remember { Random.nextInt(67, 70) }
                val particles = remember { mutableListOf<Particle>() }
                
                val icons = remember {
                    listOf(
                        Icons.Rounded.FitnessCenter,
                        Icons.Rounded.SportsGymnastics,
                        Icons.Rounded.DirectionsRun,
                        Icons.Rounded.SportsMartialArts,
                        Icons.Rounded.DirectionsBike,
                        Icons.Rounded.DirectionsWalk,
                        Icons.Rounded.SportsKabaddi,
                        Icons.Rounded.Hiking,
                        Icons.Rounded.Snowboarding,
                        Icons.Rounded.Pool
                    )
                }
                
                if (particles.isEmpty()) {
                    val dailySeed = LocalDate.now().toEpochDay()
                    val random = Random(dailySeed)
                    
                    for (i in 0 until numSilhouettes) {
                        val sizeDp = random.nextFloat() * 16f + 16f 
                        val radius = (sizeDp * density) / 2f
                        
                        val x = (random.nextFloat() * width).coerceIn(radius, width - radius)
                        val y = (random.nextFloat() * height).coerceIn(radius, height - radius)
                        val speed = random.nextFloat() * 10f + 5f
                        val angle = random.nextFloat() * Math.PI.toFloat() * 2f
                        val vx = kotlin.math.cos(angle) * speed
                        val vy = kotlin.math.sin(angle) * speed
                        val rotSpeed = (random.nextFloat() * 8f + 2f) * (if (random.nextBoolean()) 1f else -1f)
                        val icon = icons[random.nextInt(icons.size)]
                        
                        particles.add(
                            Particle(
                                xState = mutableFloatStateOf(x),
                                yState = mutableFloatStateOf(y),
                                rotationState = mutableFloatStateOf(random.nextFloat() * 360f),
                                vx = vx,
                                vy = vy,
                                radius = radius,
                                rotSpeed = rotSpeed,
                                icon = icon
                            )
                        )
                    }
                }
                
                LaunchedEffect(Unit) {
                    var lastTime = withFrameNanos { it }
                    while (true) {
                        withFrameNanos { time ->
                            val dtNanos = time - lastTime
                            lastTime = time
                            val dt = (dtNanos / 1_000_000_000f).coerceAtMost(0.05f)
                            
                            val maxSpeed = 20f * density
                            val minSpeed = 2f * density
                            
                            for (i in particles.indices) {
                                val p1 = particles[i]
                                
                                for (j in i + 1 until particles.size) {
                                    val p2 = particles[j]
                                    val dx = p1.x - p2.x
                                    val dy = p1.y - p2.y
                                    val distSq = dx * dx + dy * dy
                                    
                                    val hardMinDist = p1.radius + p2.radius + paddingPx
                                    
                                    val isSimilar = p1.icon == p2.icon
                                    val effectiveRepelRadius = if (isSimilar) repelRadius * 1.5f else repelRadius
                                    
                                    if (distSq < effectiveRepelRadius * effectiveRepelRadius && distSq > 0.0001f) {
                                        val dist = sqrt(distSq)
                                        val nx = dx / dist
                                        val ny = dy / dist
                                        
                                        val force = (effectiveRepelRadius - dist) / effectiveRepelRadius
                                        val strengthMultiplier = if (isSimilar) 100f else 40f
                                        val overlapMult = if (dist < hardMinDist) 5f else 1f
                                        val repelStrength = force * (strengthMultiplier * overlapMult * density) * dt
                                        
                                        p1.vx += nx * repelStrength
                                        p1.vy += ny * repelStrength
                                        p2.vx -= nx * repelStrength
                                        p2.vy -= ny * repelStrength
                                    }
                                }
                                
                                val speedSq = p1.vx * p1.vx + p1.vy * p1.vy
                                if (speedSq > maxSpeed * maxSpeed) {
                                    val speed = sqrt(speedSq)
                                    p1.vx = (p1.vx / speed) * maxSpeed
                                    p1.vy = (p1.vy / speed) * maxSpeed
                                } else if (speedSq < minSpeed * minSpeed && speedSq > 0.0001f) {
                                    val speed = sqrt(speedSq)
                                    p1.vx = (p1.vx / speed) * minSpeed
                                    p1.vy = (p1.vy / speed) * minSpeed
                                }
                                
                                p1.x += p1.vx * dt
                                p1.y += p1.vy * dt
                                p1.rotation += p1.rotSpeed * dt
                                
                                if (p1.x - p1.radius < 0) { 
                                    p1.x = p1.radius
                                    if (p1.vx < 0) p1.vx *= -1 
                                } else if (p1.x + p1.radius > width) { 
                                    p1.x = width - p1.radius
                                    if (p1.vx > 0) p1.vx *= -1 
                                }
                                
                                if (p1.y - p1.radius < 0) { 
                                    p1.y = p1.radius
                                    if (p1.vy < 0) p1.vy *= -1 
                                } else if (p1.y + p1.radius > height) { 
                                    p1.y = height - p1.radius
                                    if (p1.vy > 0) p1.vy *= -1 
                                }
                            }
                        }
                    }
                }
                
                Box(modifier = Modifier.fillMaxSize()) {
                    particles.forEach { p ->
                        Icon(
                            imageVector = p.icon,
                            contentDescription = null,
                            tint = iconColor.copy(alpha = iconAlpha),
                            modifier = Modifier
                                .graphicsLayer {
                                    translationX = p.x - p.radius
                                    translationY = p.y - p.radius
                                    rotationZ = p.rotation
                                }
                                .size((p.radius * 2 / density).dp)
                        )
                    }
                }
            }
        }
        content()
    }
}
