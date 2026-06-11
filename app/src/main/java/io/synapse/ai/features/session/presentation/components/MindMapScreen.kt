package io.synapse.ai.features.session.presentation.components

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.synapse.ai.R
import io.synapse.ai.core.theme.AppTheme
import io.synapse.ai.core.theme.SemanticColors
import io.synapse.ai.core.theme.SynapseTheme
import io.synapse.ai.core.theme.synapse
import kotlin.math.roundToInt

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MindMapScreen(mermaidCode: String) {

    val colorScheme = MaterialTheme.colorScheme
    val semanticColors = MaterialTheme.synapse.semantic
    val isInspectionMode = LocalInspectionMode.current

    var isLoading by remember { mutableStateOf(true) }

    // Rebuild HTML only when content or theme flips – avoids redundant WebView reloads
    val htmlContent = remember(mermaidCode, colorScheme) {
        buildMindMapHtml(
            mermaidCode = mermaidCode,
            semanticColors = semanticColors,
            bgHex = colorScheme.background.toHexString(),
            outlineHex = colorScheme.outline.toHexString(),
            onSurfaceHex = colorScheme.onSurface.toHexString(),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                try {
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean = true

                            override fun onPageFinished(view: WebView?, url: String?) {
                                view?.postDelayed({ isLoading = false }, 800)
                    }
                        
                        }

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            setSupportZoom(true)
                        }

                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        @SuppressLint("ClickableViewAccessibility")
                        setOnTouchListener { v, _ ->
                            v.parent?.requestDisallowInterceptTouchEvent(true)
                            false
                        }
                        loadDataWithBaseURL(
                            "https://localhost",
                            htmlContent,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }
                } catch (e: Throwable) {
                    // Fallback for missing or broken WebView
                    TextView(context).apply {
                        text = context.getString(R.string.error_webview_unavailable)
                        setTextColor(android.graphics.Color.RED)
                    }
                }
            },
            update = { view ->
                if (view is WebView) {
                    isLoading = true
                    view.loadDataWithBaseURL(
                        "https://localhost",
                        htmlContent,
                        "text/html",
                        "UTF-8",
                        null
                    )
                } else {
                    isLoading = false
                }
            }
        )


        // ── Loading indicator ────────────────────────────────────────────────
        AnimatedVisibility(
            visible = isLoading && !isInspectionMode,
            enter = fadeIn(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
    }
}

private fun Color.toHexString(): String {
    val r = (red * 255).roundToInt().coerceIn(0, 255)
    val g = (green * 255).roundToInt().coerceIn(0, 255)
    val b = (blue * 255).roundToInt().coerceIn(0, 255)
    return "#%02X%02X%02X".format(r, g, b)
}

private fun buildMindMapHtml(
    mermaidCode: String,
    semanticColors: SemanticColors,
    bgHex: String,
    outlineHex: String,
    onSurfaceHex: String,
): String {

    val primaryHex = semanticColors.primary.toHexString()
    val onPrimaryHex = semanticColors.onPrimary.toHexString()
    val primaryBorder = semanticColors.primaryBorder.toHexString()
    val accentHex = semanticColors.accent.toHexString()
    val onAccentHex = semanticColors.onAccent.toHexString()
    val accentBorder = semanticColors.accentBorder.toHexString()
    val successHex = semanticColors.success.toHexString()
    val onSuccessHex = semanticColors.onSuccess.toHexString()
    val successBorder = semanticColors.successBorder.toHexString()

    val shadowAlpha = ".12"
    val dotOpacity = ".08"

    return """
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
  <meta charset="utf-8">
  <meta name="viewport"
        content="width=device-width, initial-scale=1.0, maximum-scale=6.0, user-scalable=yes">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=IBM+Plex+Sans+Arabic:wght@400;600;700&display=swap"
        rel="stylesheet">

  <style>
    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

    html, body {
      width: 100%; height: 100%;
      overflow: auto;
      -webkit-overflow-scrolling: touch;
      background: $bgHex;
      font-family: 'IBM Plex Sans Arabic', sans-serif;
    }

    .canvas {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 24px 16px;
      background-image: radial-gradient(
        circle,
        rgba(148,163,184,$dotOpacity) 1px,
        transparent 1px
      );
      background-size: 28px 28px;
    }

    .mermaid {
      color: transparent; 
      max-width: 100%;
      display: flex;
      justify-content: center;
    }

    .mermaid svg {
      border-radius: 20px;
      filter: drop-shadow(0 8px 32px rgba(0,0,0,$shadowAlpha));
      overflow: visible;
      animation: revealMap .55s ease forwards;
    }

    /* ── TEXT FIX (Always White) ─────────────────────────────────── */
    .mermaid svg .nodeLabel,
    .mermaid svg foreignObject,
    .mermaid svg foreignObject div,
    .mermaid svg foreignObject span,
    .mermaid svg foreignObject p,
    .mermaid svg foreignObject *,
    .mermaid svg text,
    .mermaid svg tspan {
      overflow: visible !important;
      color: #FFFFFF !important; /* لون أبيض دائم */
      fill: #FFFFFF !important;  /* لون أبيض دائم */
    }
    
    .mermaid svg foreignObject div {
      display: flex;
      justify-content: center;
      align-items: center;
      text-align: center;
      white-space: nowrap; 
    }
    @keyframes revealMap {
      from { opacity: 0; transform: scale(.97); }
      to   { opacity: 1; transform: scale(1);   }
    }
  </style>
</head>
<body>
  <div class="canvas">
    <pre class="mermaid">
$mermaidCode
    </pre>
  </div>

  <script type="module">
    import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.esm.min.mjs';

    mermaid.initialize({
      startOnLoad  : true,
      securityLevel: 'loose',
      theme        : 'base',

      themeVariables: {
        fontFamily : "'IBM Plex Sans Arabic', sans-serif",
        fontSize   : "15px",
        background : '$bgHex',
        mainBkg    : '$bgHex',

        primaryColor        : '$primaryHex',
        primaryTextColor    : '$onPrimaryHex',
        primaryBorderColor  : '$primaryBorder',

        secondaryColor      : '$accentHex',
        secondaryTextColor  : '$onAccentHex',
        secondaryBorderColor: '$accentBorder',

        tertiaryColor       : '$successHex',
        tertiaryTextColor   : '$onSuccessHex',
        tertiaryBorderColor : '$successBorder',

        lineColor           : '$outlineHex',
        labelTextColor      : '$onSurfaceHex',
        textColor           : '$onSurfaceHex',

        nodeBorder          : '1.5px',
        nodeRadius          : '12px'
      },

      mindmap: {
        padding     : 12,
      }
    });
  </script>
</body>
</html>
""".trimIndent()
}

@Preview(showBackground = true)
@Composable
fun MindMapScreenPreview() {
    val sample = """
        mindmap
          root((Algorithms Data Structures))
            Graph Algorithms
              Shortest Path
                Dijkstra Algorithm
                Bellman Ford
                A Star Search
              Spanning Trees
                Prim Algorithm
                Kruskal Algorithm
              Connectivity
                Tarjan Algorithm
                Kosaraju Algorithm
            Sorting and Searching
              Comparison Sorts
                QuickSort
                MergeSort
                Timsort
              Other Sorts
                Radix Sort
                Heap Sort
              String Matching
                Rabin Karp
                Knuth Morris Pratt
                Boyer Moore
            Data Structures
              Trees
                AVL and Red Black
                B Trees
                Radix Tree
              Hashing and Caches
                Hash Maps
                LRU Cache
              Advanced Sets
                Disjoint Set Union
                Sparse Table
            Theory and Paradigms
              Complexity Analysis
                Big O and Omega
                Master Theorem
                Amortized Analysis
              Design Paradigms
                Dynamic Programming
                Sliding Window
                Divide and Conquer
    """.trimIndent()
    SynapseTheme(appTheme = AppTheme.SYSTEM) {
        MindMapScreen(mermaidCode = sample)
    }
}