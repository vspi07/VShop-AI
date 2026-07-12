package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.outlined.Person
import kotlin.random.Random

import androidx.compose.foundation.BorderStroke

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.compose.ui.viewinterop.AndroidView
import android.net.Uri
import android.media.MediaPlayer
import android.widget.VideoView
import com.example.data.CartItem
import com.example.data.OrderEntity
import com.example.data.Product
import com.example.data.ShortVideo
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AppTab {
    FEED, CART, UPLOAD, PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(AppTab.FEED) }
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Checkout Success Dialog State
    var showCheckoutSuccess by remember { mutableStateOf(false) }
    var lastCreatedOrderId by remember { mutableStateOf("") }

    // Floating success toast/banner state
    var showAddedBanner by remember { mutableStateOf(false) }
    var bannerText by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black,
        bottomBar = {
            if (currentTab != AppTab.UPLOAD) {
                NavigationBar(
                    containerColor = Color.Black,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.05f))
                        .testTag("bottom_nav_bar")
                ) {
                    // FEED Tab
                    NavigationBarItem(
                        selected = currentTab == AppTab.FEED,
                        onClick = { currentTab = AppTab.FEED },
                        icon = {
                            CustomFeedIcon(selected = currentTab == AppTab.FEED)
                        },
                        label = {
                            Text(
                                "Feed",
                                color = if (currentTab == AppTab.FEED) Color.White else Color(0xFF8E8E93),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("tab_feed")
                    )

                    // CART Tab
                    NavigationBarItem(
                        selected = currentTab == AppTab.CART,
                        onClick = { currentTab = AppTab.CART },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (cartItems.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .offset(x = 4.dp, y = (-4).dp)
                                                .size(16.dp)
                                                .background(Color.White, CircleShape)
                                                .border(0.5.dp, Color.Black, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                cartItems.sumOf { it.quantity }.toString(),
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == AppTab.CART) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag,
                                    contentDescription = "Shopping",
                                    tint = if (currentTab == AppTab.CART) Color.White else Color(0xFF8E8E93)
                                )
                            }
                        },
                        label = {
                            Text(
                                "Cart",
                                color = if (currentTab == AppTab.CART) Color.White else Color(0xFF8E8E93),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("tab_cart")
                    )

                    // PROFILE Tab
                    NavigationBarItem(
                        selected = currentTab == AppTab.PROFILE,
                        onClick = { currentTab = AppTab.PROFILE },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Profile",
                                tint = if (currentTab == AppTab.PROFILE) Color.White else Color(0xFF8E8E93)
                            )
                        },
                        label = {
                            Text(
                                "Profile",
                                color = if (currentTab == AppTab.PROFILE) Color.White else Color(0xFF8E8E93),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("tab_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        var totalDragX by remember { mutableStateOf(0f) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(currentTab) {
                    detectHorizontalDragGestures(
                        onDragStart = { totalDragX = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            totalDragX += dragAmount
                        },
                        onDragEnd = {
                            when (currentTab) {
                                AppTab.UPLOAD -> {
                                    if (totalDragX < -120f) {
                                        // Swipe LEFT on Upload goes to Feed
                                        currentTab = AppTab.FEED
                                    }
                                }
                                AppTab.FEED -> {
                                    if (totalDragX < -120f) {
                                        // Swipe LEFT on Feed goes to Cart
                                        currentTab = AppTab.CART
                                    } else if (totalDragX > 120f) {
                                        // Swipe RIGHT on Feed goes to Upload
                                        currentTab = AppTab.UPLOAD
                                    }
                                }
                                AppTab.CART -> {
                                    if (totalDragX < -120f) {
                                        // Swipe LEFT on Cart goes to Profile
                                        currentTab = AppTab.PROFILE
                                    } else if (totalDragX > 120f) {
                                        // Swipe RIGHT on Cart goes to Feed
                                        currentTab = AppTab.FEED
                                    }
                                }
                                AppTab.PROFILE -> {
                                    if (totalDragX > 120f) {
                                        // Swipe RIGHT on Profile goes to Cart
                                        currentTab = AppTab.CART
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            // Main views routing
            when (currentTab) {
                AppTab.FEED -> {
                    FeedScreen(
                        viewModel = viewModel,
                        onAddToCart = { product ->
                            viewModel.addToCart(product)
                            bannerText = "Added ${product.name} to cart!"
                            coroutineScope.launch {
                                showAddedBanner = true
                                delay(2000)
                                showAddedBanner = false
                            }
                        },
                        onNavigateToProfile = { currentTab = AppTab.PROFILE }
                    )
                }
                AppTab.CART -> {
                    CartScreen(
                        viewModel = viewModel,
                        onPlaceOrderClicked = {
                            val activeCart = cartItems
                            if (activeCart.isNotEmpty()) {
                                viewModel.placeOrder()
                                lastCreatedOrderId = "ORD-${Random.nextInt(10000, 99999)}"
                                showCheckoutSuccess = true
                            }
                        }
                    )
                }
                AppTab.UPLOAD -> {
                    UploadScreen(
                        viewModel = viewModel,
                        onUploadSuccess = { currentTab = AppTab.FEED }
                    )
                }
                AppTab.PROFILE -> {
                    ProfileScreen(viewModel = viewModel, onBackClicked = { currentTab = AppTab.FEED })
                }
            }

            // Top added toast/banner alert
            AnimatedVisibility(
                visible = showAddedBanner,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFCD4D).copy(alpha = 0.6f)),
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFFFCD4D), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = bannerText,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Checkout Celebration Dialog
            if (showCheckoutSuccess) {
                Dialog(onDismissRequest = { showCheckoutSuccess = false }) {
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = Color(0xFF161616),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .padding(16.dp)
                            .testTag("checkout_success_dialog")
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Text(
                                text = "Order Placed Successfully!",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Thank you for shopping with VShop. Your fashion request is being processed under identifier $lastCreatedOrderId.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { showCheckoutSuccess = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFCD4D)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("dismiss_checkout_button")
                            ) {
                                Text(
                                    "Continue Shopping",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    viewModel: MainViewModel,
    onAddToCart: (Product) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val shorts by viewModel.filteredShorts.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { shorts.size })
    val scope = rememberCoroutineScope()
    var showCategoryDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (shorts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Videos in $selectedCategory",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "There are currently no style videos tagged in this category. Go ahead and publish your own!",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("vertical_feed_pager")
            ) { page ->
                val video = shorts[page]
                FeedItem(
                    video = video,
                    onAddToCart = onAddToCart,
                    onNavigateToProfile = onNavigateToProfile,
                    onPreviousVideo = {
                        if (pagerState.currentPage > 0) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    },
                    onNextVideo = {
                        if (pagerState.currentPage < shorts.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }
                )
            }
        }

        // Top Header overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { showCategoryDialog = true },
                    modifier = Modifier.size(36.dp).testTag("top_category_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Categories Menu",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = if (selectedCategory == "All") "VShop" else selectedCategory,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { /* Search action */ }
                )
                GridIcon(
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { /* Grid action */ }
                )
            }
        }

        // Beautiful Category Sheet Dialog Overlay
        if (showCategoryDialog) {
            Dialog(
                onDismissRequest = { showCategoryDialog = false }
            ) {
                Surface(
                    color = Color(0xFF161616),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(16.dp)
                        .testTag("category_selection_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = Color(0xFFFFCD4D),
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Select Category",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { showCategoryDialog = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = "Filter the video shopping feed by your favorite style collections:",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        // Category Choices
                        val categories = listOf(
                            Triple("All", "🌟 All Collections", "Everything in stock"),
                            Triple("Male", "🤵 Men's Drip", "Male Fashion & Sneakers"),
                            Triple("Female", "💃 Women's Style", "Female Fashion & Designer Wear"),
                            Triple("Accessories", "⌚ Accessories", "Watches, Shades & Tech")
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { (catId, label, desc) ->
                                val isSelected = selectedCategory == catId
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF8E2DE2).copy(alpha = 0.2f) else Color(0xFF1E1E1E)
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFF8E2DE2) else Color.White.copy(alpha = 0.05f)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectCategory(catId)
                                            showCategoryDialog = false
                                        }
                                        .testTag("category_option_$catId")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = label,
                                                color = if (isSelected) Color(0xFFFFCD4D) else Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = desc,
                                                color = Color.Gray,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = Color(0xFFFFCD4D),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedItem(
    video: ShortVideo,
    onAddToCart: (Product) -> Unit,
    onNavigateToProfile: () -> Unit,
    onPreviousVideo: () -> Unit,
    onNextVideo: () -> Unit
) {
    val context = LocalContext.current
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(video.likes) }
    var showProductCard by remember { mutableStateOf(false) }
    var isFollowing by remember { mutableStateOf(false) }

    // Video Mute/Unmute state (starts unmuted as requested)
    var isMuted by remember { mutableStateOf(false) }
    var mediaPlayerState by remember { mutableStateOf<MediaPlayer?>(null) }

    // Rating Dialog States
    var userRating by remember { mutableStateOf<Int?>(null) }
    var showRatingDialog by remember { mutableStateOf(false) }

    // More Options States
    var showMoreOptions by remember { mutableStateOf(false) }

    LaunchedEffect(isMuted, mediaPlayerState) {
        val mp = mediaPlayerState
        if (mp != null) {
            try {
                val vol = if (isMuted) 0f else 1f
                mp.setVolume(vol, vol)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    DisposableEffect(video.id) {
        onDispose {
            mediaPlayerState = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (video.videoUrl.isNotEmpty()) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        try {
                            setVideoURI(Uri.parse(video.videoUrl))
                            setOnPreparedListener { mp ->
                                mediaPlayerState = mp
                                mp.isLooping = true
                                val vol = if (isMuted) 0f else 1f
                                mp.setVolume(vol, vol)
                                start()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Fullscreen visual placeholder for the video stream
            AsyncImage(
                model = video.posterUrl,
                contentDescription = "Post content backdrop",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Semi-transparent overlay gradients for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        ),
                        startY = 0f
                    )
                )
        )

        // Interactive centered play simulation feedback overlay & side-switching controls
        var showPlayRipple by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        var boxWidth by remember { mutableStateOf(0) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { boxWidth = it.width }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (boxWidth > 0) {
                            val tapX = offset.x
                            if (tapX < boxWidth * 0.25f) {
                                // Left 25% tap -> Go to previous video
                                onPreviousVideo()
                            } else if (tapX > boxWidth * 0.75f) {
                                // Right 25% tap -> Go to next video
                                onNextVideo()
                            } else {
                                // Middle 50% tap -> Toggle mute state
                                isMuted = !isMuted
                                showPlayRipple = true
                                scope.launch {
                                    delay(600)
                                    showPlayRipple = false
                                }
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (showPlayRipple) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = if (isMuted) "Muted" else "Unmuted",
                        tint = Color.White,
                        modifier = Modifier.size(45.dp)
                    )
                }
            }
        }

        // ------------------ DIALOGS ------------------
        if (showRatingDialog) {
            Dialog(onDismissRequest = { showRatingDialog = false }) {
                Surface(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Rate this Outfit",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { star ->
                                val isSelected = (userRating ?: 0) >= star
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star $star",
                                    tint = if (isSelected) Color(0xFFFFCD4D) else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable {
                                            userRating = star
                                        }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(
                                onClick = { showRatingDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                            }
                            Button(
                                onClick = {
                                    if (userRating != null) {
                                        Toast.makeText(context, "Rated: $userRating Stars!", Toast.LENGTH_SHORT).show()
                                        showRatingDialog = false
                                    } else {
                                        Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Submit", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (showMoreOptions) {
            Dialog(onDismissRequest = { showMoreOptions = false }) {
                Surface(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Options",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                        
                        val options = listOf(
                            "Not Interested" to Icons.Default.FavoriteBorder,
                            "Report Video" to Icons.Default.Warning,
                            "Save to Collection" to Icons.Default.Star,
                            "Copy Video Link" to Icons.Default.Share
                        )
                        
                        options.forEach { (text, icon) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showMoreOptions = false
                                        when (text) {
                                            "Not Interested" -> {
                                                Toast.makeText(context, "We will show you fewer videos like this", Toast.LENGTH_SHORT).show()
                                            }
                                            "Report Video" -> {
                                                Toast.makeText(context, "Video reported. Thank you for making our community safe.", Toast.LENGTH_SHORT).show()
                                            }
                                            "Save to Collection" -> {
                                                Toast.makeText(context, "Video saved to collection!", Toast.LENGTH_SHORT).show()
                                            }
                                            "Copy Video Link" -> {
                                                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("VSP Video Link", "https://vshop.app/video/${video.id}")
                                                clipboardManager.setPrimaryClip(clip)
                                                Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = text,
                                    tint = Color.White
                                )
                                Text(text = text, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        // Right side Floating Social Buttons Panel (Matched to Screenshot)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Likes Interaction (Heart Outline/Filled)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        isLiked = !isLiked
                        likesCount = if (isLiked) "8.6K" else video.likes
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like button",
                        tint = if (isLiked) Color(0xFFFF4D4D) else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = likesCount,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // 2. Rating Interaction (Sparkle 4-point star)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SparkleStarIcon(
                    tint = if (userRating != null) Color(0xFFFFCD4D) else Color.White,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            showRatingDialog = true
                        }
                )
                Text(
                    text = if (userRating != null) userRating.toString() else video.rating,
                    color = if (userRating != null) Color(0xFFFFCD4D) else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 3. Shopping Bag / Tagged Product (Click to toggle card)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { showProductCard = !showProductCard },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingBag,
                        contentDescription = "Tagged product price",
                        tint = if (showProductCard) Color(0xFFFFCD4D) else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "₹${video.product.price.toInt().toString()}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // 4. Shares Interaction
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("VSP Video Link", "https://vshop.app/video/${video.id}")
                        clipboardManager.setPrimaryClip(clip)
                        Toast.makeText(context, "Link copied to clipboard! Share the look.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share post",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = video.shares,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // 5. More Options (...)
            IconButton(
                onClick = {
                    showMoreOptions = true
                },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "More",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // 6. Spinning Music Album Vinyl
            SpinningMusicAlbum(modifier = Modifier.padding(top = 4.dp))
        }

        // Bottom Left Creator Info (Matched to Screenshot)
        if (!showProductCard) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 24.dp, start = 16.dp)
            ) {
                // Circular avatar with white outline
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White, CircleShape)
                        .clickable { onNavigateToProfile() }
                ) {
                    AsyncImage(
                        model = video.userImg,
                        contentDescription = "Creator Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Username
                Text(
                    text = video.username,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                // Follow Button
                Box(
                    modifier = Modifier
                        .background(if (isFollowing) Color.White.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(16.dp))
                        .border(1.dp, if (isFollowing) Color.Transparent else Color.White, RoundedCornerShape(16.dp))
                        .clickable {
                            isFollowing = !isFollowing
                            if (isFollowing) {
                                Toast.makeText(context, "Following @${video.username}", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Unfollowed @${video.username}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isFollowing) "Following" else "Follow",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Slide-up Interactive Product Tag Card Overlay (Toggles when Clicking the Shopping Bag)
        AnimatedVisibility(
            visible = showProductCard,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.82f)
                .padding(bottom = 24.dp, start = 16.dp)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            model = video.product.imageUrl,
                            contentDescription = "Product Tag Image",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = video.product.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Card",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { showProductCard = false }
                                )
                            }
                            Text(
                                text = "₹${video.product.price.toInt().toString()}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onAddToCart(video.product)
                            Toast.makeText(context, "${video.product.name} added to cart!", Toast.LENGTH_SHORT).show()
                            showProductCard = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_to_cart_button_${video.product.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "Add",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartScreen(
    viewModel: MainViewModel,
    onPlaceOrderClicked: () -> Unit
) {
    var activeSubTab by remember { mutableStateOf("cart") } // "cart", "orders", "history"
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()

    val activeOrders = orders.filter { !it.isHistory }
    val historyOrders = orders.filter { it.isHistory }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("cart_screen_container")
    ) {
        // Sticky Top Navigation Header with Custom Selector Tab bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF070707))
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            Text(
                text = "My Shopping",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // Inner Tab Selectors: Cart, Orders, History
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                listOf(
                    Triple("cart", "Cart (${cartItems.sumOf { it.quantity }})", "cart_tab_trigger"),
                    Triple("orders", "Orders (${activeOrders.size})", "orders_tab_trigger"),
                    Triple("history", "History (${historyOrders.size})", "history_tab_trigger")
                ).forEach { (tabId, label, tag) ->
                    val isSelected = activeSubTab == tabId
                    Column(
                        modifier = Modifier
                            .clickable { activeSubTab = tabId }
                            .padding(bottom = 6.dp)
                            .testTag(tag),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color(0xFF8E8E93),
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .width(36.dp)
                                .background(
                                    if (isSelected) Color(0xFFFFCD4D) else Color.Transparent,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }

        // Sub Tab Content Router
        when (activeSubTab) {
            "cart" -> {
                CartTabContent(
                    viewModel = viewModel,
                    cartItems = cartItems,
                    onPlaceOrderClicked = onPlaceOrderClicked
                )
            }
            "orders" -> {
                OrdersTabContent(orders = activeOrders, title = "Active Orders")
            }
            "history" -> {
                OrdersTabContent(orders = historyOrders, title = "Delivered History")
            }
        }
    }
}

@Composable
fun CartTabContent(
    viewModel: MainViewModel,
    cartItems: List<CartItem>,
    onPlaceOrderClicked: () -> Unit
) {
    val aiAdvice by viewModel.aiAdvice.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    if (cartItems.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ShoppingBag,
                contentDescription = null,
                tint = Color.DarkGray,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Your Cart is Empty",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Go to the Feed tab and explore trendy reels to tag clothes and fill your collection!",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 16.dp)
        ) {
            // Cart Items
            items(cartItems, key = { it.id }) { item ->
                CartItemRow(item = item, viewModel = viewModel)
            }

            // AI STYLIST COMPONENT (GEMINI API)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF191122)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp))
                        .testTag("ai_stylist_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF8E2DE2).copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "✨ AI Fashion Stylist Advice",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Powered by Gemini 3.5 Flash",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Text(
                            text = "Want advice on pairing or styling the clothes in your bag?",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )

                        // Trigger Stylist button
                        Button(
                            onClick = { viewModel.askAiStylist() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8E2DE2)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("ask_stylist_button")
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Styling drip...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ask AI Stylist", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        // AI Output Display
                        if (aiAdvice.isNotEmpty()) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = aiAdvice,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ORDER TOTALS & BREAKDOWN SUMMARY
            item {
                Spacer(modifier = Modifier.height(8.dp))
                val subtotal = cartItems.sumOf { it.price * it.quantity }
                val tax = subtotal * 0.05
                val total = subtotal + tax

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF121212), RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Payment Summary", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", color = Color.Gray, fontSize = 13.sp)
                        Text("₹ ${subtotal.toInt().toString()}", color = Color.White, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estimated GST (5%)", color = Color.Gray, fontSize = 13.sp)
                        Text("₹ ${tax.toInt().toString()}", color = Color.White, fontSize = 13.sp)
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("₹ ${total.toInt().toString()}", color = Color(0xFFFFCD4D), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = onPlaceOrderClicked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFCD4D)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("checkout_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Black)
                            Text(
                                "Place Order (₹ ${total.toInt().toString()})",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    viewModel: MainViewModel
) {
    Surface(
        color = Color(0xFF161616),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Size: ${item.size} | Qty: ${item.quantity}",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Text(
                    text = "₹ ${(item.price * item.quantity).toInt().toString()}",
                    color = Color(0xFFFFCD4D),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Quantity Selectors
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Minus
                IconButton(
                    onClick = { viewModel.updateCartItemQty(item.id, -1) },
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .testTag("qty_minus_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Reduce quantity",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Text(
                    text = item.quantity.toString(),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                // Plus
                IconButton(
                    onClick = { viewModel.updateCartItemQty(item.id, 1) },
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .testTag("qty_plus_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase quantity",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Delete Row
                IconButton(
                    onClick = { viewModel.removeCartItem(item.id) },
                    modifier = Modifier.size(28.dp).testTag("delete_item_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete item",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OrdersTabContent(
    orders: List<OrderEntity>,
    title: String
) {
    if (orders.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = null,
                tint = Color.DarkGray,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Items in $title",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Placed orders will automatically appear here for tracking.",
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 16.dp)
        ) {
            items(orders) { order ->
                OrderCardRow(order = order)
            }
        }
    }
}

@Composable
fun OrderCardRow(order: OrderEntity) {
    Surface(
        color = Color(0xFF161616),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = order.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.id,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.date,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "Items count: ${order.itemsCount}",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )

                Text(
                    text = "Total amount: ₹ ${order.total.toInt().toString()}",
                    color = Color(0xFFFFCD4D),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Status Indicator
                val badgeColor = when (order.status) {
                    "Processing" -> Color(0xFFFFA500)
                    "Shipped" -> Color(0xFF00BFFF)
                    "Delivered" -> Color(0xFF4CAF50)
                    else -> Color.Gray
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(badgeColor, CircleShape)
                    )
                    Text(
                        text = order.status,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(viewModel: MainViewModel, onUploadSuccess: () -> Unit) {
    var activeView by remember { mutableStateOf("menu") } // "menu" | "caption_maker" | "upload_form"

    val topic by viewModel.captionTopic.collectAsStateWithLifecycle()
    val aiCaption by viewModel.aiCaption.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingCaption.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Video Upload Form States
    var selectedThumbnailIndex by remember { mutableStateOf(0) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedVideoUri = uri
    }
    var customCaption by remember { mutableStateOf("") }
    var taggedProductName by remember { mutableStateOf("") }
    var taggedProductPrice by remember { mutableStateOf("") }
    var taggedProductDesc by remember { mutableStateOf("") }
    var isUploadingPost by remember { mutableStateOf(false) }
    var selectedUploadCategory by remember { mutableStateOf("Male") }

    val thumbnails = listOf(
        "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&h=1000&fit=crop" to "Elegant Style",
        "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=600&h=1000&fit=crop" to "Street Drip",
        "https://images.unsplash.com/photo-1551028919-383718603bd5?w=600&h=1000&fit=crop" to "Biker Leather",
        "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=600&h=1000&fit=crop" to "Cyber Shades"
    )

    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp)
                .testTag("upload_screen_container")
        ) {
            // Sticky Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeView != "menu") {
                    IconButton(
                        onClick = {
                            if (activeView == "caption_maker") {
                                viewModel.resetCaption()
                            }
                            activeView = "menu"
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Text(
                    text = when (activeView) {
                        "caption_maker" -> "✨ AI Caption Maker"
                        "upload_form" -> "Publish Style Reel"
                        else -> "Upload & Tools"
                    },
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (activeView == "menu") {
                // Main upload list choices
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Choice 1: Gemini AI Caption Maker
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF191122)
                        ),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(28.dp))
                            .clickable { activeView = "caption_maker" }
                            .testTag("menu_option_caption_maker")
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFF8E2DE2).copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "✨ AI Caption Maker",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Let Gemini write the perfect viral Gen-Z style caption with trending fashion hashtags for your next video post.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }

                    // Choice 2: Product Video Tagging
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                customCaption = aiCaption
                                if (topic.isNotEmpty()) {
                                    taggedProductName = topic
                                }
                                activeView = "upload_form"
                            }
                            .testTag("menu_option_product_tagging")
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFFFFA500).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = Color(0xFFFFA500),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "Product Video Tagging",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Publish a video reel directly linking to your custom store catalog products.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }

                    // Choice 3: Fun Entertainment Video
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                customCaption = aiCaption
                                activeView = "upload_form"
                            }
                            .testTag("menu_option_entertainment_video")
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFF00BFFF).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = Color(0xFF00BFFF),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "Entertainment Video",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Post a casual review, try-on vlog, or fashion dance reel to gain followers.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            } else if (activeView == "caption_maker") {
                // Caption Maker UI Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF121212), RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "What is your video about?",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = topic,
                        onValueChange = { viewModel.setCaptionTopic(it) },
                        placeholder = { Text("e.g., Trying my new maroon winter coat look in New York...", color = Color.Gray, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFFCD4D),
                            focusedBorderColor = Color(0xFF8E2DE2),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            if (topic.trim().isNotEmpty() && !isGenerating) {
                                viewModel.generateAiCaption()
                            }
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("caption_input_field")
                    )

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.generateAiCaption()
                        },
                        enabled = topic.trim().isNotEmpty() && !isGenerating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8E2DE2),
                            disabledContainerColor = Color(0xFF8E2DE2).copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_caption_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Writing magic...", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate Magic Caption", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // AI Output results box
                    if (aiCaption.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF191122), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFF8E2DE2).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "AI Generated Caption",
                                    color = Color(0xFFD0BCFF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Text(
                                text = aiCaption,
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("VShop Caption", aiCaption)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Caption copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.05f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("copy_caption_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFFFFCD4D), modifier = Modifier.size(16.dp))
                                    Text("Copy to Clipboard", color = Color(0xFFFFCD4D), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    customCaption = aiCaption
                                    if (topic.isNotEmpty()) {
                                        taggedProductName = topic
                                    }
                                    activeView = "upload_form"
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF8E2DE2)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("use_caption_post_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.AddBox, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("Use Caption to Post Video", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else if (activeView == "upload_form") {
                // Post Style Reel Form UI
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF121212), RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "1. Select Short Video",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (selectedVideoUri == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { videoPickerLauncher.launch("video/*") }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = "Upload video",
                                    tint = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "Tap to select Short Video from Device",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Supported: MP4, WebM, MKV, 3GP",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFFFFCD4D), RoundedCornerShape(16.dp))
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        try {
                                            setVideoURI(selectedVideoUri)
                                            setOnPreparedListener { mp ->
                                                mp.isLooping = true
                                                start()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Overlay bar for status/actions
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Video Selected & Playing",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Change",
                                    color = Color(0xFFFFCD4D),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { videoPickerLauncher.launch("video/*") }
                                )
                            }
                        }
                    }

                    Text(
                        text = "2. Select Video Cover (Fallback)",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Horizontal Thumbnail row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        thumbnails.forEachIndexed { index, (url, label) ->
                            val isSelected = selectedThumbnailIndex == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.7f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFFFCD4D) else Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedThumbnailIndex = index }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = label,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                            )
                                        )
                                )
                                Text(
                                    text = label,
                                    color = if (isSelected) Color(0xFFFFCD4D) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(4.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "3. Video Caption",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = customCaption,
                        onValueChange = { customCaption = it },
                        placeholder = { Text("What is your style inspiration? Add fashion tags...", color = Color.Gray, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFFCD4D),
                            focusedBorderColor = Color(0xFF8E2DE2),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("upload_caption_field")
                    )

                    if (aiCaption.isNotEmpty() && customCaption != aiCaption) {
                        Button(
                            onClick = { customCaption = aiCaption },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E2DE2).copy(alpha = 0.15f)),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("🪄 Load Generated AI Caption", color = Color(0xFFD0BCFF), fontSize = 12.sp)
                        }
                    }

                    Text(
                        text = "4. Tag Product Details",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = taggedProductName,
                        onValueChange = { taggedProductName = it },
                        label = { Text("Product Name", color = Color.Gray, fontSize = 12.sp) },
                        placeholder = { Text("e.g., Slim Leather Biker Jacket", color = Color.Gray, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFFCD4D),
                            focusedBorderColor = Color(0xFF8E2DE2),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_product_name")
                    )

                    OutlinedTextField(
                        value = taggedProductPrice,
                        onValueChange = { taggedProductPrice = it },
                        label = { Text("Product Price ($)", color = Color.Gray, fontSize = 12.sp) },
                        placeholder = { Text("e.g., 2499.00", color = Color.Gray, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFFCD4D),
                            focusedBorderColor = Color(0xFF8E2DE2),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_product_price")
                    )

                    Text(
                        text = "5. Select Feed Category",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf("Male" to "🤵 Male", "Female" to "💃 Female", "Accessories" to "⌚ Accessories")
                        categories.forEach { (catId, label) ->
                            val isSelected = selectedUploadCategory == catId
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF8E2DE2).copy(alpha = 0.2f) else Color(0xFF1E1E1E)
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF8E2DE2) else Color.White.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedUploadCategory = catId }
                                    .testTag("upload_category_$catId")
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color(0xFFFFCD4D) else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (selectedVideoUri == null) {
                                Toast.makeText(context, "Please select/upload a video file!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (customCaption.trim().isEmpty()) {
                                Toast.makeText(context, "Please write a caption for your post!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (taggedProductName.trim().isEmpty()) {
                                Toast.makeText(context, "Please tag a product name!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val priceVal = taggedProductPrice.toDoubleOrNull() ?: 0.0
                            if (priceVal <= 0.0) {
                                Toast.makeText(context, "Please enter a valid product price!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isUploadingPost = true
                            coroutineScope.launch {
                                delay(1500) // Simulate server/cloud publication latency
                                viewModel.uploadVideo(
                                    caption = customCaption,
                                    productName = taggedProductName,
                                    price = priceVal,
                                    imageUrl = thumbnails[selectedThumbnailIndex].first,
                                    videoUrl = selectedVideoUri?.toString() ?: "",
                                    description = taggedProductDesc.ifEmpty { customCaption },
                                    category = selectedUploadCategory
                                )
                                isUploadingPost = false
                                Toast.makeText(context, "✨ Style Reel Published successfully!", Toast.LENGTH_LONG).show()
                                onUploadSuccess()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("publish_post_button")
                    ) {
                        Text("Publish Style Reel to Feed", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        // Animated full-screen translucent loading overlay during publishing
        if (isUploadingPost) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFFCD4D),
                        modifier = Modifier.size(56.dp),
                        strokeWidth = 4.dp
                    )
                    Text(
                        "Publishing style reel to VShop feed...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

sealed class ProfilePostItem {
    data class StaticImage(val imageUrl: String) : ProfilePostItem()
    data class UserShort(val video: ShortVideo) : ProfilePostItem()
}

@Composable
fun AwardRibbonIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Ribbon gold colors
        val goldColor = Color(0xFFFFC107) // Amber/Gold
        val darkGoldColor = Color(0xFFFF9800) // Darker gold/orange for shading
        
        // Left ribbon tail path
        val leftTail = Path().apply {
            moveTo(w * 0.22f, h * 0.45f)
            lineTo(w * 0.22f, h * 0.95f)
            lineTo(w * 0.45f, h * 0.78f)
            lineTo(w * 0.45f, h * 0.45f)
            close()
        }
        drawPath(leftTail, color = darkGoldColor)
        
        // Right ribbon tail path
        val rightTail = Path().apply {
            moveTo(w * 0.55f, h * 0.45f)
            lineTo(w * 0.55f, h * 0.78f)
            lineTo(w * 0.78f, h * 0.95f)
            lineTo(w * 0.78f, h * 0.45f)
            close()
        }
        drawPath(rightTail, color = darkGoldColor)
        
        // Circular badge body
        drawCircle(
            color = goldColor,
            radius = w * 0.36f,
            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.38f)
        )
    }
}

@Composable
fun HighlightItem(title: String, imageUrl: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                .padding(3.dp)
                .clip(CircleShape)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Text(
            text = title,
            color = Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ProfileScreen(viewModel: MainViewModel, onBackClicked: () -> Unit) {
    val staticPosts = viewModel.profilePosts
    val shorts by viewModel.shortsData.collectAsStateWithLifecycle()
    
    // Filter videos uploaded by user
    val userUploadedShorts = shorts.filter { it.username == "vshop_creator" }
    
    // Combine them: newest uploaded videos first
    val basePosts = userUploadedShorts.map { ProfilePostItem.UserShort(it) } + staticPosts.map { ProfilePostItem.StaticImage(it) }

    // Let's ensure the very first items in the post list are matching the screenshot:
    // 1. Man in blue suit
    // 2. Bearded man close up
    // 3. Orange beanie/stylish portrait
    val customMatchedPosts = listOf(
        ProfilePostItem.StaticImage("https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=600&h=600&fit=crop"), // Blue suit
        ProfilePostItem.StaticImage("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=600&h=600&fit=crop"), // Bearded portrait
        ProfilePostItem.StaticImage("https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=600&h=600&fit=crop")  // Beanie portrait
    )

    // Remove duplicates from base posts and join them so there is no loss of any existing uploads
    val filteredBasePosts = basePosts.filter { post ->
        val url = when (post) {
            is ProfilePostItem.StaticImage -> post.imageUrl
            is ProfilePostItem.UserShort -> post.video.posterUrl
        }
        url != "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=600&h=600&fit=crop" &&
        url != "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=600&h=600&fit=crop" &&
        url != "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=600&h=600&fit=crop"
    }

    val combinedPosts = customMatchedPosts + filteredBasePosts

    var selectedSubTab by remember { mutableStateOf("Post") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("profile_screen_container")
    ) {
        // 1. Tall Cover background image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=1000&fit=crop",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Transparent dark layer overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
        }

        // 2. Main scrollable content
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Spacer to let cover image shine
            item {
                Spacer(modifier = Modifier.height(280.dp))
            }

            // The elegant white sheet containing all profile details
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White,
                            RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        )
                        .padding(horizontal = 24.dp)
                        .padding(top = 28.dp, bottom = 12.dp)
                ) {
                    // Profile Header: Gold Ribbon, Name, Airplane Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AwardRibbonIcon(
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 6.dp)
                        )

                        Text(
                            text = "Hunny",
                            color = Color.Black,
                            fontSize = 32.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            imageVector = Icons.Default.AirplanemodeActive,
                            contentDescription = null,
                            tint = Color(0xFF2E3E5C),
                            modifier = Modifier
                                .size(28.dp)
                                .graphicsLayer(rotationZ = 45f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ID number and Rating row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ID: 489270",
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(Color(0xFFD1D5DB), CircleShape)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFCD4D),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "4.9 Rating",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Post, Follow, Following counts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Posts
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${combinedPosts.size}",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Posts",
                                color = Color(0xFF6B7280),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(Color(0xFFE5E7EB), CircleShape)
                        )
                        // Followers
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "84.2K",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Followers",
                                color = Color(0xFF6B7280),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(Color(0xFFE5E7EB), CircleShape)
                        )
                        // Following
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "327",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Following",
                                color = Color(0xFF6B7280),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Highlights Row: "New", "Goa", "Gym", "Vibes"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Highlight 1: New
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                                    .background(Color.White, CircleShape)
                                    .clickable { /* Add new highlight */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New Highlight",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "New",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Highlight 2: Goa
                        HighlightItem(
                            title = "Goa",
                            imageUrl = "https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=200&h=200&fit=crop"
                        )

                        // Highlight 3: Gym
                        HighlightItem(
                            title = "Gym",
                            imageUrl = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=200&h=200&fit=crop"
                        )

                        // Highlight 4: Vibes
                        HighlightItem(
                            title = "Vibes",
                            imageUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=200&h=200&fit=crop"
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Sub Tabs Bar: Post, Feed, Tag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("Post", "Feed", "Tag").forEach { tab ->
                            val isSelected = selectedSubTab == tab
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        selectedSubTab = tab
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = tab,
                                    color = if (isSelected) Color.Black else Color(0xFF9CA3AF),
                                    fontSize = 17.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                // Thick Indicator bar
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .width(48.dp)
                                            .height(3.dp)
                                            .background(Color.Black, RoundedCornerShape(1.5.dp))
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(3.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Grid posts displayed in rows of 3 inside White container background
            val chunkedPosts = combinedPosts.chunked(3)
            items(chunkedPosts) { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 24.dp, vertical = 1.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (i in 0 until 3) {
                        val item = rowItems.getOrNull(i)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        ) {
                            if (item != null) {
                                val (modelUrl, isVideo) = when (item) {
                                    is ProfilePostItem.StaticImage -> Pair(item.imageUrl, false)
                                    is ProfilePostItem.UserShort -> Pair(item.video.posterUrl, true)
                                }
                                AsyncImage(
                                    model = modelUrl,
                                    contentDescription = "Post Grid Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                if (isVideo) {
                                    Box(
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .size(20.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            .align(Alignment.TopEnd),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Video",
                                            tint = Color(0xFFFFCD4D),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }

            // White padding block at the bottom so it flows perfectly
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.White)
                )
            }
        }

        // 3. Top Action Buttons Row (Overlaid on Cover photo)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onBackClicked() },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /* Export action */ },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Export",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = { /* Settings action */ },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CustomFeedIcon(selected: Boolean) {
    if (selected) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color.White, RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier
                    .size(14.dp)
                    .offset(x = 1.dp)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, Color(0xFF8E8E93), RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color(0xFF8E8E93),
                modifier = Modifier
                    .size(12.dp)
                    .offset(x = 1.dp)
            )
        }
    }
}

@Composable
fun SparkleStarIcon(modifier: Modifier = Modifier, tint: Color = Color.White) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                val w = size.width
                val h = size.height
                moveTo(w / 2f, 0f)
                quadraticTo(w / 2f, h / 2f, w, h / 2f)
                quadraticTo(w / 2f, h / 2f, w / 2f, h)
                quadraticTo(w / 2f, h / 2f, 0f, h / 2f)
                quadraticTo(w / 2f, h / 2f, w / 2f, 0f)
                close()
            }
            drawPath(path, color = tint)
        }
    }
}

@Composable
fun GridIcon(modifier: Modifier = Modifier, tint: Color = Color.White) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(10.dp).border(1.5.dp, tint, RoundedCornerShape(2.dp)))
                Box(modifier = Modifier.size(10.dp).border(1.5.dp, tint, RoundedCornerShape(2.dp)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(10.dp).border(1.5.dp, tint, RoundedCornerShape(2.dp)))
                Box(modifier = Modifier.size(10.dp).border(1.5.dp, tint, RoundedCornerShape(2.dp)))
            }
        }
    }
}

@Composable
fun SpinningMusicAlbum(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .border(2.dp, Color.White, RoundedCornerShape(12.dp))
            .padding(6.dp)
            .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
            .background(Color.Black, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(Color.White, RoundedCornerShape(2.dp))
        )
    }
}
