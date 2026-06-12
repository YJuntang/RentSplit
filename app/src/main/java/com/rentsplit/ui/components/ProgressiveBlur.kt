package com.rentsplit.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import org.intellij.lang.annotations.Language

@Language("AGSL")
const val PROGRESSIVE_MASK_SHADER = """
    uniform shader blurredImage;
    uniform float startY;
    uniform float endY;
    uniform float direction;
    uniform half4 tintColor;
    
    uniform float saturationBoost;
    uniform float luminanceBoost;
    uniform float noiseAmount;
    
    // Simple pseudo-random hash for noise
    float hash(float2 p) {
        return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
    }
    
    half4 main(float2 fragCoord) {
        float fraction;
        if (direction > 0.0) { // Top Bar
            fraction = clamp((fragCoord.y - startY) / (endY - startY), 0.0, 1.0);
            fraction = 1.0 - fraction;
        } else { // Bottom Bar
            fraction = clamp((fragCoord.y - startY) / (endY - startY), 0.0, 1.0);
        }
        
        // Easing: Apple-style S-Curve (Cosine) for a much smoother, extended gradient fade
        float ease = 0.5 - 0.5 * cos(3.14159265 * fraction);
        
        half4 color = blurredImage.eval(fragCoord);
        
        // 1. Saturation Boost
        half3 lumaWeights = half3(0.2126, 0.7152, 0.0722);
        half luma = dot(color.rgb, lumaWeights);
        half3 saturatedColor = mix(half3(luma), color.rgb, saturationBoost);
        
        // 2. Luminance Adjustment
        half3 lumColor = saturatedColor * luminanceBoost;
        
        // 3. Mix the blurred background with the tint overlay (iOS style)
        half3 mixedRgb = mix(lumColor, tintColor.rgb, tintColor.a);
        
        // 4. Noise/Film Grain
        float noise = (hash(fragCoord) - 0.5) * noiseAmount;
        mixedRgb = clamp(mixedRgb + half3(noise), 0.0, 1.0);
        
        // Apply the progressive alpha mask over the entire layer
        return half4(mixedRgb, 1.0) * ease;
    }
"""

fun Modifier.progressiveBlurBackground(layer: GraphicsLayer): Modifier = this.then(
    Modifier.drawWithContent {
        layer.record {
            this@drawWithContent.drawContent()
        }
        drawLayer(layer)
    }
)

fun Modifier.progressiveBlur(
    backgroundLayer: GraphicsLayer,
    isTopBar: Boolean,
    tintColor: Color = Color.White.copy(alpha = 0.3f),
    blurRadius: Float = 120f,
    saturationBoost: Float = 1.3f,
    luminanceBoost: Float = 1.05f,
    noiseAmount: Float = 0.04f
): Modifier = composed {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return@composed this
    }

    var bounds by remember { mutableStateOf(Rect.Zero) }
    val tempLayer = rememberGraphicsLayer()
    
    val shader = remember { RuntimeShader(PROGRESSIVE_MASK_SHADER) }

    Modifier
        .onGloballyPositioned { bounds = it.boundsInWindow() }
        .drawWithContent {
            if (bounds.width > 0f && bounds.height > 0f) {
                // Update uniforms for this specific draw pass
                shader.setFloatUniform("startY", 0f)
                shader.setFloatUniform("endY", size.height)
                shader.setFloatUniform("direction", if (isTopBar) 1f else -1f)
                shader.setFloatUniform("tintColor", tintColor.red, tintColor.green, tintColor.blue, tintColor.alpha)
                shader.setFloatUniform("saturationBoost", saturationBoost)
                shader.setFloatUniform("luminanceBoost", luminanceBoost)
                shader.setFloatUniform("noiseAmount", noiseAmount)

                val blurEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
                val runtimeEffect = RenderEffect.createRuntimeShaderEffect(shader, "blurredImage")
                val chainedEffect = RenderEffect.createChainEffect(runtimeEffect, blurEffect)
                
                tempLayer.record(IntSize(size.width.toInt(), size.height.toInt())) {
                    translate(left = -bounds.left, top = -bounds.top) {
                        drawLayer(backgroundLayer)
                    }
                }
                
                tempLayer.renderEffect = chainedEffect.asComposeRenderEffect()
                drawLayer(tempLayer)
            }
            
            // Draw the actual content of the bar (icons, text) on top of the blurred background
            drawContent()
        }
}
