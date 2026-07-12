package com.example.data

import kotlinx.coroutines.flow.Flow

class VShopRepository(private val database: AppDatabase) {
    val cartItems: Flow<List<CartItem>> = database.cartDao().getCartItems()
    val orders: Flow<List<OrderEntity>> = database.orderDao().getOrders()

    suspend fun addToCart(item: CartItem) {
        database.cartDao().insert(item)
    }

    suspend fun updateCartItem(item: CartItem) {
        database.cartDao().update(item)
    }

    suspend fun deleteCartItem(id: Int) {
        database.cartDao().deleteById(id)
    }

    suspend fun clearCart() {
        database.cartDao().clear()
    }

    suspend fun placeOrder(order: OrderEntity) {
        database.orderDao().insert(order)
    }
}
