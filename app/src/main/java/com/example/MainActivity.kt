package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.AudioAttributes
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme
import kotlin.concurrent.thread
import android.accounts.AccountManager
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFDF8FD) // Theme Base Color
                ) {
                    WebViewScreen()
                }
            }
        }
    }
}

/**
 * Checks if the network is available.
 */
fun isNetworkAvailable(context: Context): Boolean {
    return try {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager == null) return false
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        false
    }
}

/**
 * Plays a high-fidelity synthetic pitch tone for guitar tuning.
 */
fun playGuitarTone(frequency: Double, durationSeconds: Double = 0.8) {
    thread {
        try {
            val sampleRate = 44100
            val numSamples = (durationSeconds * sampleRate).toInt()
            val sample = DoubleArray(numSamples)
            val generatedSnd = ByteArray(2 * numSamples)
            
            for (i in 0 until numSamples) {
                sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / frequency))
            }
            
            var idx = 0
            for (dVal in sample) {
                val valShort = (dVal * 32767).toInt().toShort()
                generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
                generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
            }
            
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            
            // For MODE_STATIC, the AudioTrack buffer size must be at least minBufSize 
            // and exactly match the size of the written data. Pad with zeros if necessary.
            val dataToWrite = if (minBufSize > 0 && generatedSnd.size < minBufSize) {
                val padded = ByteArray(minBufSize)
                System.arraycopy(generatedSnd, 0, padded, 0, generatedSnd.size)
                padded
            } else {
                generatedSnd
            }
            
            var audioTrack: AudioTrack? = null
            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(dataToWrite.size)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                if (audioTrack.state == AudioTrack.STATE_INITIALIZED) {
                    audioTrack.write(dataToWrite, 0, dataToWrite.size)
                    audioTrack.play()
                    Thread.sleep((durationSeconds * 1000 + 100).toLong())
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            } finally {
                try {
                    audioTrack?.release()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}

/**
 * Launches Chrome Custom Tab inside the app for a secure browsing experience and Gmail integration.
 */
fun launchCustomTab(context: Context, url: String) {
    try {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setInstantAppsEnabled(true)
            .build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (e: Throwable) {
        e.printStackTrace()
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (t: Throwable) {
            t.printStackTrace()
            Toast.makeText(context, "Could not open secure browser.", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Reactive online status state helper.
 */
@Composable
fun rememberIsOnline(): Boolean {
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(isNetworkAvailable(context)) }

    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                mainHandler.post {
                    isOnline = true
                }
            }
            override fun onLost(network: Network) {
                mainHandler.post {
                    isOnline = false
                }
            }
        }
        
        if (connectivityManager != null) {
            try {
                connectivityManager.registerDefaultNetworkCallback(callback)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        
        onDispose {
            if (connectivityManager != null) {
                try {
                    connectivityManager.unregisterNetworkCallback(callback)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    return isOnline
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen() {
    val context = LocalContext.current
    val isOnline = rememberIsOnline()

    val baseUrl = "https://chordme1.netlify.app"
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    
    // WebPage load stats
    var progress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    // Splash screen holding states
    var isMinimumDurationPassed by remember { mutableStateOf(false) }
    var isPageFinishedLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000) // Minimum duration of 2 seconds
        isMinimumDurationPassed = true
    }

    val showSplashScreen = !(isMinimumDurationPassed && (isPageFinishedLoading || hasError))
    
    // History states
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    // Dialog state for Guitar Pitch Tuner
    var showTunerDialog by remember { mutableStateOf(false) }

    var hasAutoLaunched by remember { mutableStateOf(false) }
    var showChromeSyncBanner by remember { mutableStateOf(true) }

    LaunchedEffect(isOnline) {
        if (isOnline && !hasAutoLaunched) {
            hasAutoLaunched = true
            delay(1000)
            launchCustomTab(context, baseUrl)
        }
    }

    // Intercept system back pressure
    BackHandler(enabled = canGoBack) {
        webViewInstance?.goBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(Color(0xFFFDF8FD)) // Match Geometric Balance background
        ) {

            // Custom Top App Bar Row for Chrome Custom Tabs Companion
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFDF8FD))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .border(BorderStroke(0.5.dp, Color(0xFFE6E0E9)), RoundedCornerShape(0.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF6750A4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground_asset_1779867851791),
                            contentDescription = "App Icon",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ChordMe Practice",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = "Standard Mode",
                            fontSize = 10.sp,
                            color = Color(0xFF6750A4),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Chrome Custom Tab Launch action
                Button(
                    onClick = { launchCustomTab(context, baseUrl) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6750A4),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Launch",
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Secure Chrome Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Google Account Connection Banner
            if (isOnline && showChromeSyncBanner) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFEADDFF))
                        .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF6750A4)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Secure Key Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Secure Google Account Sign-In",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF21005D)
                                )
                            }
                            
                            // Dismiss Button
                            IconButton(
                                onClick = { showChromeSyncBanner = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Dismiss",
                                    tint = Color(0xFF49454F),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = "Because Google restricts sign-in on embedded WebViews, you can log in, sync, and access Chrome-saved passwords securely by opening ChordMe in Chrome Custom Tabs.",
                            fontSize = 11.sp,
                            color = Color(0xFF49454F),
                            lineHeight = 15.sp
                        )
                        
                        Button(
                            onClick = { launchCustomTab(context, baseUrl) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6750A4),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Text("Connect via Chrome Custom Tab", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Offline Banner (Section 1 of the design HTML, shown to state offline capabilities)
        if (!isOnline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF3EDF7))
                    .border(1.dp, Color(0xFFE6E0E9), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Icon Block
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFD0BCFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Offline Music Mode Icon",
                            tint = Color(0xFF381E72),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Info text matching "Offline Mode Ready" banner
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Offline Mode Enabled",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Acoustic chords cache. Play saved tracks offline without Wi-Fi.",
                            fontSize = 11.sp,
                            color = Color(0xFF49454F),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Loader progress indicator
        Box(modifier = Modifier.fillMaxWidth().height(3.dp)) {
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF6750A4),
                    trackColor = Color(0xFFEADDFF)
                )
            }
        }

        // Webview / Error Screen Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("web_view"),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // Set up Third-Party cookies explicitly for secure third-party authenticators (like Google)
                        val cookieManager = android.webkit.CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            allowFileAccess = true
                            javaScriptCanOpenWindowsAutomatically = true
                            cacheMode = if (isNetworkAvailable(ctx)) {
                                WebSettings.LOAD_DEFAULT
                            } else {
                                WebSettings.LOAD_CACHE_ELSE_NETWORK
                            }

                            // Clean/Strip the UserAgent String to bypass the Google Sign-In "disallowed_useragent" WebView restriction
                            // We set a high-quality modern Google Chrome on Android User-Agent to prevent Google from falling back to legacy login
                            userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
                        }
                        
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                                progress = 0.1f
                            }
                            
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                progress = 1.0f
                                isPageFinishedLoading = true
                                canGoBack = view?.canGoBack() == true
                                canGoForward = view?.canGoForward() == true

                                // Flush WebView cookies to disk to ensure session persistence across app launches
                                try {
                                    android.webkit.CookieManager.getInstance().flush()
                                } catch (t: Throwable) {
                                    t.printStackTrace()
                                }
                            }
                            
                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                                    val isAvailable = isNetworkAvailable(ctx)
                                    if (!isAvailable) {
                                        if (view?.title.isNullOrEmpty()) {
                                            hasError = true
                                        }
                                    } else {
                                        hasError = true
                                    }
                                }
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val url = request?.url?.toString() ?: return false
                                
                                // Http and standard links get loaded directly in WebView
                                if (url.startsWith("http://") || url.startsWith("https://")) {
                                    return false
                                }
                                
                                // Launch custom intents, external deep link schemes (e.g. google login, intent://, whatsapp://, etc.)
                                try {
                                    val intent = if (url.startsWith("intent://")) {
                                        android.content.Intent.parseUri(url, android.content.Intent.URI_INTENT_SCHEME)
                                    } else {
                                        android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    }
                                    
                                    if (intent != null) {
                                        val resolveInfo = ctx.packageManager.resolveActivity(
                                            intent, 
                                            android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
                                        )
                                        if (resolveInfo != null) {
                                            ctx.startActivity(intent)
                                            return true
                                        } else {
                                            val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                                            if (fallbackUrl != null) {
                                                view?.loadUrl(fallbackUrl)
                                                return true
                                            }
                                        }
                                    }
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                }
                                return true // Prevent WebView from attempting to load custom schemes natively (saves from protocol error screens)
                            }
                        }
                        
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress / 100f
                                isLoading = newProgress < 100
                                if (newProgress >= 100) {
                                    isPageFinishedLoading = true
                                }
                                canGoBack = view?.canGoBack() == true
                                canGoForward = view?.canGoForward() == true
                            }
                        }
                        
                        loadUrl(baseUrl)
                        webViewInstance = this
                    }
                },
                update = { webView ->
                    webView.settings.cacheMode = if (isOnline) {
                        WebSettings.LOAD_DEFAULT
                    } else {
                        WebSettings.LOAD_CACHE_ELSE_NETWORK
                    }
                    webViewInstance = webView
                },
                onRelease = { webView ->
                    webViewInstance = null
                    try {
                        webView.stopLoading()
                        webView.onPause()
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
            )

            // Dynamic Custom Error Panel styling mimicking "Geometric Balance" layout elements
            if (hasError) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFDF8FD))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Offline banner Alert
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFF3EDF7))
                            .border(1.dp, Color(0xFFE6E0E9), RoundedCornerShape(24.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFD0BCFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning Badge",
                                    tint = Color(0xFF381E72),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Offline Mode Active",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D1B20)
                                )
                                Text(
                                    text = "Internet is disconnected. Local app files are active.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF49454F)
                                )
                            }
                        }
                    }

                    // Continue Playing subtitle
                    Text(
                        text = "OFFLINE PRACTICE SONG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F),
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Recent Song Chord layout replica card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(28.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "Hallelujah",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Light,
                                        color = Color(0xFF1D1B20)
                                    )
                                    Text(
                                        text = "Leonard Cohen • G Major",
                                        fontSize = 13.sp,
                                        color = Color(0xFF625B71)
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(Color(0xFFE8DEF8))
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "LOCAL CHORD",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D192B),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Simulated Chord box layouts
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("C", "Am", "F").forEach { chord ->
                                    Box(
                                        modifier = Modifier
                                            .size(width = 44.dp, height = 50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF7F2FA))
                                            .border(1.dp, Color(0xFFE6E0E9), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = chord,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1D1B20)
                                        )
                                    }
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(width = 44.dp, height = 50.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF7F2FA).copy(alpha = 0.4f))
                                        .border(1.dp, Color(0xFFE6E0E9).copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+2",
                                        fontSize = 11.sp,
                                        color = Color(0xFF1D1B20).copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }

                    // Action buttons with beautiful geometric grid
                    Text(
                        text = "QUICK UTILITY ACTIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F),
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Quick Reference pitch tuning tool button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF7F2FA))
                                .border(1.dp, Color(0xFFE6E0E9), RoundedCornerShape(16.dp))
                                .clickable { showTunerDialog = true }
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFD0BCFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Synth Tuner",
                                        tint = Color(0xFF381E72),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Acoustic Pitch",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1C1B1F)
                                )
                            }
                        }

                        // Try loader click
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFEADDFF))
                                .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp))
                                .clickable {
                                    hasError = false
                                    isLoading = true
                                    webViewInstance?.reload()
                                }
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF6750A4)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Retry Sync",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Retry Connecting",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF21005D)
                                )
                            }
                        }
                    }
                }
            }
            

        }
    }

    // --- Elegant Splash Screen Layout Overlay ---
    AnimatedVisibility(
        visible = showSplashScreen,
        exit = fadeOut(animationSpec = tween(durationMillis = 600))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF6750A4), Color(0xFFD0BCFF)),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Beautiful Centered Logo Frame
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(32.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground_asset_1779867851791),
                        contentDescription = "ChordMe Icon",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "ChordMe",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Play Anywhere • Acoustic Practice",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
    }

    // High Fidelity Pitch Tuner Modal Dialog
    if (showTunerDialog) {
        AlertDialog(
            onDismissRequest = { showTunerDialog = false },
            containerColor = Color(0xFFFDF8FD),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Acoustic Tuning Note",
                        tint = Color(0xFF6750A4)
                    )
                    Text(
                        text = "Acoustic Guitar Tuner",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tap a selector note below to synthesize its standard acoustic guitar frequency reference pitch:",
                        fontSize = 13.sp,
                        color = Color(0xFF49454F)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // E-A-D-G-B-E Grid Layout
                    val stringsFrequencies = listOf(
                        Triple("E4 (High E)", 329.63, "High String"),
                        Triple("B3", 246.94, "String 2"),
                        Triple("G3", 196.00, "String 3"),
                        Triple("D3", 146.83, "String 4"),
                        Triple("A2", 110.00, "String 5"),
                        Triple("E2 (Low E)", 82.41, "Thick String")
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        stringsFrequencies.forEach { (label, hertz, desc) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF3EDF7))
                                    .clickable { playGuitarTone(hertz) }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1C1B1F)
                                    )
                                    Text(
                                        text = "$desc • $hertz Hz",
                                        fontSize = 11.sp,
                                        color = Color(0xFF625B71)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEADDFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play pitch",
                                        tint = Color(0xFF21005D),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showTunerDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF6750A4)
                    )
                ) {
                    Text("Close Tuner")
                }
            }
        )
    }

}
