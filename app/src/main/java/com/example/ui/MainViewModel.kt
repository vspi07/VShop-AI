package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.AppDatabase
import com.example.data.CartItem
import com.example.data.OrderEntity
import com.example.data.Product
import com.example.data.ShortVideo
import com.example.data.VShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VShopRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = VShopRepository(database)

        // Prepopulate orders if database is completely empty to match the React spec
        viewModelScope.launch {
            val existingOrders = repository.orders.first()
            if (existingOrders.isEmpty()) {
                val prepopulated = listOf(
                    OrderEntity(
                        id = "ORD-88392",
                        date = "Oct 24, 2023",
                        status = "Shipped",
                        itemsCount = 2,
                        total = 5200.0,
                        imageUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=300&h=300&fit=crop",
                        isHistory = false
                    ),
                    OrderEntity(
                        id = "ORD-92110",
                        date = "Oct 26, 2023",
                        status = "Processing",
                        itemsCount = 1,
                        total = 2100.0,
                        imageUrl = "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=300&h=300&fit=crop",
                        isHistory = false
                    ),
                    OrderEntity(
                        id = "ORD-11029",
                        date = "Sep 12, 2023",
                        status = "Delivered",
                        itemsCount = 3,
                        total = 8900.0,
                        imageUrl = "https://images.unsplash.com/photo-1551028919-383718603bd5?w=300&h=300&fit=crop",
                        isHistory = true
                    ),
                    OrderEntity(
                        id = "ORD-09281",
                        date = "Aug 05, 2023",
                        status = "Delivered",
                        itemsCount = 1,
                        total = 49900.0,
                        imageUrl = "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=300&h=300&fit=crop",
                        isHistory = true
                    )
                )
                prepopulated.forEach { repository.placeOrder(it) }
            }
        }
    }

    // Reactive database flows
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val orders: StateFlow<List<OrderEntity>> = repository.orders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Mock Shorts Data
    private val _shortsData = MutableStateFlow<List<ShortVideo>>(
        listOf(
            ShortVideo(
                id = 1,
                username = "neon_vibes",
                userImg = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop",
                videoUrl = "",
                posterUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&h=1400&fit=crop",
                likes = "500K",
                rating = "4.8",
                shares = "10K",
                product = Product(
                    id = 101,
                    name = "Maroon Designer Coat",
                    price = 4200.0,
                    imageUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=400&h=400&fit=crop",
                    description = "Sophisticated warm double-breasted maroon coat crafted with premium wool blend. Perfect for winters."
                ),
                category = "Female"
            ),
            ShortVideo(
                id = 2,
                username = "tech_guru",
                userImg = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop",
                videoUrl = "",
                posterUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&h=1400&fit=crop",
                likes = "8.5K",
                rating = "4.2",
                shares = "205",
                product = Product(
                    id = 102,
                    name = "Smart Watch Series 7",
                    price = 1000.0,
                    imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=400&fit=crop",
                    description = "Feature-packed fitness smart watch with continuous pulse sensor, AMOLED display, and up to 10 days of battery life."
                ),
                category = "Accessories"
            ),
            ShortVideo(
                id = 3,
                username = "street_drip",
                userImg = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=100&h=100&fit=crop",
                videoUrl = "",
                posterUrl = "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=800&h=1400&fit=crop",
                likes = "112K",
                rating = "4.9",
                shares = "4.5K",
                product = Product(
                    id = 103,
                    name = "Neon Street Sneakers",
                    price = 2400.0,
                    imageUrl = "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=400&h=400&fit=crop",
                    description = "Stand out in any crowd with these ultra-comfortable retro street sneakers in reflective neon highlights."
                ),
                category = "Male"
            ),
            ShortVideo(
                id = 4,
                username = "biker_chic",
                userImg = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop",
                videoUrl = "",
                posterUrl = "https://images.unsplash.com/photo-1551028919-383718603bd5?w=800&h=1400&fit=crop",
                likes = "45K",
                rating = "4.5",
                shares = "1.2K",
                product = Product(
                    id = 104,
                    name = "Vantage Leather Jacket",
                    price = 5800.0,
                    imageUrl = "https://images.unsplash.com/photo-1551028919-383718603bd5?w=400&h=400&fit=crop",
                    description = "Classic jet-black slim fit leather biker jacket with premium steel zippers and comfortable inner lining."
                ),
                category = "Male"
            ),
            ShortVideo(
                id = 5,
                username = "cyber_vibe",
                userImg = "https://images.unsplash.com/photo-1521119989659-a83eee488004?w=100&h=100&fit=crop",
                videoUrl = "",
                posterUrl = "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=800&h=1400&fit=crop",
                likes = "89K",
                rating = "4.6",
                shares = "3K",
                product = Product(
                    id = 105,
                    name = "Futuristic Cyber Shades",
                    price = 1200.0,
                    imageUrl = "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=400&h=400&fit=crop",
                    description = "Polarized rimless aerodynamic high-contrast lenses with lightweight steel arms. Perfect street style shield."
                ),
                category = "Accessories"
            )
        )
    )
    val shortsData: StateFlow<List<ShortVideo>> = _shortsData.asStateFlow()

    // Category Filtering State
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredShorts: StateFlow<List<ShortVideo>> = combine(_shortsData, _selectedCategory) { shorts, category ->
        if (category == "All") {
            shorts
        } else {
            shorts.filter { it.category.equals(category, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // User Profile Data
    val profileHighlights = listOf(
        Pair("Goa", "https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=200&h=200&fit=crop"),
        Pair("Gym", "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=200&h=200&fit=crop"),
        Pair("Vibes", "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=200&h=200&fit=crop")
    )

    val profilePosts = listOf(
        "https://images.unsplash.com/photo-1592827095305-68f21edefb82?w=400&h=400&fit=crop",
        "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400&h=400&fit=crop",
        "https://images.unsplash.com/photo-1521119989659-a83eee488004?w=400&h=400&fit=crop",
        "https://images.unsplash.com/photo-1488161628813-04466f872507?w=400&h=400&fit=crop",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&h=400&fit=crop",
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&h=400&fit=crop"
    )

    // AI Stylist States
    private val _aiAdvice = MutableStateFlow("")
    val aiAdvice: StateFlow<String> = _aiAdvice.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // AI Caption Maker States
    private val _captionTopic = MutableStateFlow("")
    val captionTopic: StateFlow<String> = _captionTopic.asStateFlow()

    private val _aiCaption = MutableStateFlow("")
    val aiCaption: StateFlow<String> = _aiCaption.asStateFlow()

    private val _isGeneratingCaption = MutableStateFlow(false)
    val isGeneratingCaption: StateFlow<Boolean> = _isGeneratingCaption.asStateFlow()

    // Cart Management
    fun addToCart(product: Product, size: String = "M", color: String = "Default") {
        viewModelScope.launch {
            val currentItems = cartItems.value
            val existing = currentItems.find { it.productId == product.id && it.size == size && it.color == color }
            if (existing != null) {
                repository.updateCartItem(existing.copy(quantity = existing.quantity + 1))
            } else {
                repository.addToCart(
                    CartItem(
                        productId = product.id,
                        name = product.name,
                        price = product.price,
                        quantity = 1,
                        size = size,
                        color = color,
                        imageUrl = product.imageUrl
                    )
                )
            }
        }
    }

    fun updateCartItemQty(id: Int, delta: Int) {
        viewModelScope.launch {
            val item = cartItems.value.find { it.id == id } ?: return@launch
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                repository.deleteCartItem(id)
            } else {
                repository.updateCartItem(item.copy(quantity = newQty))
            }
        }
    }

    fun removeCartItem(id: Int) {
        viewModelScope.launch {
            repository.deleteCartItem(id)
        }
    }

    fun placeOrder() {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isEmpty()) return@launch

            val orderId = "ORD-${Random.nextInt(10000, 99999)}"
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val currentDate = sdf.format(Date())
            val subtotal = items.sumOf { it.price * it.quantity }
            val tax = subtotal * 0.05
            val total = subtotal + tax

            val firstItemImage = items.firstOrNull()?.imageUrl ?: ""

            val newOrder = OrderEntity(
                id = orderId,
                date = currentDate,
                status = "Processing",
                itemsCount = items.sumOf { it.quantity },
                total = total,
                imageUrl = firstItemImage,
                isHistory = false
            )

            repository.placeOrder(newOrder)
            repository.clearCart()
        }
    }

    // AI Stylist API Trigger
    fun askAiStylist() {
        val items = cartItems.value
        if (items.isEmpty()) return
        
        _isAiLoading.value = true
        _aiAdvice.value = ""

        viewModelScope.launch {
            val itemNames = items.joinToString(", ") { "${it.name} (${it.color})" }
            val prompt = "I am buying these items: $itemNames. Give me a short, stylish 2-sentence tip on how to pair or wear these items together or separately. Be encouraging and use fashion emojis!"
            val systemMsg = "You are a high-end Gen-Z fashion stylist assistant for VShop, a trendy video shopping app."

            val result = GeminiClient.generateContent(prompt, systemMsg)
            _aiAdvice.value = result
            _isAiLoading.value = false
        }
    }

    // AI Caption Maker API Trigger
    fun setCaptionTopic(topic: String) {
        _captionTopic.value = topic
    }

    fun generateAiCaption() {
        val topic = _captionTopic.value
        if (topic.trim().isEmpty()) return

        _isGeneratingCaption.value = true
        _aiCaption.value = ""

        viewModelScope.launch {
            val prompt = "Write a very catchy, viral short video caption for a post about: \"$topic\". Include 4-5 relevant trending fashion hashtags and a couple of emojis. Keep it under 3 sentences."
            val systemMsg = "You are a Gen-Z social media influencer and styling expert."

            val result = GeminiClient.generateContent(prompt, systemMsg)
            _aiCaption.value = result
            _isGeneratingCaption.value = false
        }
    }

    fun resetCaption() {
        _captionTopic.value = ""
        _aiCaption.value = ""
        _isGeneratingCaption.value = false
    }

    fun uploadVideo(caption: String, productName: String, price: Double, imageUrl: String, videoUrl: String, description: String, category: String) {
        val newVideoId = (_shortsData.value.maxOfOrNull { it.id } ?: 0) + 1
        val newVideo = ShortVideo(
            id = newVideoId,
            username = "vshop_creator",
            userImg = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop",
            videoUrl = videoUrl,
            posterUrl = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&h=1000&fit=crop" },
            likes = "1",
            rating = "5.0",
            shares = "0",
            product = Product(
                id = 1000 + newVideoId,
                name = productName.ifEmpty { "Creator's Featured Style" },
                price = if (price <= 0.0) 1999.0 else price,
                imageUrl = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&h=1000&fit=crop" },
                description = description.ifEmpty { caption }
            ),
            category = category
        )
        _shortsData.value = listOf(newVideo) + _shortsData.value
    }
}
